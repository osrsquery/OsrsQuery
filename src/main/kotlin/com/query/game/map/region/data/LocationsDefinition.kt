package com.query.game.map.region.data

data class LocationsDefinition(
    var regionX : Int,
    var regionY : Int,
    val locations: MutableList<Location>
)