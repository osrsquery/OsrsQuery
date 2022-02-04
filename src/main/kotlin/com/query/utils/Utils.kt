package com.query.utils

import com.displee.cache.CacheLibrary
import com.query.Application
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarStyle
import org.apache.commons.lang.StringUtils
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun CacheLibrary.index(type : IndexType) = index(type.number)

fun CacheLibrary.data(type : IndexType,archive: String, xtea: IntArray? = null) = this.data(type.number, archive, 0, xtea)

fun CacheLibrary.data(index: IndexType, archive: Int, file: Int = 0, xtea: IntArray? = null) = this.index(index).archive(archive, xtea)?.file(file)?.data

fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

fun String.stringToTimestamp() : LocalDateTime {
    val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
    val time = this.replace("T"," ").replaceAfterLast(".","")
    val dateTime = LocalDateTime.parse(time.replaceLastLetter(""), formatter)
    return dateTime
}

fun LocalDateTime.toEchochUTC() : Long {
    return this.toEpochSecond(ZoneOffset.UTC)
}

fun revisionID() = Application.cacheInfo.builds[0].major

fun revisionAfter(rev : Int) = rev <= revisionID()
fun revisionBefore(rev : Int) = rev >= revisionID()

fun String.replaceLastLetter(newLetter: String): String {
    val substring = this.substring(0, this.length - 1)
    return substring + newLetter
}


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

fun writeJson(file : File, data : Any, prettyPrint : Boolean = true) {
    var output = BufferedWriter(FileWriter(file))
    output.write(data.jsonToString(prettyPrint))
    output.close()
}
