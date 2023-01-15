package com.query

import com.google.gson.GsonBuilder
import com.query.Constants.properties
import com.query.cache.CacheManager
import com.query.cache.definitions.impl.*
import com.query.dump.*
import com.query.game.map.HeightMapGenerator
import com.query.game.map.MapImageGenerator
import com.query.game.map.builders.HeightMapImageBuilder
import com.query.game.map.builders.MapImageBuilder
import com.query.utils.FileUtil
import com.query.utils.TimeUtils
import com.query.utils.revisionBefore
import joptsimple.ArgumentAcceptingOptionSpec
import joptsimple.OptionParser
import joptsimple.ValueConverter
import joptsimple.util.EnumConverter
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
     * What Game Type you wish to dump.
     */
    var gameType : GameType = GameType.OLDSCHOOL

    /**
     * What Game World you wish to download.
     */
    var gameWorld : Int = 0

    /**
     * How Many cores to run with
     */
    var cores : Int = 0

    /**
     * Task Type
     */
    var type : TaskType = TaskType.ALL

    /**
     * Write Types
     */
    var writeData = false

    /**
     * Base Dir to store your data
     */
    var BASE_DIR = "./repository/"

    /**
     * Main Logger for the Application.
     */
    val logger = KotlinLogging.logger {}

    var gson = GsonBuilder().setPrettyPrinting().create()

    private fun initialize() {
        val time = measureTimeMillis {

            CacheManager.initialize()

            //Latch is necessary.
            val latch = CountDownLatch(18)

            writeData = when(type) {
                TaskType.TYPES, TaskType.ALL, TaskType.DUMP317 -> true
                else -> false
            }


            val commands = listOf(
                AreaProvider(latch),
                EnumProvider(latch),
                HealthBarProvider(latch),
                InvProvider(latch),
                MusicProvider(latch),
                JingleProvider(latch),
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
            SpriteProvider(latch).run()

            val availableCores = Runtime.getRuntime().availableProcessors()

            if (availableCores > cores) {
                val pool = Executors.newFixedThreadPool(cores)
                commands.forEach(pool::execute)
                pool.shutdown()
            } else {
                commands.forEach(Runnable::run)
            }
            latch.await()

            when(type) {

                TaskType.ALL -> {
                    SpriteDumper().init()
                    MapSceneDumper().init()
                    OverlayImages().init()
                    Textures().init()
                    Dump317.init()

                    //ModelOrganization.init()
                    dumpMapImages(true)
                    dumpMapImages(false)

                }

                TaskType.MODELS_SORTED -> ModelOrganization.init()

                TaskType.DUMP317 -> {
                    SpriteDumper().init()
                    MapSceneDumper().init()
                    OverlayImages().init()
                    Textures().init()
                    Dump317.init()
                }

                TaskType.SPRITES -> SpriteDumper().init()
                TaskType.HEIGHT_MAPS -> dumpMapImages(true)
                TaskType.MAP_IMAGES -> dumpMapImages(false)
                TaskType.MAP_SCENE -> MapSceneDumper().init()
                TaskType.TEXTURE_IMAGES -> Textures().init()
                TaskType.MAP_FUNCTIONS -> MapFunctionsDumper.init()
                TaskType.UNUSED_MODELS -> UnusedModels.init()
            }


        }

        logger.info { "Dump Completed in ${TimeUtils.millsToFormat(time)}" }

    }

    private fun dumpMapImages(heightMap : Boolean) {
        when(heightMap) {
            true -> {
                val dumper = HeightMapGenerator(HeightMapImageBuilder().scale(4).viewable(true).build())
                val timer = measureTimeMillis {
                    dumper.drawHeightMap()
                }
                logger.info { "Map Images Written in ${TimeUtils.millsToFormat(timer)}" }
            }
            false -> {
                val dumper = MapImageGenerator(MapImageBuilder().scale(4).build())

                val timer = measureTimeMillis {
                    dumper.draw()
                }

                logger.info { "Map Images Written in ${TimeUtils.millsToFormat(timer)}" }
            }
        }
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
    val definitions: ConcurrentHashMap<Class<out com.query.cache.definitions.Definition>, List<com.query.cache.definitions.Definition>> = ConcurrentHashMap()


    /**
     * Prompts the application console with performance numbers.
     */
    fun prompt(command: Class<out Runnable>, start: Long) {
        //logger.debug { String.format("%s took %sms to cache.", command.simpleName, System.currentTimeMillis() - start) }
    }

    /**
     * Stores a provided list of definitions.
     */
    fun store(clazz: Class<out com.query.cache.definitions.Definition>, list: List<com.query.cache.definitions.Definition>) {
        definitions[clazz] = list
    }

    /**
     * Gets the textures definitions.
     */
    fun textures(): List<TextureDefinition> {
        return definitions[TextureDefinition::class.java]?.filterIsInstance<TextureDefinition>()?: error("Texture Definitions not loaded.")
    }

    /**
     * Gets the sprites definitions.
     */
    fun sprites(): List<SpriteDefinition> {
        return definitions[SpriteDefinition::class.java]?.filterIsInstance<SpriteDefinition>()?: error("Sprite Definitions not loaded.")
    }

    /**
     * Gets the objects definitions.
     */
    fun objects(): List<ObjectDefinition> {
        return definitions[ObjectDefinition::class.java]?.filterIsInstance<ObjectDefinition>()?: error("Object Definitions not loaded.")
    }

    /**
     * Gets the areas definitions.
     */
    fun areas(): List<AreaDefinition> {
        return definitions[AreaDefinition::class.java]?.filterIsInstance<AreaDefinition>()?: error("Area Definitions not loaded.")
    }

    /**
     * Gets the enum definitions.
     */
    fun enums(): List<EnumDefinition> {
        return definitions[EnumDefinition::class.java]?.filterIsInstance<EnumDefinition>()?: error("Enum Definitions not loaded.")
    }

    /**
     * Gets the health definitions.
     */
    fun healths(): List<HealthBarDefinition>? {
        return definitions[HealthBarDefinition::class.java]?.filterIsInstance<HealthBarDefinition>()?: error("Health Bar Definitions not loaded.")
    }

    /**
     * Gets the inventory definitions.
     */
    fun invs(): List<InvDefinition>? {
        return definitions[InvDefinition::class.java]?.filterIsInstance<InvDefinition>()?: error("Inv Definitions not loaded.")
    }

    /**
     * Gets the item definitions.
     */
    fun items(): List<ItemDefinition> {
        return definitions[ItemDefinition::class.java]?.filterIsInstance<ItemDefinition>()?: error("Item Definitions not loaded.")
    }

    /**
     * Gets the kits definitions.
     */
    fun kits(): List<KitDefinition> {
        return definitions[KitDefinition::class.java]?.filterIsInstance<KitDefinition>()?: error("Kit Definitions not loaded.")
    }

    /**
     * Gets the npc definitions.
     */
    fun npcs(): List<NpcDefinition> {
        return definitions[NpcDefinition::class.java]?.filterIsInstance<NpcDefinition>()?: error("Npc Definitions not loaded.")
    }

    /**
     * Gets the param definitions.
     */
    fun params(): List<ParamDefinition> {
        return definitions[ParamDefinition::class.java]?.filterIsInstance<ParamDefinition>()?: error("Param Definitions not loaded.")
    }

    /**
     * Gets the overlay definitions.
     */
    fun overlays(): List<OverlayDefinition> {
        return definitions[OverlayDefinition::class.java]?.filterIsInstance<OverlayDefinition>()?: error("Overlay Definitions not loaded.")
    }

    /**
     * Gets the sequences definitions.
     */
    fun sequences(): List<SequenceDefinition> {
        return definitions[SequenceDefinition::class.java]?.filterIsInstance<SequenceDefinition>()?: error("Sequence Definitions not loaded.")
    }

    /**
     * Gets the spotAnim definitions.
     */
    fun spotanimations(): List<SpotAnimationDefinition> {
        return definitions[SpotAnimationDefinition::class.java]?.filterIsInstance<SpotAnimationDefinition>()?: error("Spot Animation Definitions not loaded.")
    }

    /**
     * Gets the underlay definitions.
     */
    fun underlays(): List<UnderlayDefinition> {
        return definitions[UnderlayDefinition::class.java]?.filterIsInstance<UnderlayDefinition>()?: error("Underlay Definitions not loaded.")
    }

    /**
     * Gets the varbit definitions.
     */
    fun varbits(): List<VarbitDefinition> {
        return definitions[VarbitDefinition::class.java]?.filterIsInstance<VarbitDefinition>()?: error("Varbit Definitions not loaded.")
    }

    /**
     * Gets Music tracks.
     */
    fun music(): List<MusicDefinition> {
        return definitions[MusicDefinition::class.java]?.filterIsInstance<MusicDefinition>()?: error("Music Definitions not loaded.")
    }

    /**
     * Gets Jingle tracks.
     */
    fun jingle(): List<JingleDefinition> {
        return definitions[JingleDefinition::class.java]?.filterIsInstance<JingleDefinition>()?: error("Jingle Definitions not loaded.")
    }

    @JvmStatic
    fun main(args : Array<String>) {
        val parser = OptionParser(false)

        val rev: ArgumentAcceptingOptionSpec<Int> = parser
            .accepts("revision", "The revision you wish to dump")
            .withRequiredArg()
            .ofType(Int::class.java)
        .defaultsTo(-1)

        val game: ArgumentAcceptingOptionSpec<GameType> = parser
            .accepts("game", "Select a Game type you wish to download")
            .withRequiredArg()
            .ofType(GameType::class.java)
            .defaultsTo(GameType.OLDSCHOOL)
            .withValuesConvertedBy(object : EnumConverter<GameType>(GameType::class.java), ValueConverter<GameType> {
                override fun convert(v: String): GameType {
                    return super.convert(v.lowercase())
                }
            }
        )

        val world: ArgumentAcceptingOptionSpec<Int> = parser
            .accepts("world", "Select the the world cache you wish to download live useful for beta caches")
            .withRequiredArg()
            .ofType(Int::class.java)
        .defaultsTo(0)

        val storeDir: ArgumentAcceptingOptionSpec<String> = parser
            .accepts("storeDir", "Select the location to store files")
            .withRequiredArg()
            .ofType(String::class.java)
        .defaultsTo("./repository/")

        val useCores: ArgumentAcceptingOptionSpec<Int> = parser
            .accepts("cores", "Select the amount of cores to use")
            .withRequiredArg()
            .ofType(Int::class.java)
        .defaultsTo(4)

        var task: ArgumentAcceptingOptionSpec<TaskType> = parser
            .accepts("task", "Select the task you wish to run")
            .withRequiredArg()
            .ofType(TaskType::class.java)
            .defaultsTo(TaskType.ALL)
            .withValuesConvertedBy(object : EnumConverter<TaskType>(TaskType::class.java), ValueConverter<TaskType> {
                override fun convert(v: String): TaskType {
                    return super.convert(v.lowercase())
                }
            }
        )


        val options = parser.parse(*args)

        gameWorld = options.valueOf(world)
        gameType = options.valueOf(game)
        revision = options.valueOf(rev)
        BASE_DIR = options.valueOf(storeDir)
        cores = options.valueOf(useCores)
        type = options.valueOf(task)
        initialize()
    }


}
