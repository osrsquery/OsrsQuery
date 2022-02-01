package com.query.utils

import com.beust.klaxon.Klaxon
import com.displee.cache.CacheLibrary
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarStyle
import java.nio.ByteBuffer
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.experimental.and

fun CacheLibrary.index(type : IndexType) = index(type.number)

fun CacheLibrary.data(type : IndexType,archive: String, xtea: IntArray? = null) = this.data(type.number, archive, 0, xtea)

fun CacheLibrary.data(index: IndexType, archive: Int, file: Int = 0, xtea: IntArray? = null) = this.index(index).archive(archive, xtea)?.file(file)?.data

fun String.capitalizeWords(): String = split(" ").map { it.capitalize() }.joinToString(" ")

fun progress(task : String, amt : Long) : ProgressBar {
    return ProgressBar(
        task,
        amt,
        1,
        System.err,
        ProgressBarStyle.ASCII,
        "",
        1,
        false,
        null,
        ChronoUnit.SECONDS,
        0L,
        Duration.ZERO
    )
}