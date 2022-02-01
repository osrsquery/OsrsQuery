package com.query.dump.impl

import com.query.Application.areas
import com.query.Application.sprites
import com.query.cache.definitions.loader.AreaLoader
import com.query.cache.definitions.loader.SpriteLoader
import com.query.dump.TypeManager
import com.query.utils.FileUtils.getFile
import com.query.utils.progress
import java.nio.file.Files
import java.nio.file.StandardCopyOption


class MapFunctions : TypeManager() {

    override fun load() {
        writeImage()
    }

    override fun onTest() {
        if(areas() == null) {
            AreaLoader(null,false).run()
        }
        if(sprites() == null) {
            SpriteLoader(null,false).run()
        }
        writeImage()
    }


    private fun writeImage() {

        val progress = progress("Writing Area Sprites",  areas()!!.filter { it.spriteId != -1 }.size.toLong())
        areas()!!.filter { it.spriteId != -1 }.forEachIndexed { index, area ->
            val pink = getFile("sprites/pink/", "${area.spriteId}.png")
            Files.copy(pink.toPath(), getFile("mapFunctions/", "${index}.png").toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
            progress.step()
        }
        progress.close()

    }

    companion object {
        @JvmStatic
        fun main(args : Array<String>) {
            MapFunctions().test()
        }
    }

}