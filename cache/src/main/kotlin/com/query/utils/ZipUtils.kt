package com.query.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

fun unzipFileWithProgress(zipFile: File, outputDirectory: File, onProgress: (Double) -> Unit, onFinished: () -> Unit) {
    val bufferSize = 1024
    val buffer = ByteArray(bufferSize)

    val zipInputStream = ZipInputStream(FileInputStream(zipFile))
    var entry: ZipEntry?
    var totalBytesRead: Long = 0
    val zipSize = zipFile.length()

    while (zipInputStream.nextEntry.also { entry = it } != null) {
        val entryFile = File(outputDirectory, entry!!.name)
        if (entry!!.isDirectory) {
            entryFile.mkdirs()
        } else {
            val entryOutputStream = FileOutputStream(entryFile)
            var bytesRead: Int
            while (zipInputStream.read(buffer).also { bytesRead = it } != -1) {
                entryOutputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead.toLong()

                // Calculate progress
                val progress = (totalBytesRead.toDouble() / zipSize) * 100
                onProgress.invoke(progress)
            }
            entryOutputStream.close()
        }
    }
    zipInputStream.close()
    onFinished.invoke()
}