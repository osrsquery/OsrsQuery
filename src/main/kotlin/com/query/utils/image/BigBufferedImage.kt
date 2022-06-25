package com.query.utils.image

import java.awt.Point
import java.awt.color.ColorSpace
import java.awt.image.*
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import javax.imageio.ImageIO

/*
 * This class is part of MCFS (Mission Control - Flight Software) a development
 * of Team Puli Space, official Google Lunar XPRIZE contestant.
 * This class is released under Creative Commons CC0.
 * @author Zsolt Pocze, Dimitry Polivaev
 * Please like us on facebook, and/or join our Small Step Club.
 * http://www.pulispace.com
 * https://www.facebook.com/pulispace
 * http://nyomdmegteis.hu/en/
 */
class BigBufferedImage private constructor(
    cm: ColorModel,
    raster: SimpleRaster,
    isRasterPremultiplied: Boolean,
    properties: Hashtable<*, *>?
) : BufferedImage(cm, raster, isRasterPremultiplied, properties) {

    fun dispose() {
        (raster as SimpleRaster).dispose()
    }

    private class SimpleRaster(sampleModel: SampleModel?, dataBuffer: FileDataBuffer?, origin: Point?) :
        WritableRaster(sampleModel, dataBuffer, origin) {
        fun dispose() {
            (getDataBuffer() as FileDataBuffer).dispose()
        }
    }

    companion object {
        private val TMP_DIR = System.getProperty("java.io.tmpdir")
        private const val MAX_PIXELS_IN_MEMORY = 1024 * 1024
        fun create(width: Int, height: Int, imageType: Int): BufferedImage {
            return if (width * height > MAX_PIXELS_IN_MEMORY) {
                try {
                    val tempDir = File(TMP_DIR)
                    createBigBufferedImage(tempDir, width, height, imageType)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            } else {
                BufferedImage(width, height, imageType)
            }
        }

        @Throws(IOException::class)
        fun create(inputFile: File, imageType: Int): BufferedImage? {
            ImageIO.createImageInputStream(inputFile).use { stream ->
                val readers = ImageIO.getImageReaders(stream)
                if (readers.hasNext()) {
                    try {
                        val reader = readers.next()
                        reader.setInput(stream, true, true)
                        val width = reader.getWidth(reader.minIndex)
                        val height = reader.getHeight(reader.minIndex)
                        val image = create(width, height, imageType)
                        val cores = Math.max(1, Runtime.getRuntime().availableProcessors() / 2)
                        val block = Math.min(
                            MAX_PIXELS_IN_MEMORY / cores / width, Math.ceil(height / cores.toDouble())
                                .toInt()
                        )
                        val generalExecutor = Executors.newFixedThreadPool(cores)
                        val partLoaders: MutableList<Callable<ImagePartLoader>> = ArrayList()
                        var y = 0
                        while (y < height) {
                            partLoaders.add(ImagePartLoader(y, width, block.coerceAtMost(height - y), inputFile, image))
                            y += block
                        }
                        generalExecutor.invokeAll(partLoaders)
                        generalExecutor.shutdown()
                        return image
                    } catch (ex: InterruptedException) {
                        ex.printStackTrace()
                    }
                }
            }
            return null
        }

        @Throws(IOException::class)
        private fun createBigBufferedImage(tempDir: File, width: Int, height: Int, imageType: Int): BufferedImage {
            val buffer = FileDataBuffer(tempDir, width * height, 4)
            val colorModel: ColorModel
            val sampleModel: BandedSampleModel
            when (imageType) {
                TYPE_INT_RGB -> {
                    colorModel = ComponentColorModel(
                        ColorSpace.getInstance(ColorSpace.CS_sRGB),
                        intArrayOf(8, 8, 8, 0),
                        false,
                        false,
                        TRANSLUCENT,
                        DataBuffer.TYPE_BYTE
                    )
                    sampleModel = BandedSampleModel(DataBuffer.TYPE_BYTE, width, height, 3)
                }
                TYPE_INT_ARGB -> {
                    colorModel = ComponentColorModel(
                        ColorSpace.getInstance(ColorSpace.CS_sRGB),
                        intArrayOf(8, 8, 8, 8),
                        true,
                        false,
                        TRANSLUCENT,
                        DataBuffer.TYPE_BYTE
                    )
                    sampleModel = BandedSampleModel(DataBuffer.TYPE_BYTE, width, height, 4)
                }
                else -> throw IllegalArgumentException("Unsupported image type: $imageType")
            }
            val raster = SimpleRaster(sampleModel, buffer, Point(0, 0))
            return BigBufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null)
        }

        fun dispose(image: RenderedImage?) {
            if (image is BigBufferedImage) {
                image.dispose()
            }
        }

    }
}