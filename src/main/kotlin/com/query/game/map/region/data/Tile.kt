package com.query.game.map.region.data

data class Tile(
    var height: Int? = null,
    var attrOpcode : Int = 0,
    var settings: Byte = 0,
    var overlayId: Short = 0,
    var overlayPath: Byte = 0,
    var overlayRotation: Byte = 0,
    var underlayId: Short = 0,
)