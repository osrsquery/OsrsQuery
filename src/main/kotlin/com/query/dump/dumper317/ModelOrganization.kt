package com.query.dump.dumper317

import com.query.Application.items
import com.query.Application.npcs
import com.query.Application.objects
import com.query.cache.definitions.impl.ItemProvider
import com.query.cache.definitions.impl.NpcProvider
import com.query.cache.definitions.impl.ObjectProvider
import com.query.cache.download.CacheLoader
import com.query.utils.progress
import java.io.File


object ModelOrganization {

    fun init() {

        CacheLoader.initialize()
        ObjectProvider(null,false).run()
        NpcProvider(null,false).run()
        ItemProvider(null,false).run()

        val objects = objects().filter { it.objectModels != null }
        val npcs = npcs()
        val items = items()

       val pb = progress("Dumping Objects",objects.size.toLong())
        objects.forEach {
            val name = formatName(it.name,it.id)
            val dir = File("C:\\Users\\Shadow\\Desktop\\models-organised\\objects\\${if(exists.contains(name)) "${name}-${it.id}" else name}\\")
            it.objectModels?.forEach {
                getFiles(it).first.copyTo(File(dir,"${it}.obj"),true)
                getFiles(it).second.copyTo(File(dir,"${it}.mtl"),true)
            }
            pb.step()
        }

        val progressItems = progress("Dumping Items",items.size.toLong())

        items.forEach {


            writeItemFiles(it.id,it.inventoryModel,it.name,"inv","")
            writeItemFiles(it.id,it.female_dialogue_head,it.name,"chathead","female")
            writeItemFiles(it.id,it.female_equip_attachment,it.name,"attachment","female")
            writeItemFiles(it.id,it.female_equip_emblem,it.name,"emblem","female")
            writeItemFiles(it.id,it.female_equip_main,it.name,"main","female")

            writeItemFiles(it.id,it.male_dialogue_head,it.name,"chathead","male")
            writeItemFiles(it.id,it.male_equip_attachment,it.name,"attachment","male")
            writeItemFiles(it.id,it.male_equip_emblem,it.name,"emblem","male")
            writeItemFiles(it.id,it.male_equip_main,it.name,"main","male")


            progressItems.step()
        }
        progressItems.close()

        val progressNpc = progress("Dumping Npcs",npcs.size.toLong())

        npcs.forEach {
            val name = formatName(it.name,it.id)
            val dir = File("C:\\Users\\Shadow\\Desktop\\models-organised\\npcs\\${if(exists.contains(name)) "${name}-${it.id}" else name}\\")
            exists.add(if(exists.contains(name)) "${name}-${it.id}" else name)
            dir.mkdirs()

            if(it.models != null) {
                it.models!!.forEach { modelID ->
                    val model = getFiles(modelID)
                    model.first.copyTo(File(dir,"${modelID}.obj"),true)
                    model.second.copyTo(File(dir,"${modelID}.mtl"),true)
                }
            }

            if(it.chatheadModels != null) {
                it.chatheadModels!!.forEach { modelId ->
                    val model = getFiles(modelId)
                    val dir1 = File("C:\\Users\\Shadow\\Desktop\\models-organised\\npcs\\${if(exists.contains(name)) "${name}-${it.id}" else name}\\chatheads\\")
                    dir1.mkdirs()
                    model.first.copyTo(File(dir1,"${modelId}.obj"),true)
                    model.second.copyTo(File(dir1,"${modelId}.mtl"),true)
                }
            }

            progressNpc.step()
        }

        progressNpc.close()

	}


    private fun getFiles(id : Int) : Pair<File,File> {
        return Pair(
            File("C:\\Users\\Shadow\\Desktop\\models\\$id.obj"),
            File("C:\\Users\\Shadow\\Desktop\\models\\$id.mtl")
        )
    }

    private fun writeItemFiles(id : Int,modelID: Int,name : String,type : String, gender : String) {
        if(modelID == -1) {
            return
        }
        val dir = File("C:\\Users\\Shadow\\Desktop\\models-organised\\items\\${formatName(name,id)}\\")
        val files = Pair(
            File("C:\\Users\\Shadow\\Desktop\\models\\$modelID.obj"),
            File("C:\\Users\\Shadow\\Desktop\\models\\$modelID.mtl")
        )
        File(dir,"/${gender}/").mkdirs()

        files.first.copyTo(File(dir,"/${gender}/${type}_${modelID}.obj"),true)
        files.second.copyTo(File(dir,"/${gender}/${type}_${modelID}.mtl"),true)

    }

    val exists : MutableList<String> = emptyList<String>().toMutableList()

    private fun formatName(name : String, id : Int) : String {
        var text = if(name == "null" || name.isEmpty() || name == "? ? ? ?") "Unknown-${id}" else name.replace("<col=ff9040>","")
        return text.replace(Regex("\\<.*?\\>"),"").replace(".","").replace(" (?)","").replace("??? ","").replace("?","")
    }

}

fun main() {
    ModelOrganization.init()
}