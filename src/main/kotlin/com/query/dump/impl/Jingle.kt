package com.query.dump.impl

import com.query.Application.music
import com.query.dump.DefinitionsTypes
import com.query.dump.TypeManager
import com.query.utils.progress
import com.query.Application
import com.query.Application.jingle
import com.query.utils.FileUtils.getFile
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default

class Jingle : TypeManager {

    override val requiredDefs = listOf(DefinitionsTypes.JINGLE)

    override fun load() {
        write()
    }

    override fun onTest() {
        write()
    }

    private fun write() {
        val progress = progress("Writing Jingles", music().size.toLong())
        jingle().forEach {
            val dest = getFile("/sounds/jingles/","${it.id}.midi")
            dest.writeBytes(it.midi)
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

            Jingle().test()

        }
    }

}