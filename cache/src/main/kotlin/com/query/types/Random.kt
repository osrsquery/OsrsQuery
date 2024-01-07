package com.query.types

import kotlin.random.Random

var random: Random = Random
    private set

fun setRandom(rand: Random) {
    random = rand
}