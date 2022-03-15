package com.query.utils

import com.query.cache.download.CacheInfo
import mu.KotlinLogging
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.system.exitProcess

object DownloadUtils {

    val logger = KotlinLogging.logger {}

    fun downloadCache(cache : CacheInfo) {
        try {
            val url = URL("https://archive.openrs2.org/caches/${cache.id}/disk.zip")
            val httpConnection = url.openConnection() as HttpURLConnection
            val completeFileSize = cache.size
            val input : InputStream = httpConnection.inputStream
            val out = FileOutputStream(FileUtils.getFile("cache/osrs/","cache.zip"))

            val data = ByteArray(1024)
            var downloadedFileSize: Long = 0
            var count: Int

            val pb = progress("Downloading Cache",completeFileSize)

            while (input.read(data, 0, 1024).also { count = it } != -1) {
                downloadedFileSize += count.toLong()
                pb.stepBy(count.toLong())
                out.write(data, 0, count)
            }
            pb.close()
        }catch (e: Exception) {
            logger.error { "Unable to download Cache: $e" }
            exitProcess(0)
        }

    }

}