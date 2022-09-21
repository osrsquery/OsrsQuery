package com.query.cache.download

import com.displee.cache.CacheLibrary
import com.displee.cache.ProgressListener
import com.google.gson.Gson
import com.query.Application
import com.query.Application.cacheInfo
import com.query.Application.loadProperties
import com.query.Application.revision
import com.query.Application.saveProperties
import com.query.Constants.CACHE_DOWNLOAD_LOCATION
import com.query.Constants.library
import com.query.Constants.properties
import com.query.cache.XteaLoader
import com.query.utils.*
import com.query.utils.DownloadUtils.downloadCache
import com.query.utils.FileUtils.getCacheLocation
import com.query.utils.ZipUtils.unZip
import mu.KotlinLogging
import pertinax.osrscd.CacheDownloader
import java.io.*
import java.net.URL
import java.util.*
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

object CacheLoader {

    private val logger = KotlinLogging.logger {}

    fun initialize() {
        var needsUpdate = true
        loadProperties()
        val time = measureTimeMillis {
            logger.info { "Looking for cache Updates" }
            val caches = Gson().fromJson(URL(CACHE_DOWNLOAD_LOCATION).readText(), Array<CacheInfo>::class.java)
            cacheInfo = if(revision == 0) getLatest(caches) else findRevision(revision,caches)
            needsUpdate = needsUpdate()
            if(needsUpdate) {
                if(Application.gameWorld == -1) {
                    downloadCache(cacheInfo)
                    unZip()
                    FileUtils.getFile("cache/osrs/","cache.zip").delete()
                } else {
                    CacheDownloader.downloadCache(Application.gameWorld)
                }
                saveXteas(cacheInfo)
                properties.setProperty("cache-version-${cacheInfo.builds[0].major}", cacheInfo.timestamp)
                saveProperties(properties)
            }
        }
        val message = if(needsUpdate) "Cache Downloaded in ${TimeUtils.millsToFormat(time)}" else "Cache is Latest"
        logger.info { message }
        loadCache()
    }

    private fun loadCache() {
        val pb = progress("Loading Cache",getCacheLocation().listFiles().size.toLong())

        library = CacheLibrary(getCacheLocation().toString(), false, object : ProgressListener {
            override fun notify(progress: Double, message: String?) {
                pb.step()
            }
        })
        pb.close()
        XteaLoader.load()
    }

    private fun needsUpdate() : Boolean {
        if(getCacheLocation().listFiles() == null) {
            return true
        }
        if(getCacheLocation().listFiles().isEmpty()) {
            return true
        }
        return properties.getProperty("cache-version-${cacheInfo.builds[0].major}") != cacheInfo.timestamp
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
        it.game.contains(Application.gameType)
        && it.timestamp != null
        && it.builds.isNotEmpty()
    }.maxByOrNull { it.timestamp.stringToTimestamp().toEchochUTC() }?: error("Unable to find Latest Revision")

    private fun findRevision(rev : Int, caches : Array<CacheInfo>) = caches.filter {
        it.game.contains(Application.gameType)
        && it.timestamp != null && it.builds.isNotEmpty() && it.builds[0].major == rev
    }.maxByOrNull { it.timestamp.stringToTimestamp().toEchochUTC() }?: error("Unable to find Revision: $revision")



}