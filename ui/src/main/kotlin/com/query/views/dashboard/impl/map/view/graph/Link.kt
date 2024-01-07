package world.gregs.voidps.tools.map.view.graph

import com.fasterxml.jackson.annotation.JsonProperty
import com.query.types.Tile

data class Link(
    @get:JsonProperty("start")
    var start: Tile,
    @get:JsonProperty("end")
    var end: Tile,
    var actions: List<String>? = null,
    var requirements: List<String>? = null,
    var cost: Int = -1
)