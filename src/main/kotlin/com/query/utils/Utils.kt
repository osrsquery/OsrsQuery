package com.query.utils

import com.beust.klaxon.JsonBase
import com.beust.klaxon.Klaxon
import com.beust.klaxon.Parser
import com.displee.cache.CacheLibrary
import com.query.Application
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarStyle
import java.io.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.zip.GZIPOutputStream

fun Klaxon.toPrettyJsonString(value: Any): String {
    val builder = StringBuilder(Klaxon().toJsonString(value))
    return (Parser().parse(builder) as JsonBase).toJsonString(true)
}

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


fun gzip(output : File,uncompressedData: ByteArray) {
    try {
        ByteArrayOutputStream(uncompressedData.size).use { bos ->
            GZIPOutputStream(output.outputStream()).use { gzipOS ->
                gzipOS.write(uncompressedData)
                gzipOS.close()
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun isObjectInteger(o: Any?): Boolean {
    return o is Int
}

fun getRegion(x : Int, y : Int) = x shl 8 or y

fun gzip(content: String): ByteArray {
    val bos = ByteArrayOutputStream()
    GZIPOutputStream(bos).bufferedWriter().use { it.write(content) }
    return bos.toByteArray()
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

fun progress(task : String, amt : Int) : ProgressBar {
    return ProgressBar(
        task,
        amt.toLong(),
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
