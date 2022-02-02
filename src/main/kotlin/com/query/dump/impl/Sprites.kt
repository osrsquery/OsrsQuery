package com.query.dump.impl

import com.query.Application.sprites
import com.query.dump.TypeManager
import com.query.utils.FileUtils.getFile
import com.query.utils.progress


class Sprites : TypeManager() {

    override fun load() {
        writeSprites()
    }

    override fun onTest() {
        //SpriteLoader(null,false).run()
        writeSprites()
    }

    private fun writeSprites() {
        val progress = progress("Writing Sprites", sprites()!!.size.toLong())
        sprites()!!.forEach {
            it.sprite.writeTransparent(getFile("sprites/transparent/", "${it.id}.png"))
            it.sprite.writePink(getFile("sprites/pink/", "${it.id}.png"))
            progress.step()
        }
        progress.close()
    }

    companion object {
        @JvmStatic
        fun main(args : Array<String>) {
            Sprites().test()
        }

    }

}