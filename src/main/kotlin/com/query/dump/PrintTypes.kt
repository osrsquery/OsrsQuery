package com.query.dump

import com.query.Application.gson
import com.query.cache.definitions.Definition
import com.query.utils.FileUtils
import com.query.utils.capitalizeWords
import com.query.utils.progress
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

enum class DefinitionsTypes {
    OBJECTS,
    AREAS,
    SPRITES,
    KIT,
    NPCS,
    ENUMS,
    HEALTH,
    INVS,
    ITEMS,
    OVERLAYS,
    PARAMS,
    SEQUENCES,
    SPOTANIMS,
    UNDERLAYS,
    VARBITS,
    TEXTURES,
    MUSIC,
    JINGLE;

    val typeName = this.name.lowercase()

}

class PrintTypes(type : DefinitionsTypes, def : List<Definition>) {

    private val name = type.typeName
    private val completeLocation = FileUtils.getFile("/types/","$name-complete.json")
    private val location = FileUtils.getDir("/types/${name}/")

    init {
        val output = BufferedWriter(FileWriter(completeLocation))

        output.write(gson.toJson(def))
        output.close()

        val progress = progress("Writing ${type.typeName.capitalizeWords()} Types", def.size.toLong())
        def.forEach {
            val file = BufferedWriter(FileWriter(File(location,"${it.id}.json")))
            file.write(gson.toJson(it))
            file.close()
            progress.step()
        }
        progress.close()
    }

}