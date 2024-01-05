package com.query.openrs2

import com.beust.klaxon.Klaxon
import com.google.gson.Gson
import com.query.utils.stringToTimestamp
import org.jsoup.Jsoup
import kotlin.system.exitProcess

object OpenRS2Manager {

    private var caches : MutableList<CacheInfo> = emptyList<CacheInfo>().toMutableList()


    fun init() {
        val cachesTemp = getCacheInfo()
        if (cachesTemp != null) {
            caches = cachesTemp.filter { c ->
                c.language == "en" &&
                        (c.game == "runescape" || c.game == "oldschool") &&
                        c.builds.any { it.major != -1 }
            }.toMutableList()

            caches.sortBy { it.builds.first { it.major != -1 }.major }

            getRS2().forEach {
                println(it)
            }

        } else {
            exitProcess(0)
        }

    }

    fun getOldSchoolCaches(): List<CacheInfo> {
        return caches.filter { it.game == "oldschool" }
    }

    fun getRS2(): List<CacheInfo> {
        return caches.filter { it.game == "runescape" }
    }

    fun getAll(): List<CacheInfo> {
        return caches
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