package com.query.utils

import java.io.File

object TrackData {

    val musicNames = mapOf(
        0 to ""
    )

}

fun main() {
    File("C:\\Users\\Shadow\\Desktop\\track1_by_file\\").listFiles().forEach {
        val name = it.nameWithoutExtension.substring(4)
        println("0 to $name ,")
    }
}