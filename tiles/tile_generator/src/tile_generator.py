"""
Tile generator for creating tile sets for OSRS maps to be used with map viewers such as leaflet.
"""

import logging
import math
import os
from enum import Enum
from pathlib import Path

from contextlib import contextmanager
from concurrent.futures import ThreadPoolExecutor, as_completed, thread
import concurrent.futures
import numpy as np
import pyvips
from PIL import Image, ImageDraw
from skimage.metrics import structural_similarity

CACHES_BASE_URL = "https://archive.openrs2.org"

LOG = logging.getLogger(__name__)
LOG.setLevel(logging.INFO)

Image.MAX_IMAGE_PIXELS = 1000000000000000

TILE_SIZE_PX = 256

MIN_ZOOM = 3
MAX_ZOOM = 11

MIN_Z = 0
MAX_Z = 3

REPO_DIR = 'E://RSPS//OsrsQuery//OsrsQuery//tiles' # Name of the directory mounted on the local machine
ROOT_CACHE_DIR = os.path.join(REPO_DIR, 'cache/')
GENERATED_FULL_IMAGES_OLD = os.path.join(REPO_DIR, 'rev-1/')
GENERATED_FULL_IMAGES_NEW = os.path.join(REPO_DIR, 'rev-18/')
GENERATED_FULL_IMAGES = os.path.join(REPO_DIR, 'gen/')
TILE_DIR = REPO_DIR

image_prefix = "full_image_"

logging.basicConfig(
    format='%(asctime)s %(levelname)-4s %(message)s',
    level=logging.WARNING,
    datefmt='%Y-%m-%d %H:%M:%S'
)

DEFAULT_TILE_IMAGE = (
    pyvips.Image.black(TILE_SIZE_PX, TILE_SIZE_PX, bands=4)
    .draw_rect([0, 0, 0, 0], 0, 0, TILE_SIZE_PX, TILE_SIZE_PX, fill=True)
)

def generate_tiles_for_plane(plane):
    """
        Generates OSRS map tiles for a given Z plane.
        
        The tiles are generated from the full OSRS map image by:
            1. Finding which "zoom" level the full image is in
            2. Splitting the full image into tiles at each higher zoom level from the original (e.g. 8 -> 9 -> 10 -> 11)
            3. Joining split tiles in the original zoom level to lower zoom levels (e.g. 8 -> 7 -> 6)

        This process is optimised by first diffing the new full OSRS map image with the previously generated OSRS Map image for the previous cache version.
        We use pyvips to compare each tile at the original zoom level (e.g. 8) with the tile at the same coordinates at the same zoom level in the old image.
        We then only split & join tiles which have changed, to higher or lower zoom levels. This massively reduces the output size & run time of the program.

        In an ideal world we would load the new cache & old cache (with XTEAs) and compare the underlying data for a given region to see if tiles have changed.
        However I'm too lazy to implement that right now, so instead we're just comparing images.

        Where possible we also utilise threadpools to speed up the process.
    """
    log_prefix = f"[Plane: {plane}]:"

    LOG.info(f"{log_prefix} Generating plane {plane}")
    LOG.info(f"{log_prefix} Loading images into memory")

    old_image_location = os.path.join(GENERATED_FULL_IMAGES, f"current-map-image-{plane}.png")
    new_image_location = os.path.join(GENERATED_FULL_IMAGES, f"new-map-image-{plane}.png")

    old_image = pyvips.Image.new_from_file(old_image_location)
    new_image = pyvips.Image.new_from_file(new_image_location)

    image_width = new_image.width
    image_width_tiles = int(image_width / TILE_SIZE_PX)

    starting_zoom = int(math.sqrt(math.pow(2, math.ceil(math.log(image_width_tiles) / math.log(2)))))

    LOG.info(f"{log_prefix} Calculating changed tiles")
    changed_tiles = get_changed_tiles(old_image, new_image, plane, starting_zoom)

    LOG.info(f"{log_prefix} Storing diff image")
    output_tile_diff_image(changed_tiles, new_image_location,  str(Path(GENERATED_FULL_IMAGES, f"diff-map-image-{plane}.png")))

    LOG.info(f"{log_prefix} Found {len(changed_tiles)} changed tiles at zoom level {starting_zoom}")

    LOG.info(f"{log_prefix} Saving changed tiles at zoom level {starting_zoom}")
    for tile in changed_tiles:
        save_tile(tile["image"], plane, starting_zoom, tile["x"], tile["y"])

    next_changed_tiles = changed_tiles
    for zoom in range(starting_zoom + 1, MAX_ZOOM + 1):
        LOG.info(f"{log_prefix} Splitting changed tiles from zoom level {zoom-1} to zoom level {zoom}")
        next_changed_tiles = split_tiles_to_new_zoom(next_changed_tiles, plane, zoom)
        LOG.info(f"{log_prefix} Done")

    for zoom in reversed(range(MIN_ZOOM + 1, starting_zoom + 1)):
        LOG.info(f"{log_prefix} Joining changed tiles from zoom level {zoom} to zoom level {zoom - 1}")
        changed_tiles = join_tiles_to_new_zoom(changed_tiles, plane, zoom, zoom - 1)
        LOG.info(f"{log_prefix} Done")

class Side(Enum):
    TOP_LEFT = 1
    TOP_RIGHT = 2
    BOTTOM_LEFT = 3
    BOTTOM_RIGHT = 4

def main():
    LOG.info("Generating tiles")
    for plane in range(MAX_Z + 1):
        generate_tiles_for_plane(plane)

    for plane in range(MIN_Z, MAX_Z + 1):
        previous_map_image_name = os.path.join(GENERATED_FULL_IMAGES_OLD, f"map-{plane}.png")
        current_map_image_name = os.path.join(GENERATED_FULL_IMAGES_NEW, f"map-{plane}.png")
        generated_file_name = os.path.join(GENERATED_FULL_IMAGES_NEW, f"img-{plane}.png")

        os.replace(current_map_image_name, previous_map_image_name)
        os.replace(generated_file_name, current_map_image_name)
        os.remove(generated_file_name)

def get_changed_tiles(old_image, new_image, plane, zoom):
    new_image_width_px = new_image.width
    new_image_height_px = new_image.height

    changed_tiles = []

    with thread_pool_executor() as executor:
        futures = []

        # Loop over all tiles in the new image
        for tile_x in range(0, new_image_width_px, TILE_SIZE_PX):
            for tile_y in range(0, new_image_height_px, TILE_SIZE_PX):
                futures.append(
                    executor.submit(
                        has_tile_changed,
                        plane=plane,
                        zoom=zoom,
                        tile_x=tile_x,
                        tile_y=tile_y,
                        old_image=old_image,
                        new_image=new_image
                    )
                )

        for future in concurrent.futures.as_completed(futures):
            ((tile_x, tile_y), new_image_tile, has_changed) = future.result()

            if has_changed:
                x = int(tile_x / TILE_SIZE_PX)
                y = int(tile_y / TILE_SIZE_PX)
                max_y = math.floor(new_image.height / TILE_SIZE_PX)
                y = max_y - y - 1

                changed_tiles.append({
                    "pixel_x": tile_x,
                    "pixel_y": tile_y,
                    "x": x,
                    "y": y,
                    "image": new_image_tile
                })

    return changed_tiles


def has_tile_changed(plane, zoom, tile_x, tile_y, old_image, new_image):
    new_image_tile = new_image.crop(tile_x, tile_y, TILE_SIZE_PX, TILE_SIZE_PX)

    # If there is no tile at (tile_x, tile_y) in the old image
    if tile_x > old_image.width - TILE_SIZE_PX or tile_y > old_image.height - TILE_SIZE_PX:
        return ((tile_x, tile_y), new_image_tile, True)

    old_image_tile = old_image.crop(tile_x, tile_y, TILE_SIZE_PX, TILE_SIZE_PX)

    old_image_buff = old_image_tile.write_to_memory()
    new_image_buff = new_image_tile.write_to_memory()

    old_image_np = np.ndarray(buffer=old_image_buff,
                            dtype=np.uint8,
                            shape=[old_image_tile.height, old_image_tile.width, old_image_tile.bands])

    new_image_np = np.ndarray(buffer=new_image_buff,
                            dtype=np.uint8,
                            shape=[new_image_tile.height, new_image_tile.width, new_image_tile.bands])

    ssim = structural_similarity(old_image_np, new_image_np, multichannel=True)

    has_changed = ssim < 0.999

    return ((tile_x, tile_y), new_image_tile, has_changed)


def split_tiles_to_new_zoom(changed_tiles, plane, new_zoom):
    new_changed_tiles = []

    with thread_pool_executor() as executor:
        futures = []

        for changed_tile in changed_tiles:
            futures.append(
                executor.submit(
                    split_tile_to_new_zoom,
                    changed_tile=changed_tile,
                    plane=plane,
                    new_zoom=new_zoom
                )
            )

        for future in concurrent.futures.as_completed(futures):
            new_changed_tiles.extend(future.result())

    return new_changed_tiles


def split_tile_to_new_zoom(changed_tile, plane, new_zoom):
    original_x = changed_tile["x"]
    original_y = changed_tile["y"]

    tile_image = changed_tile["image"]

    tile_image_resized = tile_image.resize(2, kernel='nearest')

    new_x = original_x * 2
    new_y = original_y * 2

    tile_image_0 = tile_image_resized.crop(0, TILE_SIZE_PX, TILE_SIZE_PX, TILE_SIZE_PX)
    save_tile(tile_image_0, plane, new_zoom, new_x, new_y)

    tile_image_1 = tile_image_resized.crop(TILE_SIZE_PX, TILE_SIZE_PX, TILE_SIZE_PX, TILE_SIZE_PX)
    save_tile(tile_image_1, plane, new_zoom, new_x + 1, new_y)

    tile_image_2 = tile_image_resized.crop(0, 0, TILE_SIZE_PX, TILE_SIZE_PX)
    save_tile(tile_image_2, plane, new_zoom, new_x, new_y + 1)

    tile_image_3 = tile_image_resized.crop(TILE_SIZE_PX, 0, TILE_SIZE_PX, TILE_SIZE_PX)
    save_tile(tile_image_3, plane, new_zoom, new_x + 1, new_y + 1)

    # New tiles at new zoom
    return [
        {
            "x": new_x,
            "y": new_y,
            "image": tile_image_0
        },
        {
            "x": new_x + 1,
            "y": new_y,
            "image": tile_image_1
        },
        {
            "x": new_x,
            "y": new_y + 1,
            "image": tile_image_2
        },
        {
            "x": new_x + 1,
            "y": new_y + 1,
            "image": tile_image_3
        }
    ]


def join_tiles_to_new_zoom(changed_tiles, plane, current_zoom, new_zoom):
    new_changed_tiles = []

    with thread_pool_executor() as executor:
        futures = []

        for changed_tile in changed_tiles:
            futures.append(
                executor.submit(
                    join_changed_tile_to_new_zoom,
                    changed_tile=changed_tile,
                    plane=plane,
                    current_zoom=current_zoom,
                    new_zoom=new_zoom
                )
            )

        for future in concurrent.futures.as_completed(futures):
            new_changed_tiles.append(future.result())

    return new_changed_tiles


def join_changed_tile_to_new_zoom(changed_tile, plane, current_zoom, new_zoom):
    original_x = changed_tile["x"]
    original_y = changed_tile["y"]

    is_left = original_x % 2 == 0
    is_bottom = original_y % 2 == 0

    if is_left:
        side = Side.BOTTOM_LEFT if is_bottom else Side.TOP_LEFT
    else:
        side = Side.BOTTOM_RIGHT if is_bottom else Side.TOP_RIGHT

    if side == Side.TOP_LEFT:
        tiles = [
            load_generated_tile(plane, current_zoom, original_x, original_y),
            load_generated_tile(plane, current_zoom, original_x + 1, original_y),
            load_generated_tile(plane, current_zoom, original_x, original_y - 1),
            load_generated_tile(plane, current_zoom, original_x + 1, original_y - 1)
        ]
    elif side == Side.TOP_RIGHT:
        tiles = [
            load_generated_tile(plane, current_zoom, original_x - 1, original_y),
            load_generated_tile(plane, current_zoom, original_x, original_y),
            load_generated_tile(plane, current_zoom, original_x - 1, original_y - 1),
            load_generated_tile(plane, current_zoom, original_x, original_y - 1)
        ]
    elif side == Side.BOTTOM_LEFT:
        tiles = [
            load_generated_tile(plane, current_zoom, original_x, original_y + 1),
            load_generated_tile(plane, current_zoom, original_x + 1, original_y + 1),
            load_generated_tile(plane, current_zoom, original_x, original_y),
            load_generated_tile(plane, current_zoom, original_x + 1, original_y)
        ]
    else:
        tiles = [
            load_generated_tile(plane, current_zoom, original_x - 1, original_y + 1),
            load_generated_tile(plane, current_zoom, original_x, original_y + 1),
            load_generated_tile(plane, current_zoom, original_x - 1, original_y),
            load_generated_tile(plane, current_zoom, original_x, original_y)
        ]

    new_tile_image = pyvips.Image.arrayjoin(tiles, across=2)

    new_tile_image_resized = new_tile_image.resize(0.5, kernel='lanczos3')

    new_x = math.floor(original_x / 2)
    new_y = math.floor(original_y / 2)

    save_tile(new_tile_image_resized, plane, new_zoom, new_x, new_y)

    return {
        "x": new_x,
        "y": new_y,
        "image": new_tile_image_resized
    }

def save_tile(tile_image, plane, zoom, x, y):
    file_dir = Path(TILE_DIR, str(plane), str(zoom), str(x))

    file_dir.mkdir(parents=True, exist_ok=True)

    file_path = Path(file_dir, str(y) + ".png")

    tile_image.pngsave(str(file_path), compression=9)


def load_generated_tile(plane, zoom, x, y):
    """
        Loads a tile image from the tile set stored in the repo
    """
    if not generated_tile_exists(plane, zoom, x, y):
        return DEFAULT_TILE_IMAGE

    file_path = generated_tile_path(plane, zoom, x, y)

    image = pyvips.Image.new_from_file(str(file_path), access="sequential")

    if not image.hasalpha():
        image = image.addalpha()

    return image


def generated_tile_exists(plane, zoom, x, y):
    return os.path.isfile(generated_tile_path(plane, zoom, x, y))


def generated_tile_path(plane, zoom, x, y):
    return Path(TILE_DIR, str(plane), str(zoom), str(x), str(y) + ".png")


def output_tile_diff_image(changed_tiles, new_image_location, output_file_name):
    diff_image = im = Image.open(new_image_location)

    draw = ImageDraw.Draw(im)

    for tile in changed_tiles:
        shape = [(tile["pixel_x"], tile["pixel_y"]), (tile["pixel_x"] + TILE_SIZE_PX, tile["pixel_y"] + TILE_SIZE_PX)]
        draw.rectangle(shape, fill=None, outline="red", width=3)

    diff_image.save(output_file_name)


@contextmanager
def thread_pool_executor():
    with ThreadPoolExecutor() as executor:
        try:
            yield executor
        except KeyboardInterrupt:
            LOG.warning("Performing non-graceful shutdown of threads")
            executor._threads.clear()
            concurrent.futures.thread._threads_queues.clear()


if __name__ == '__main__':
    main()
