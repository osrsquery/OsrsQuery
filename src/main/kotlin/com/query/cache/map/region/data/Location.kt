package com.query.cache.map.region.data

import com.query.utils.Position

data class Location(
    val id : Int,
    val type : Int,
    val orientation : Int,
    val position: Position
)