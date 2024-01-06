package com.query.openrs2

import com.google.gson.Gson
import com.query.ApplicationSettings
import com.query.utils.stringToTimestamp
import com.query.utils.toEchochUTC
import org.jsoup.Jsoup
import java.io.File
import kotlin.system.exitProcess

object CacheManager {

    private val onlineCaches: MutableList<CacheInfo> = mutableListOf()
    val localCaches: MutableList<CacheInfo> = mutableListOf()

    fun init() {
        val cachesTemp = getCacheInfo()
        if (cachesTemp != null) {
            onlineCaches.addAll(cachesTemp.filter { c ->
                c.language == "en" &&
                        (c.game == "runescape" || c.game == "oldschool") &&
                        c.builds.any { it.major != -1 }
            })

            onlineCaches.addAll(
                onlineCaches.filter { it.timestamp != null }
                    .groupBy { Pair(it.getRev(), it.game) }
                    .values
                    .map { group -> group.maxByOrNull { it.timestamp.stringToTimestamp().toEchochUTC() }!! }
            )

            ApplicationSettings.settings.installed
                .filter { File(it).exists() }
                .forEach { path ->
                    localCaches.add(Gson().fromJson(File(path, "info.json").readText(), CacheInfo::class.java))
                }

        } else {
            exitProcess(0)
        }
    }

    fun getOldSchoolCaches(): List<CacheInfo> {
        return onlineCaches.filter { it.game == "oldschool" }.reversed()
    }

    fun getRS2(): List<CacheInfo> {
        return onlineCaches.filter { it.game == "runescape" }.reversed()
    }

    fun getInstalled(): List<CacheInfo> {
        return localCaches
    }

    fun getAll(): List<CacheInfo> {
        return onlineCaches
    }

    private fun getCacheInfo(): Array<CacheInfo>? {
        val content = Jsoup.connect("https://archive.openrs2.org/caches.json")
            .ignoreContentType(true)
            .get()
            .body()
            .ownText()
        return Gson().fromJson(content, Array<CacheInfo>::class.java)
    }
}