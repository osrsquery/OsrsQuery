package com.query.cache.download

import com.displee.cache.CacheLibrary
import com.displee.cache.ProgressListener
import com.google.gson.Gson
import com.query.Application.loadProperties
import com.query.Application.saveProperties
import com.query.Constants.CACHE_DOWNLOAD_LOCATION
import com.query.Constants.library
import com.query.Constants.properties
import com.query.utils.FileUtils
import com.query.utils.TimeUtils
import com.query.utils.jsonToString
import com.query.utils.progress
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarStyle
import mu.KotlinLogging
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis


data class CacheInfo(
    val id : Int,
    val game : String,
    val timestamp : String,
    val builds : List<CacheInfoBuilds>,
    val sources : List<String>,
    val size : Long
)

data class Xteas(
    val archive : Int,
    val group : Int,
    val name_hash : Long,
    val name : String,
    val mapsquare : Int,
    val key : Array<Long>,
)

data class CacheInfoBuilds(
    val major : Int
)

object UpdateCache {

    private val logger = KotlinLogging.logger {}

    fun initialize() {
        var latestCache = true
        loadProperties()
        val time = measureTimeMillis {
            logger.info { "Looking for cache Updates" }
            val caches = Gson().fromJson(URL(CACHE_DOWNLOAD_LOCATION).readText(), Array<CacheInfo>::class.java)
            val latest = getLatest(caches)!!
            if(properties.getProperty("cache-version") != latest.timestamp) {
                latestCache = false
                download(latest)
                saveXteas(latest)
                unZip()
                FileUtils.getFile("cache/osrs/","cache.zip").delete()
                properties.setProperty("cache-version", latest.timestamp)
                saveProperties(properties)
            }
        }
        val message = if(!latestCache) "Cache Downloaded in ${TimeUtils.millsToFormat(time)}" else "Cache is Latest"
        logger.info { message }


        val pb = progress("Loading Cache",19L)

        library = CacheLibrary(FileUtils.getDir("cache/osrs/cache/").toString(), false, object : ProgressListener {
            override fun notify(progress: Double, message: String?) {
                pb.step()
            }
        })
    }

    private fun unZip() {
        val path = FileUtils.getFile("cache/osrs/","cache.zip")
        val zipFile = ZipFile(path)
        try {
            logger.info { "Unzipping Cache please wait" }
            zipFile.extractAll(FileUtils.getDir("cache/osrs/").toString())
        } catch (e: ZipException) {
            logger.error { "Unable extract files from $path : $e" }
        }

    }

    private fun download(cache : CacheInfo) {
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

    private fun saveXteas(cache : CacheInfo) {
        logger.info { "Saving Xteas" }
        val file = FileUtils.getFile("cache/osrs/","xteas.json")
        try {
            val url = "https://archive.openrs2.org/caches/${cache.id}/keys.json"
            val xteas = Gson().fromJson(URL(url).readText(), Array<Xteas>::class.java)
            var output = BufferedWriter(FileWriter(file))
            output.write(xteas.jsonToString(true))
            output.close()
        }catch (e : Exception) {
            logger.error { "Unable to get Xteas $e" }
        }
    }

    private fun getLatest(caches : Array<CacheInfo>) = caches.filter {
        it.game.contains("oldschool")
                && it.builds.isNotEmpty()
                && it.builds.first().major >= 202
                && it.sources.contains("Jagex")
        }.maxByOrNull { getTime(it.timestamp) }


    private fun getTime(text : String) : Long {
        val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
        val time = text.replace("T"," ").replaceAfterLast(".","")
        val dateTime = LocalDateTime.parse(replaceLastLetter(time,""), formatter)
        return dateTime.toEpochSecond(ZoneOffset.UTC)
    }

    private fun replaceLastLetter(text: String, newLetter: String): String? {
        val substring = text.substring(0, text.length - 1) // ABC -> AB
        return substring + newLetter // ABD
    }

    @JvmStatic
    fun main(args : Array<String>) {
        initialize()
    }

}