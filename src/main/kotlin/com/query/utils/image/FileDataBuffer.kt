package com.query.utils.image

import java.awt.image.DataBuffer
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class FileDataBuffer : DataBuffer {
    private val id = "buffer-" + System.currentTimeMillis() + "-" + (Math.random() * 1000).toInt()
    private var dir: File?
    private var path: String? = null
    private var files: Array<File?>? = null
    private var accessFiles: Array<RandomAccessFile?>? = null
    private var buffer: Array<MappedByteBuffer?>? = null

    constructor(dir: File?, size: Int) : super(TYPE_BYTE, size) {
        this.dir = dir
        init()
    }

    constructor(dir: File?, size: Int, numBanks: Int) : super(TYPE_BYTE, size, numBanks) {
        this.dir = dir
        init()
    }

    @Throws(IOException::class)
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
        return buffer!![bank]!![i].toInt() and 0xff
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