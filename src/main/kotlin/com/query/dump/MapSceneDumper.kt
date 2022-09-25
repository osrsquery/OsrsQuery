package com.query.dump

import com.query.cache.SpriteDecoder
import com.query.Application.objects
import com.query.Constants.library
import com.query.cache.definitions.impl.ObjectProvider
import com.query.dump.SpriteDumper.Companion.writePink
import com.query.utils.FileUtil
import com.query.utils.FileUtil.getFile
import com.query.utils.IndexType
import com.query.utils.progress
import java.awt.image.BufferedImage
import java.nio.ByteBuffer

class MapSceneDumper {

    fun init() {
        if (objects().isEmpty()) {
            ObjectProvider(null).run()
            SpriteDumper().init()
        }
        writeImage()
    }

    private fun writeImage() {
        val progress = progress("Writing Area Sprites",  objects().filter { it.mapSceneID != -1 }.distinctBy { it.mapSceneID }.size.toLong())
        FileUtil.getDir("mapScenes/").delete()
        collectSprites().forEach {
            try {
                it.value.writePink(getFile("mapScenes/", "${it.key}.png"))
                progress.step()
            }catch (e : Exception) {
                progress.step()
            }
        }
        progress.close()

    }

    companion object {

        fun collectSprites() : Map<Int, BufferedImage> {
            val map : MutableMap<Int,BufferedImage> = emptyMap<Int,BufferedImage>().toMutableMap()
            objects().filter { it.mapSceneID != -1 }.distinctBy { it.mapSceneID }.forEachIndexed { index, objects ->
                val container: ByteArray = library.data(IndexType.SPRITES.number, 317)!!
                val sprite = SpriteDecoder.decode(ByteBuffer.wrap(container))
                val img = sprite.getFrame(index)
                map[index] = img!!
            }
            return map
        }

    }

}