package com.query.utils.image

import java.util.HashSet

class FileDataBufferDeleterHook : Thread() {
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