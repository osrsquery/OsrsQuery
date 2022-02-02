package com.query.dump.impl

import com.query.Application
import com.query.Application.sprites
import com.query.dump.DefinitionsTypes
import com.query.dump.TypeManager
import com.query.utils.FileUtils.getFile
import com.query.utils.progress
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default


class Sprites : TypeManager {

    override fun load() {
        writeSprites()
    }

    override val requiredDefs = listOf(DefinitionsTypes.SPRITES)

    override fun onTest() {
        writeSprites()
    }

    private fun writeSprites() {
        val progress = progress("Writing Sprites", sprites().size.toLong())
        sprites().forEach {
            it.sprite.writeTransparent(getFile("sprites/transparent/", "${it.id}.png"))
            it.sprite.writePink(getFile("sprites/pink/", "${it.id}.png"))
            progress.step()
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

            Sprites().test()
        }

    }

}