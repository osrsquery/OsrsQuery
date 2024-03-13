package com.query.cache

import com.displee.cache.CacheLibrary
import com.displee.cache.ProgressListener
import com.google.gson.Gson
import com.query.Application
import com.query.Application.gameType
import com.query.Application.gameWorld
import com.query.Application.loadProperties
import com.query.Application.revision
import com.query.Application.saveProperties
import com.query.Constants.CACHE_DOWNLOAD_LOCATION
import com.query.Constants.library
import com.query.Constants.properties
import com.query.GameType
import com.query.cache.downloader.DownloadOSRS
import com.query.cache.downloader.DownloadOpenRS2
import com.query.utils.*
import com.query.utils.FileUtil.getCacheLocation
import mu.KotlinLogging
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

object CacheManager {

    private val logger = KotlinLogging.logger {}


    fun findAllCaches(): Array<CacheInfo> {
        val caches = Gson().fromJson(URL(CACHE_DOWNLOAD_LOCATION).readText(), Array<CacheInfo>::class.java)
        return caches
    }

    fun initialize(revision : Int = 220) {

        var updateAvailable = true
        loadProperties()

        Application.revision = 220



        val time = measureTimeMillis {
            System.out.println("Rev: ${Application.revision}")
            logger.info { "Looking for cache Updates" }

            val caches = Gson().fromJson(URL(CACHE_DOWNLOAD_LOCATION).readText(), Array<CacheInfo>::class.java)
            val cacheInfo = if(Application.revision == -1) getLatest(caches) else findRevision(Application.revision,caches)

            if (gameWorld != 0) {
                if(gameType == GameType.OLDSCHOOL) {
                    DownloadOSRS.init()
                }
            } else {
                updateAvailable = updateAvailable(cacheInfo.timestamp)

                if(updateAvailable) {
                    DownloadOpenRS2.downloadCache(cacheInfo)
                    ZipUtils.unZip()
                    saveXteas(cacheInfo.id)
                    properties.setProperty("${gameType.getName()}-cache-version-${revision}", cacheInfo.timestamp)
                    saveProperties(properties)
                    File(FileUtil.getBase(),"/cache.zip").delete()
                }
            }
        }

        val message = if(updateAvailable) "Cache Downloaded in ${TimeUtils.millsToFormat(time)}" else "Cache is Latest"
        logger.info { message }
        loadCache()

    }

    private fun loadCache() {
        //val pb = progress("Loading Cache",getCacheLocation().listFiles().size.toLong())

        library = CacheLibrary(getCacheLocation().toString(), false, object : ProgressListener {
            override fun notify(progress: Double, message: String?) {
                //pb.step()
            }
        })
        //pb.close()
        XteaLoader.load()
    }

    private fun updateAvailable(timestamp: String) : Boolean {
        if(getCacheLocation().listFiles() == null) {
            return true
        }
        if(getCacheLocation().listFiles()?.isEmpty()!!) {
            return true
        }
        return properties.getProperty("${gameType.getName()}-cache-version-${revision}") != timestamp
    }

    public fun saveXteas(id : Int) {
        //logger.info { "Saving Xteas" }
        val file = File(FileUtil.getBase(),"xteas.json")
        try {
            val url = "https://archive.openrs2.org/caches/${id}/keys.json"
            val output = BufferedWriter(FileWriter(file))
            output.write(Gson().fromJson(URL(url).readText(), Array<Xteas>::class.java).jsonToString(true))
            output.close()
        }catch (e : Exception) {
            //logger.error { "Unable to write Xteas $e" }
        }
    }

    fun getLatest(caches : Array<CacheInfo>) = caches.filter {
        it.game.contains(gameType.name.lowercase())
        && it.timestamp != null
        && it.builds.isNotEmpty()
    }.maxByOrNull { it.timestamp.stringToTimestamp().toEchochUTC() }?: error("Unable to find Latest Revision")

    private fun findRevision(rev : Int, caches : Array<CacheInfo>) = caches.filter {
        it.game.contains(gameType.name.lowercase())
        && it.timestamp != null && it.builds.isNotEmpty() && it.builds[0].major == rev
    }.maxByOrNull { it.timestamp.stringToTimestamp().toEchochUTC() }?: error("Unable to find Revision: $revision")



}