package com.query.dump.impl

import com.query.Application.sprites
import com.query.Application.textures
import com.query.cache.definitions.loader.SpriteLoader
import com.query.cache.definitions.loader.TextureLoader
import com.query.dump.TypeManager
import com.query.utils.FileUtils
import com.query.utils.progress
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.StandardCopyOption


class Textures : TypeManager() {

    override fun load() {
        writeTexures()
    }

    override fun onTest() {
        if(sprites() == null) {
            SpriteLoader(null,false).run()
        }
        TextureLoader(null,true).run()
        writeTexures()
    }

    private fun writeTexures() {

        val progress = progress("Writing Textures", textures()!!.size.toLong())
        textures()!!.forEach {
            val trans = FileUtils.getFile("sprites/transparent/", "${it.fileIds[0]}.png")
            val pink = FileUtils.getFile("sprites/pink/", "${it.fileIds[0]}.png")

            Files.copy(trans.toPath(), FileUtils.getFile("textures/transparent/", "${it.id}.png").toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
            Files.copy(pink.toPath(), FileUtils.getFile("textures/pink/", "${it.id}.png").toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )

            progress.step()
        }
        progress.close()

    }

    companion object {
        @JvmStatic
        fun main(args : Array<String>) {
            Textures().test()
        }
    }

}