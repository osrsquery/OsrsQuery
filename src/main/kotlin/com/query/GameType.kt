package com.query

enum class GameType(online : Boolean = true) {
    OLDSCHOOL,
    RUNESCAPE;

    fun getName() = this.name.lowercase()
}