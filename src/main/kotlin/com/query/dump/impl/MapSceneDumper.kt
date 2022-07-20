package com.query.dump.impl

import com.query.Application
import com.query.Application.mapScene
import com.query.dump.DefinitionsTypes
import com.query.dump.TypeManager
import com.query.utils.FileUtils.getFile
import com.query.utils.progress
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default

class MapSceneDumper : TypeManager {

    override val requiredDefs = listOf(
        DefinitionsTypes.MAPSCENE,
        DefinitionsTypes.SPRITES
    )

    override fun load() {
        writeImage()
    }

    override fun onTest() {
        writeImage()
    }


    private fun writeImage() {
        val progress = progress("Writing Map Scene Sprites",  mapScene().size.toLong())
        mapScene().forEach {
            try {
                getFile("sprites/pink/", "${it.spriteId}.png").copyTo(getFile("mapsScene/","${it.spriteId}.png"))
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

    }

}