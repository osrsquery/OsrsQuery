package com.query.utils

class Position {

    var x : Int = 0
    var y : Int = 0
    var z : Int = 0

    constructor(x : Int, y : Int, z : Int) {
        this.x = x
        this.y = y
        this.z = z
    }

    constructor(x : Int, y : Int) {
        this.x = x
        this.y = y
    }

    override fun toString(): String {
        return "Position{x=$x, y=$y, z=$z}"
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 67 * hash + x
        hash = 67 * hash + y
        hash = 67 * hash + z
        return hash
    }


}
