package com.query.dump.impl

import com.google.gson.Gson
import com.query.Application.areas
import com.query.cache.definitions.impl.AreaProvider
import com.query.dump.DefinitionsTypes
import com.query.dump.TypeManager
import com.query.utils.FileUtils.getBase
import java.io.File


data class Labels(
    val locations : List<Locations> = emptyList()
)

data class Locations(
    val name : String,
    val coords : ArrayList<Int>,
    val size : String
)


class LabelDumper : TypeManager {

    override val requiredDefs: List<DefinitionsTypes> = emptyList<DefinitionsTypes>().toMutableList()

    override fun load() {

        val data: Labels? = Gson().fromJson(File(getBase(),"locations.json").readText(), Labels::class.java)

        areas().filter { it.description != null }.forEach {
            if (data!!.locations.count { data -> it.description!!.contains(data.name) } == 0) {
                println(AreaProvider.formatName("${it.description} : ${AreaProvider.fontSizeName(it.fontSize)}"))
            }
        }

    }

    override fun onTest() {

    }



}