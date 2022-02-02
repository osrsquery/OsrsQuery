package com.query

import com.google.gson.GsonBuilder
import com.query.Constants.properties
import com.query.cache.definitions.Definition
import com.query.cache.definitions.loader.*
import com.query.cache.definitions.provider.*
import com.query.cache.download.UpdateCache
import com.query.dump.impl.*
import com.query.utils.TimeUtils
import mu.KotlinLogging
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis


object Application {

    var gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * Cached definitions provided from the cache library.
     */
    val definitions: ConcurrentHashMap<Class<out Definition>, List<Definition>> = ConcurrentHashMap()

    val logger = KotlinLogging.logger {}

    fun initialize() {
        val time = measureTimeMillis {
            UpdateCache.initialize()

            //Latch is necessary.
            val latch = CountDownLatch(15)
            val commands = listOf(
                SpriteLoader(latch),
                AreaLoader(latch),
                EnumLoader(latch),
                HealthBarLoader(latch),
                InvLoader(latch),
                ItemLoader(latch),
                KitLoader(latch),
                NpcLoader(latch),
                ObjectLoader(latch),
                OverlayLoader(latch),
                ParamLoader(latch),
                SequenceLoader(latch),
                SpotAnimationLoader(latch),
                TextureLoader(latch),
                UnderlayLoader(latch),
                VarbitLoader(latch),
            )
            val cores = Runtime.getRuntime().availableProcessors()
            if (cores > 4) {
                val pool = Executors.newFixedThreadPool(cores)
                commands.forEach(pool::execute)
                pool.shutdown()
            } else {
                commands.forEach(Runnable::run)
            }

            MapFunctions().load()
            MapScene().load()
            Overlay().load()
            Sprites().load()
            Textures().load()
        }

        logger.info { "Dump completed in ${TimeUtils.millsToFormat(time)}" }

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
        //logger.debug { String.format("%s took %sms to cache.", command.simpleName, System.currentTimeMillis() - start) }
    }

    /**
     * Stores a provided list of definitions.
     */
    fun store(clazz: Class<out Definition>, list: List<Definition>) {
        definitions[clazz] = list
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
     * Gets the enum definitions.
     */
    fun enums(): List<EnumDefinition>? {
        return definitions[EnumDefinition::class.java]?.filterIsInstance<EnumDefinition>()
    }

    /**
     * Gets the health definitions.
     */
    fun healths(): List<HealthBarDefinition>? {
        return definitions[HealthBarDefinition::class.java]?.filterIsInstance<HealthBarDefinition>()
    }

    /**
     * Gets the inventory definitions.
     */
    fun invs(): List<InvDefinition>? {
        return definitions[InvDefinition::class.java]?.filterIsInstance<InvDefinition>()
    }

    /**
     * Gets the item definitions.
     */
    fun items(): List<ItemDefinition>? {
        return definitions[ItemDefinition::class.java]?.filterIsInstance<ItemDefinition>()
    }

    /**
     * Gets the kits definitions.
     */
    fun kits(): List<KitDefinition>? {
        return definitions[KitDefinition::class.java]?.filterIsInstance<KitDefinition>()
    }

    /**
     * Gets the npc definitions.
     */
    fun npcs(): List<NpcDefinition>? {
        return definitions[NpcDefinition::class.java]?.filterIsInstance<NpcDefinition>()
    }

    /**
     * Gets the param definitions.
     */
    fun params(): List<ParamDefinition>? {
        return definitions[ParamDefinition::class.java]?.filterIsInstance<ParamDefinition>()
    }

    /**
     * Gets the overlay definitions.
     */
    fun overlays(): List<OverlayDefinition>? {
        return definitions[OverlayDefinition::class.java]?.filterIsInstance<OverlayDefinition>()
    }

    /**
     * Gets the sequences definitions.
     */
    fun sequences(): List<SequenceDefinition>? {
        return definitions[SequenceDefinition::class.java]?.filterIsInstance<SequenceDefinition>()
    }

    /**
     * Gets the spotAnim definitions.
     */
    fun spotanimations(): List<SpotAnimationDefinition>? {
        return definitions[SpotAnimationDefinition::class.java]?.filterIsInstance<SpotAnimationDefinition>()
    }

    /**
     * Gets the underlay definitions.
     */
    fun underlays(): List<UnderlayDefinition>? {
        return definitions[UnderlayDefinition::class.java]?.filterIsInstance<UnderlayDefinition>()
    }

    /**
     * Gets the varbit definitions.
     */
    fun varbits(): List<VarbitDefinition>? {
        return definitions[VarbitDefinition::class.java]?.filterIsInstance<VarbitDefinition>()
    }


}

fun main() {
    Application.initialize()
}