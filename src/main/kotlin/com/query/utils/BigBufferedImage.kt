package com.query.utils

import java.util.concurrent.Callable
import java.awt.Rectangle
import javax.imageio.ImageIO
import java.awt.Point
import java.awt.color.ColorSpace
import java.awt.image.*
import java.io.File
import java.io.RandomAccessFile
import java.nio.MappedByteBuffer
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.Exception
import java.lang.RuntimeException
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.lang.InterruptedException
import java.lang.IllegalArgumentException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.experimental.and

class BigBufferedImage private constructor(
    cm: ColorModel,
    raster: SimpleRaster,
    isRasterPremultiplied: Boolean,
    properties: Hashtable<*, *>?
) : BufferedImage(cm, raster, isRasterPremultiplied, properties) {
    private class ImagePartLoader(
        private val y: Int,
        width: Int,
        height: Int,
        private val file: File,
        private val image: BufferedImage
    ) : Callable<ImagePartLoader> {
        private val region: Rectangle

        init {
            region = Rectangle(0, y, width, height)
        }

        @Throws(Exception::class)
        override fun call(): ImagePartLoader {
            Thread.currentThread().priority = (Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2
            ImageIO.createImageInputStream(file).use { stream ->
                val readers = ImageIO.getImageReaders(stream)
                if (readers.hasNext()) {
                    val reader = readers.next()
                    reader.setInput(stream, true, true)
                    val param = reader.defaultReadParam
                    param.sourceRegion = region
                    val part = reader.read(0, param)
                    val source: Raster = part.raster
                    val target = image.raster
                    target.setRect(0, y, source)
                }
            }
            return this@ImagePartLoader
        }
    }

    fun dispose() {
        (raster as SimpleRaster).dispose()
    }

    private class SimpleRaster(sampleModel: SampleModel?, dataBuffer: FileDataBuffer?, origin: Point?) :
        WritableRaster(sampleModel, dataBuffer, origin) {
        fun dispose() {
            (getDataBuffer() as FileDataBuffer).dispose()
        }
    }

    private class FileDataBufferDeleterHook : Thread() {
        override fun run() {
            val buffers = undisposedBuffers.toTypedArray()
            for (b in buffers) {
                b.disposeNow()
            }
        }

        companion object {
            init {
                Runtime.getRuntime().addShutdownHook(FileDataBufferDeleterHook())
            }

            val undisposedBuffers = HashSet<FileDataBuffer>()
        }
    }

    private class FileDataBuffer : DataBuffer {
        private val id = "buffer-" + System.currentTimeMillis() + "-" + (Math.random() * 1000).toInt()
        private var dir: File?
        private var path: String? = null
        var files: Array<File?>? = null
        var accessFiles: Array<RandomAccessFile?>? =  null
        var buffer: Array<MappedByteBuffer?>? = null

        constructor(dir: File?, size: Int) : super(TYPE_BYTE, size) {
            this.dir = dir
            init()
        }

        constructor(dir: File?, size: Int, numBanks: Int) : super(TYPE_BYTE, size, numBanks) {
            this.dir = dir
            init()
        }

        @Throws(FileNotFoundException::class, IOException::class)
        private fun init() {
            FileDataBufferDeleterHook.undisposedBuffers.add(this)
            if (dir == null) {
                dir = File(".")
            }
            if (!dir!!.exists()) {
                throw RuntimeException("FileDataBuffer constructor parameter dir does not exist: $dir")
            }
            if (!dir!!.isDirectory) {
                throw RuntimeException("FileDataBuffer constructor parameter dir is not a directory: $dir")
            }
            path = dir!!.path + "/" + id
            val subDir = File(path)
            subDir.mkdir()
            buffer = arrayOfNulls(banks)
            accessFiles = arrayOfNulls(banks)
            files = arrayOfNulls(banks)
            for (i in 0 until banks) {
                files!![i] = File("$path/bank$i.dat")
                val file = files!![i]
                accessFiles!![i] = RandomAccessFile(file, "rw")
                val randomAccessFile = accessFiles!![i]
                buffer!![i] = randomAccessFile!!.channel.map(FileChannel.MapMode.READ_WRITE, 0, getSize().toLong())
            }
        }

        override fun getElem(bank: Int, i: Int): Int {
            return (buffer!![bank]!![i] and 0xff.toByte()).toInt()
        }

        override fun setElem(bank: Int, i: Int, `val`: Int) {
            buffer!![bank]!!.put(i, `val`.toByte())
        }

        @Throws(Throwable::class)
        protected fun finalize() {
            dispose()
        }

        fun dispose() {
            object : Thread() {
                override fun run() {
                    disposeNow()
                }
            }.start()
        }

        fun disposeNow() {
            buffer = null
            if (accessFiles != null) {
                for (file in accessFiles!!) {
                    try {
                        file!!.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                accessFiles = null
            }
            if (files != null) {
                for (file in files!!) {
                    file!!.delete()
                }
                files = null
            }
            if (path != null) {
                File(path).delete()
                path = null
            }
        }
    }

    companion object {
        private val TMP_DIR = System.getProperty("java.io.tmpdir")
        const val MAX_PIXELS_IN_MEMORY = 1024 * 1024
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
                            partLoaders.add(
                                ImagePartLoader(
                                    y, width, Math.min(block, height - y), inputFile, image
                                )
                            )
                            y += block
                        }
                        generalExecutor.invokeAll(partLoaders)
                        generalExecutor.shutdown()
                        return image
                    } catch (ex: InterruptedException) {
                        Logger.getLogger(BigBufferedImage::class.java.name).log(Level.SEVERE, null, ex)
                    }
                }
            }
            return null
        }

        @Throws(FileNotFoundException::class, IOException::class)
        private fun createBigBufferedImage(
            tempDir: File,
            width: Int,
            height: Int,
            imageType: Int
        ): BufferedImage {
            val buffer = FileDataBuffer(tempDir, width * height, 4)
            var colorModel: ColorModel? = null
            var sampleModel: BandedSampleModel? = null
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