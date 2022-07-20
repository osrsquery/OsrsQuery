package com.query.utils

enum class ConfigType(val id: Int) {
    UNDERLAY(1),
    IDENTKIT(3),
    OVERLAY(4),
    INV(5),
    OBJECT(6),
    ENUM(8),
    NPC(9),
    ITEM(10),
    PARAMS(11),
    SEQUENCE(12),
    SPOTANIM(13),
    VARBIT(14),
    VARCLIENT(19),
    VARCLIENTSTRING(15),
    VARPLAYER(16),
    HITSPLAT(32),
    HEALTHBAR(33),
    MAP_SPRITES(34),
    MAP_AREAS(36);
}

fun containerId(containerId: Int, bit : Int): Int {
    return containerId ushr bit
}
