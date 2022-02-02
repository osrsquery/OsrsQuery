package com.query

import com.google.gson.GsonBuilder
import com.query.Constants.properties
import com.query.cache.definitions.Definition
import com.query.cache.definitions.impl.*
import com.query.cache.download.CacheInfo
import com.query.cache.download.UpdateCache
import com.query.utils.TimeUtils
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import mu.KotlinLogging
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis


object Application {

    /**
     * What Revision the user wants to dump.
     */
    var revision : Int = 0

    /**
     * Main Logger for the Application.
     */
    val logger = KotlinLogging.logger {}

    /**
     * Cache Revision data for the Revision that user requests.
     */

    lateinit var cacheInfo : CacheInfo

    var gson = GsonBuilder().setPrettyPrinting().create()

    fun initialize(args : Array<String>) {
        val time = measureTimeMillis {

            val parser = ArgParser("app")
            val rev by parser.option(ArgType.Int, description = "The revision you wish to dump").default(0)
            parser.parse(args)
            revision = rev

            UpdateCache.initialize()

            //Latch is necessary.
            val latch = CountDownLatch(1)

            val commands = listOf(
                SpriteProvider(latch,false),
                AreaProvider(latch),
                EnumProvider(latch),
                HealthBarProvider(latch),
                InvProvider(latch),
                ItemProvider(latch),
                KitProvider(latch),
                NpcProvider(latch),
                ObjectProvider(latch),
                OverlayProvider(latch),
                ParamProvider(latch),
                SequenceProvider(latch),
                SpotAnimationProvider(latch),
                TextureProvider(latch),
                UnderlayProvider(latch),
                VarbitProvider(latch),
            )
            val cores = Runtime.getRuntime().availableProcessors()
            if (cores > 4) {
                val pool = Executors.newFixedThreadPool(cores)
                commands.forEach(pool::execute)
                pool.shutdown()
            } else {
                commands.forEach(Runnable::run)
            }
            latch.await()

            //Sprites().load()
            //MapFunctions().load()
            //MapScene().load()
            //Overlay().load()
            //Textures().load()
        }

        logger.info { "Dump Completed in ${TimeUtils.millsToFormat(time)}" }

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
     * Cached definitions provided from the cache library.
     */
    val definitions: ConcurrentHashMap<Class<out Definition>, List<Definition>> = ConcurrentHashMap()


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

fun main(args : Array<String>) {
    Application.initialize(args)
}