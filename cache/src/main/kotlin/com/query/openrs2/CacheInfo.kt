package com.query.openrs2

data class CacheInfo(
    val id : Int,
    val game : String,
    val timestamp : String,
    val builds : List<CacheInfoBuilds>,
    val sources : List<String>,
    val language: String,
    val size : Long
)