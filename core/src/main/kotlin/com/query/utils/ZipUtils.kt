package com.query.utils

import mu.KotlinLogging
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import java.io.File

object ZipUtils {

    private val logger = KotlinLogging.logger {}

     fun unZip() {
        val path = File(FileUtil.getBase(),"cache.zip")
        val zipFile = ZipFile(path)
        try {
            logger.info { "Unzipping Cache please wait" }
            zipFile.extractAll(FileUtil.getBase().toString())
        } catch (e: ZipException) {
            logger.error { "Unable extract files from $path : $e" }
        }

    }


}