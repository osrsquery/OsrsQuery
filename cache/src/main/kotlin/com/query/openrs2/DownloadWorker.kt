package com.query.openrs2

import com.google.gson.GsonBuilder
import com.query.utils.unzipFileWithProgress
import java.io.*
import javax.swing.*
import java.net.HttpURLConnection
import java.net.URL

class DownloadWorker(
    private val cache: CacheInfo,
    private val outputPath: File,
    private val onComplete: () -> Unit,
    private val onProgress: (Double) -> Unit
) : SwingWorker<Void, Double>() {

    override fun doInBackground(): Void? {
        downloadFileWithProgress(cache, outputPath)
        return null
    }

    override fun process(chunks: List<Double>) {
        val progress = chunks.last()
        onProgress.invoke(progress)
    }

    override fun done() {
        unzipFileWithProgress(File(outputPath, "${cache.id}.zip"), outputPath, {
            onProgress.invoke(it)
        }, {
            onComplete.invoke()
            File(outputPath, "${cache.id}.zip").delete()
            writeKeysJsonToFile(cache, outputPath)
            writeCacheInfoToJsonFile(cache, outputPath)
        })
    }

    private fun downloadFileWithProgress(cache: CacheInfo, outputPath: File) {
        val url = URL("https://archive.openrs2.org/caches/runescape/${cache.id}/disk.zip")
        val httpURLConnection = url.openConnection() as HttpURLConnection

        try {
            val contentLength = cache.size
            val bufferSize = 1024
            val buffer = ByteArray(bufferSize)
            var bytesRead: Int
            var totalBytesRead: Long = 0

            val inputStream: InputStream = httpURLConnection.inputStream
            val bufferedInputStream = BufferedInputStream(inputStream)
            val fileOutputStream = FileOutputStream(File(outputPath, "${cache.id}.zip"))

            while (bufferedInputStream.read(buffer).also { bytesRead = it } != -1) {
                fileOutputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead.toLong()

                // Calculate progress if content length is known
                val progress = totalBytesRead.toDouble() / contentLength * 100
                onProgress.invoke(progress)
            }

            // Close streams and disconnect
            bufferedInputStream.close()
            fileOutputStream.close()
            httpURLConnection.disconnect()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun writeKeysJsonToFile(cache: CacheInfo, outputPath: File) {
        val keysJson = URL("https://archive.openrs2.org/caches/${cache.id}/keys.json").readText()
        val output = BufferedWriter(FileWriter(File(outputPath, "xteas.json")))
        output.use { writer -> writer.write(keysJson) }
    }

    private fun writeCacheInfoToJsonFile(cache: CacheInfo, outputPath: File) {
        val cacheOutput = BufferedWriter(FileWriter(File(outputPath, "info.json").absolutePath))
        val gson = GsonBuilder().setPrettyPrinting().create()
        cacheOutput.use { writer -> writer.write(gson.toJson(cache)) }
    }
}