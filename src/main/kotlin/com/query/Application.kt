package com.query

import com.google.gson.GsonBuilder
import com.query.Constants.properties
import com.query.cache.definitions.Definition
import com.query.cache.definitions.provider.AreaDefinition
import com.query.cache.definitions.provider.ObjectDefinition
import com.query.cache.definitions.provider.SpriteDefinition
import com.query.cache.definitions.provider.TextureDefinition
import com.query.cache.download.UpdateCache
import mu.KotlinLogging
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.ConcurrentHashMap


object Application {

    var gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * Cached definitions provided from the cache library.
     */
    val definitions: ConcurrentHashMap<Class<out Definition>, List<Definition>> = ConcurrentHashMap()

    val logger = KotlinLogging.logger {}

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

    /**
     * Prompts the application console with performance numbers.
     */
    fun prompt(command: Class<out Runnable>, start: Long) {
        logger.debug { String.format("%s took %sms to cache.", command.simpleName, System.currentTimeMillis() - start) }
    }

    /**
     * Gets the textures definitions.
     */
    fun textures(): List<TextureDefinition>? {
        return definitions[TextureDefinition::class.java]?.filterIsInstance<TextureDefinition>()
    }

    /**
     * Gets the sprites definitions.
     */
    fun sprites(): List<SpriteDefinition>? {
        return definitions[SpriteDefinition::class.java]?.filterIsInstance<SpriteDefinition>()
    }

    /**
     * Gets the objects definitions.
     */
    fun objects(): List<ObjectDefinition>? {
        return definitions[ObjectDefinition::class.java]?.filterIsInstance<ObjectDefinition>()
    }

    /**
     * Gets the areas definitions.
     */
    fun areas(): List<AreaDefinition>? {
        return definitions[AreaDefinition::class.java]?.filterIsInstance<AreaDefinition>()
    }


    /**
     * Stores a provided list of definitions.
     */
    fun store(clazz: Class<out Definition>, list: List<Definition>) {
        definitions[clazz] = list
    }


}

fun main() {
    Application.initialize()
}