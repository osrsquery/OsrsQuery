package com.query.utils

import com.query.cache.download.UpdateCache
import mu.KotlinLogging
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException

object ZipUtils {

    private val logger = KotlinLogging.logger {}

     fun unZip() {
        val path = FileUtils.getFile("cache/osrs/","cache.zip")
        val zipFile = ZipFile(path)
        try {
            logger.info { "Unzipping Cache please wait" }
            zipFile.extractAll(FileUtils.getDir("cache/osrs/").toString())
        } catch (e: ZipException) {
            logger.error { "Unable extract files from $path : $e" }
        }

    }


}