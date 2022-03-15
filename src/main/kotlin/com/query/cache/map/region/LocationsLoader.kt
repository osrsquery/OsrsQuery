package com.query.cache.map.region

import InputStream
import com.query.cache.map.region.data.Location
import com.query.cache.map.region.data.LocationsDefinition
import com.query.utils.Position

class LocationsLoader {

    fun load(regionX: Int, regionY: Int, b: ByteArray): LocationsDefinition {
        val loc = LocationsDefinition(regionX,regionY, emptyList<Location>().toMutableList())

        loadLocations(loc, b)
        return loc
    }


    private fun loadLocations(locations: LocationsDefinition, data: ByteArray) {
        val buffer = InputStream(data)
        var id = -1
        var idOffset: Int
        while (buffer.readUnsignedIntSmartShortCompat().also { idOffset = it } != 0) {
            id += idOffset
            var position = 0
            var positionOffset: Int
            while (buffer.readUnsignedShortSmart().also { positionOffset = it } != 0) {
                position += positionOffset - 1
                val localY = position and 0x3F
                val localX = position shr 6 and 0x3F
                val height = position shr 12 and 0x3
                val attributes: Int = buffer.readUnsignedByte()
                val type = attributes shr 2
                val orientation = attributes and 0x3
                locations.locations.add(Location(id, type, orientation, Position(localX, localY, height)))
            }
        }
    }
}