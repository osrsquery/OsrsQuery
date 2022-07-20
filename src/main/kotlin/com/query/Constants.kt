package com.query

import com.displee.cache.CacheLibrary
import com.runetopic.cache.store.Js5Store
import com.squareup.okhttp.OkHttpClient
import java.util.*

object Constants {
    const val CACHE_DOWNLOAD_LOCATION = "https://archive.openrs2.org/caches.json"
    const val BASE_DIR = "./repository/"
    var properties : Properties = Properties()
    lateinit var library : CacheLibrary
    var store : Js5Store? = null
    /**
     * The client for OkHttp.
     */
    val client = OkHttpClient()

    const val OSRS_WIKI = "https://oldschool.runescape.wiki/w/"

}