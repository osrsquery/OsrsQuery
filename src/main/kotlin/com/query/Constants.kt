package com.query

import com.displee.cache.CacheLibrary
import java.util.*

object Constants {
    const val CACHE_DOWNLOAD_LOCATION = "https://archive.openrs2.org/caches.json"
    const val BASE_DIR = "./repository/"
    var properties : Properties = Properties()
    lateinit var library : CacheLibrary
}