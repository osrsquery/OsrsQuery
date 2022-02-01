package com.query

import com.query.Constants.properties
import com.query.cache.download.UpdateCache
import mu.KotlinLogging
import java.io.File
import java.io.FileOutputStream
import java.util.*


object Application {

    private val logger = KotlinLogging.logger {}

    fun initialize() {
        UpdateCache.initialize()
    }

    fun loadProperties() {
        logger.info { "Loading properties..." }
        val file = File("./app.properties")
        if(!file.exists()) {
            file.createNewFile()
        }
        properties.load(file.inputStream())
    }

    fun saveProperties(p: Properties) {
        val path = "./app.properties"
        val fr = FileOutputStream(path)
        p.store(fr, "Properties")
        fr.close()
    }


}

fun main() {
    Application.initialize()
}