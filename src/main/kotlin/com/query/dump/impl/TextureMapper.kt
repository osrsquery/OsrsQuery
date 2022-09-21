package com.query.dump.impl

import com.google.gson.GsonBuilder
import com.query.Application.items
import com.query.Application.npcs
import com.query.Application.objects
import com.query.Application.textures
import com.query.cache.definitions.impl.ItemProvider
import com.query.cache.definitions.impl.NpcProvider
import com.query.cache.definitions.impl.ObjectProvider
import com.query.cache.model.getAllModels
import com.query.dump.DefinitionsTypes
import com.query.dump.TypeManager
import com.query.utils.FileUtils
import com.query.utils.progress


data class TextureMapped(
    var spriteID : Int = -1,
    var modelIds : MutableList<Int> = mutableListOf(),
    val named : MutableList<String> = mutableListOf()
)

class Textures : TypeManager {

    override val requiredDefs = listOf(
        DefinitionsTypes.TEXTURES,
        DefinitionsTypes.ITEMS,
        DefinitionsTypes.NPCS,
        DefinitionsTypes.OBJECTS
    )

    val modelToName = mutableMapOf<Int,String>()

    override fun load() {
        init()
    }

    override fun onTest() {
        NpcProvider(null,false).run()
        ObjectProvider(null,false).run()
        ItemProvider(null,false).run()
        init()


    }

    private fun init() {
        val mappedTextures : MutableMap<Short,TextureMapped> = mutableMapOf()

        val texturedModels = texturesToModels()

        val progress = progress("Writing Textures Mappings", texturedModels.size.toLong())

        texturesToModels().forEach {
            val textureMapped = TextureMapped()

            try {
                textureMapped.spriteID = textures()[it.key.toInt()].fileIds[0]
            }catch (e: Exception) {
                textureMapped.spriteID = 4667
            }

            textureMapped.modelIds = it.value.toList().toMutableList()

            it.value.forEach { modelID ->
                 textureMapped.named.add(getNameForModel(modelID))
            }

            mappedTextures[it.key] = textureMapped

            progress.step()
        }
        progress.close()

        val data = GsonBuilder().setPrettyPrinting().create().toJson(mappedTextures)
        FileUtils.getFile("types/","texture-mappings-complete.json").writeText(data)
    }

    private fun getNameForModel(id : Int) : String {
        if(modelToName.containsKey(id)) return modelToName[id]!!

        var name = "Unknown"
        var type = "Unknown"
        var typeID = -1

        val objectDef = objects().filter { it.objectModels != null }.find { it.objectModels!!.contains(id) }

        if(objectDef != null) {
            type = "Object"
            name = objectDef.name
            typeID = objectDef.id
            modelToName[id] = "$name - { Type: ${type}, ModelID: $id, ${type}ID: $typeID }"
            return modelToName[id]!!
        }

        val npcs = npcs().find {
            it.models != null && it.models!!.contains(id) ||  it.chatheadModels != null && it.chatheadModels!!.contains(id)
        }

        if(npcs != null) {
            type = "Npc"
            name = npcs.name
            typeID = npcs.id
            modelToName[id] = "$name - { Type: ${type}, ModelID: $id, ${type}ID: $typeID }"
            return modelToName[id]!!
        }

        val items = items().find {
            it.female_dialogue_head == id ||
            it.female_equip_attachment == id ||
            it.female_equip_emblem == id ||
            it.female_equip_main == id ||
            it.equipped_model_female_2 == id ||
            it.equipped_model_female_dialogue_2 == id ||
            it.male_dialogue_head == id ||
            it.male_equip_attachment == id ||
            it.male_equip_emblem == id ||
            it.male_equip_main == id ||
            it.inventoryModel == id
        }

        if(items != null) {
            type = "Item"
            name = items.name
            typeID = items.id
            modelToName[id] = "$name - { Type: ${type}, ModelID: $id, ${type}ID: $typeID }"
            return modelToName[id]!!
        }

        modelToName[id] = "$name - { Type: ${type}, ModelID: $id, ${type}ID: $typeID }"
        return modelToName[id]!!

    }


    private fun texturesToModels(): HashMap<Short, MutableSet<Int>> {
        val textureToModel = HashMap<Short, MutableSet<Int>>()
        getAllModels().filter { it.value.faceTextures != null }.forEach {
            it.value.faceTextures!!.filter { texID -> texID.toInt() != -1 }.forEach { texID ->
                textureToModel.getOrPut(texID) { mutableSetOf() } .add(it.key)
            }
        }
        return textureToModel
    }



    companion object {
        @JvmStatic
        fun main(args : Array<String>) {
            Textures().test()
        }
    }

}