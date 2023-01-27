package com.query.dump

import com.query.Application
import com.query.Application.areas
import com.query.Application.healths
import com.query.Application.items
import com.query.Application.kits
import com.query.Application.npcs
import com.query.Application.objects
import com.query.Application.overlays
import com.query.Application.sequences
import com.query.Application.spotanimations
import com.query.Application.textures
import com.query.Application.underlays
import com.query.Application.varbits
import com.query.cache.CacheManager
import com.query.cache.definitions.impl.*
import com.query.game.map.MapImageGenerator
import com.query.game.map.builders.MapImageBuilder
import com.query.utils.FileUtil
import com.query.utils.TimeUtils
import com.query.utils.progress
import com.query.utils.revisionBefore
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

enum class DataType {
    DAT,
    BOTH
}

data class DumpInfo(val dataType: DataType = DataType.DAT, val fileName : String = "", val name : String)

private val logger = KotlinLogging.logger {}

object Dump317 {

    fun init() {

        val mapFunctionDir = File(FileUtil.getBase(),"/mapFunctions/")
        FileUtils.deleteDirectory(mapFunctionDir)
        mapFunctionDir.mkdir()

        val mapFunctionDir1 = File(FileUtil.getBase(),"/dump317/mapFunctions/")
        FileUtils.deleteDirectory(mapFunctionDir1)
        mapFunctionDir1.mkdir()

        TextureDumper.init()

        ModelDumper.init()
        MapDumper.init()
        writeConfigs()

        val progress = progress("Copying Existing Stuff", 3)

        FileUtils.copyDirectory(FileUtil.getDir("mapScenes/"),FileUtil.getDir("dump317/mapScenes/"))
        progress.step()
        FileUtils.copyDirectory(FileUtil.getDir("mapFunctions/"),FileUtil.getDir("dump317/mapFunctions/"))
        progress.step()
        FileUtils.copyDirectory(FileUtil.getDir("sprites/"),FileUtil.getDir("dump317/sprites/"))
        progress.step()

        progress.close()

       /* val map = MapImageBuilder().
            outline(true).
            label(true).
            functions(true).
            mapScenes(true).
            objects(true).
            fill(false).
            scale(4)
         .build()

        val dumper = MapImageGenerator(map,FileUtil.getDir("dump317/"))
        dumper.objects = objects().associateBy { it.id }
        dumper.overlays = overlays().associateBy { it.id }
        dumper.underlays = underlays().associateBy { it.id }
        if(!revisionBefore(142)) {
            dumper.areas = areas().associateBy { it.id }
        }

        dumper.textures = textures().associateBy { it.id }
        dumper.sprites = Application.sprites().associateBy { it.id }

        val timer = measureTimeMillis {
            dumper.draw()
        }
        logger.info { "Map Images Written in ${TimeUtils.millsToFormat(timer)}" }*/

    }

    private fun writeConfigs() {


        val providers = mapOf(
            items() to DumpInfo(DataType.BOTH, "obj","Item"),
            varbits() to DumpInfo(DataType.DAT, "varbit","Varbit"),
            areas() to DumpInfo(DataType.BOTH, "areas","Area"),
            kits() to DumpInfo(DataType.DAT, "idk","Identity Kit"),
            sequences() to DumpInfo(DataType.DAT, "seq","Sequence"),
            spotanimations() to DumpInfo(DataType.DAT, "spotanim","Spot Animation"),
            healths() to DumpInfo(DataType.DAT, "healths","Healths Bars"),
            npcs() to DumpInfo(DataType.BOTH, "npc","Npc"),
            objects() to DumpInfo(DataType.BOTH, "loc","Object"),
            textures() to DumpInfo(DataType.DAT, "textures","Texture")

        )

        providers.filterNot { it.value.dataType == DataType.BOTH }.forEach {

            val defs = it.key
            val settings = it.value
            val progress = progress("Dumping ${settings.name} Definitions", defs.size)

            val dat = DataOutputStream(FileOutputStream(FileUtil.getFile("dump317/configs/","${settings.fileName}.dat")))

            dat.writeShort(defs.size)

            defs.forEach { def ->
                def.encode(dat)
                progress.step()
            }

            dat.close()
            progress.close()
        }

        providers.filter { it.value.dataType == DataType.BOTH }.forEach {

            val defs = it.key
            val settings = it.value
            val progress = progress("Dumping ${settings.name} Definitions", defs.size)

            val dat = DataOutputStream(FileOutputStream(FileUtil.getFile("dump317/configs/","${settings.fileName}.dat")))
            val idx = DataOutputStream(FileOutputStream(FileUtil.getFile("dump317/configs/","${settings.fileName}.idx")))

            idx.writeShort(defs.size)

            dat.writeShort(defs.size)

            defs.forEach { def ->
                val start = dat.size()
                def.encode(dat)
                val end = dat.size()
                idx.writeShort(end - start)
                progress.step()
            }

            dat.close()
            idx.close()
            progress.close()
        }

        val progressFloors = progress("Writing Floors", underlays().size + overlays().size)
        val floorDat = DataOutputStream(FileOutputStream(FileUtil.getFile("dump317/configs/","flo.dat")))

        floorDat.writeShort(underlays().size)

        underlays().forEach {
            it.encode(floorDat)
            progressFloors.step()
        }

        floorDat.writeShort(overlays().size)

        overlays().forEach {
            it.encode(floorDat)
            progressFloors.step()
        }

        progressFloors.close()

        /*val progress = progress("Encoding 317", 9);

        val dos = DataOutputStream(FileOutputStream(FileUtil.getFile("dump317/configs/", "loc.dat")))
        val idx = DataOutputStream(FileOutputStream(FileUtil.getFile("dump317/configs/", "loc.idx")))

        dos.writeShort(objects().size)
        idx.writeShort(objects().size)

        for (i in 0 until objects().size) {
            val def = objects()[i]
            val start = dos.size()
            def.encode(dos)
            val end = dos.size()
            idx.writeShort(end - start)
        }

        dos.close()
        idx.close()
        progress.step()*/



    }

}

fun main() {
    Application.revision = 210
    CacheManager.initialize()

    val latch = CountDownLatch(18)

    Application.writeData = false


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

    if (availableCores > 4) {
        val pool = Executors.newFixedThreadPool(4)
        commands.forEach(pool::execute)
        pool.shutdown()
    } else {
        commands.forEach(Runnable::run)
    }
    latch.await()

    SpriteDumper().init()
    MapSceneDumper().init()
    OverlayImages().init()
    Textures().init()
    Dump317.init()
}
