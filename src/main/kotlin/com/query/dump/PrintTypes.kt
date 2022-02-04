package com.query.dump

import com.query.cache.definitions.Definition
import com.query.utils.FileUtils
import com.query.utils.capitalizeWords
import com.query.utils.progress
import com.query.utils.writeJson
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

        writeJson(completeLocation,def)

        val progress = progress("Writing ${type.typeName.capitalizeWords()} Types", def.size.toLong())
        def.forEach {
            writeJson(File(location,"${it.id}.json"),it)
            progress.step()
        }
        progress.close()
    }

}