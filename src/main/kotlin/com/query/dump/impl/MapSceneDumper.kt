package com.query.dump.impl

import com.query.cache.Sprite
import com.query.Application
import com.query.Application.objects
import com.query.Constants.library
import com.query.dump.DefinitionsTypes
import com.query.dump.TypeManager
import com.query.dump.impl.SpriteDumper.Companion.writePink
import com.query.utils.FileUtils.getFile
import com.query.utils.IndexType
import com.query.utils.progress
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import java.awt.image.BufferedImage
import java.nio.ByteBuffer

class MapSceneDumper : TypeManager {

    override val requiredDefs = listOf(
        DefinitionsTypes.OBJECTS,
        DefinitionsTypes.SPRITES
    )

    override fun load() {
        writeImage()
    }

    override fun onTest() {
        writeImage()
    }

    fun getIdentifier(file: Int, frame: Int): Int {
        return file shl 16 or (frame and 0xFFFF)
    }

    private fun writeImage() {
        val progress = progress("Writing Area Sprites",  objects().filter { it.mapSceneID != -1 }.distinctBy { it.mapSceneID }.size.toLong())
        collectSprites().forEach {
            try {
                it.value.writePink(getFile("mapsScenes/", "${it.key}.png"))
                progress.step()
            }catch (e : Exception) {
                progress.step()
            }
        }
        progress.close()

    }

    companion object {
        @JvmStatic
        fun main(args : Array<String>) {
            val parser = ArgParser("app")
            val rev by parser.option(ArgType.Int, description = "The revision you wish to dump").default(0)
            parser.parse(args)
            Application.revision = rev

            MapSceneDumper().test()
        }

        fun collectSprites() : Map<Int, BufferedImage> {
            val map : MutableMap<Int,BufferedImage> = emptyMap<Int,BufferedImage>().toMutableMap()
            objects().filter { it.mapSceneID != -1 }.distinctBy { it.mapSceneID }.forEachIndexed { index, objects ->
                val container: ByteArray = library.data(IndexType.SPRITES.number, 317)!!
                val sprite = Sprite.decode(ByteBuffer.wrap(container))
                val img = sprite.getFrame(index)
                map[index] = img!!
            }
            return map
        }

    }

}