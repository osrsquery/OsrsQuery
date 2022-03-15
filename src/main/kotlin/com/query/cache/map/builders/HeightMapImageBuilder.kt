package com.query.cache.map.builders

data class HeightMapImageBuilder(
    var viewable : Boolean = true,
    var scale : Int = 4
) {
    fun scale(scale: Int) = apply { this.scale = scale }
    fun viewable(state: Boolean) = apply { this.viewable = state }
    fun build() = HeightMapImageBuilder(viewable, scale)
}