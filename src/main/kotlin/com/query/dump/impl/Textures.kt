package com.query.dump.impl

import com.query.Application
import com.query.Application.textures
import com.query.dump.DefinitionsTypes
import com.query.dump.TypeManager
import com.query.utils.FileUtils
import com.query.utils.progress
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default


class Textures : TypeManager {

    override val requiredDefs = listOf(DefinitionsTypes.TEXTURES)

    override fun load() {
        writeTexures()
    }

    override fun onTest() {
        writeTexures()
    }

    private fun writeTexures() {

        val progress = progress("Writing Textures", textures().size.toLong())

        val textureAlready = mutableListOf<Int>()
        var index = 0
        textures().forEach {
            try {
                if(!textureAlready.contains(it.fileIds[0])) {
                    val trans = FileUtils.getFile("sprites/transparent/", "${it.fileIds[0]}.png")
                    val pink = FileUtils.getFile("sprites/pink/", "${it.fileIds[0]}.png")

                    trans.copyTo(FileUtils.getFile("textures/transparent/","${index}.png"),true)
                    pink.copyTo(FileUtils.getFile("textures/pink/","${index}.png"),true)
                    textureAlready.add(it.fileIds[0])
                    index++
                }

            }catch (e : Exception) {

            }


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


            Textures().test()
        }
    }

}