package com.query.game.map.builders

data class MapImageBuilder(
    var drawObjects : Boolean = true,
    var drawMapScene  : Boolean = true,
    var drawFunctions  : Boolean = true,
    var labelRegions  : Boolean = false,
    var outlineRegions  : Boolean = false,
    var walls : Boolean = true,
    var fill : Boolean = false,
    var scale : Int = 4,
    var region : Int = -1
) {
    fun objects(state: Boolean) = apply { this.drawObjects = state }
    fun mapScenes(state: Boolean) = apply { this.drawMapScene = state }
    fun functions(state: Boolean) = apply { this.drawFunctions = state }
    fun label(state: Boolean) = apply { this.labelRegions = state }
    fun scale(scale: Int) = apply { this.scale = scale }

    fun fill(state: Boolean) = apply { this.fill = state }

    fun setRegion(state: Int) = apply { this.region = state }

    fun outline(state: Boolean) = apply { this.outlineRegions = state }
    fun build() = MapImageBuilder(
        drawObjects,
        drawMapScene,
        drawFunctions,
        labelRegions,
        outlineRegions,
        walls,
        fill,
        scale
    )
}