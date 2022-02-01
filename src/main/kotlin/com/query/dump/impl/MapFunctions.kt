package com.query.dump.impl

import com.query.Application.areas
import com.query.Application.objects
import com.query.Application.sprites
import com.query.Application.textures
import com.query.cache.definitions.loader.AreaLoader
import com.query.cache.definitions.loader.ObjectLoader
import com.query.cache.definitions.loader.SpriteLoader
import com.query.cache.definitions.loader.TextureLoader
import com.query.cache.definitions.provider.AreaProvider
import com.query.dump.TypeManager
import com.query.utils.FileUtils
import com.query.utils.FileUtils.getDir
import com.query.utils.Sprite
import com.query.utils.progress
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.StandardCopyOption


class MapFunctions : TypeManager() {

    override fun load() {
        writeImage()
    }

    override fun onTest() {
        if(areas() == null) {
            AreaLoader(null,true).run()
        }
        if(sprites() == null) {
            SpriteLoader(null,false).run()
        }
        writeImage()
    }

    private fun writeImage() {

        val progress = progress("Writing Area Sprites", textures()!!.size.toLong())
        areas()!!.forEachIndexed { index, area ->
            sprites()!![area.spriteId].sprite.writePink(getDir("MapFunctions"))
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