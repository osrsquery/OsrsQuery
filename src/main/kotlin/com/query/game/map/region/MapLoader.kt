package com.query.game.map.region

import com.query.Application
import com.query.Constants
import com.query.game.map.region.data.MapDefinition
import com.query.game.map.region.data.Tile
import com.query.utils.byte
import com.query.utils.uByte
import com.query.utils.uShort
import java.nio.ByteBuffer


class MapLoader {

	private val readOverlayAsShort = Application.revision >= Constants.OVERLAY_SHORT_BREAKING_CHANGE_REV_NUMBER

	fun load(regionX: Int, regionY: Int, b: ByteArray): MapDefinition {
		val map = MapDefinition(regionX,regionY)
		loadTerrain(map, b)
		return map
	}

	private fun loadTerrain(map: MapDefinition, data: ByteArray) {
		val tiles = map.tiles
		val buffer = ByteBuffer.wrap(data)
		for (z in 0 until regionSizeZ) {
			for (x in 0 until regionSizeX) {
				for (y in 0 until regionSizeY) {
					tiles[z][x][y] = Tile()
					val tile = tiles[z][x][y]
					while (true) {
						val attribute: Int = if (readOverlayAsShort) buffer.uShort else buffer.uByte
						if (attribute == 0) {
							break
						} else if (attribute == 1) {
							val height: Int = buffer.uByte
							tile.height = height
							break
						} else if (attribute <= 49) {
							tile.attrOpcode = attribute
							tile.overlayId = if (readOverlayAsShort) buffer.uShort.toShort() else buffer.uByte.toShort()
							tile.overlayPath = ((attribute - 2) / 4).toByte()
							tile.overlayRotation = (attribute - 2 and 3).toByte()
						} else if (attribute <= 81) {
							tile.settings = (attribute - 49).toByte()
						} else {
							tile.underlayId = (attribute - 81).toByte().toShort()
						}
					}
				}
			}
		}
	}

}