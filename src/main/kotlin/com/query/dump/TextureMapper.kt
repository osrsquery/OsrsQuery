package com.query.dump

import com.google.gson.GsonBuilder
import com.query.Application
import com.query.Application.items
import com.query.Application.npcs
import com.query.Application.objects
import com.query.Application.textures
import com.query.cache.CacheManager
import com.query.cache.definitions.impl.*
import com.query.game.model.getAllModels
import com.query.utils.FileUtil
import com.query.utils.progress


data class TextureMapped(
    var spriteID : Int = -1,
    var modelIds : MutableList<Int> = mutableListOf(),
    var overlayIds : MutableList<Int> = mutableListOf(),
    val named : MutableList<String> = mutableListOf()
)

object Textures {

    val modelToName = mutableMapOf<Int,String>()

    val mappedTextures : MutableMap<Int, TextureMapped> = mutableMapOf()

    fun init() {

        if (Application.definitions[NpcDefinition::class.java] == null) {
            NpcProvider(null).run()
            ObjectProvider(null).run()
            ItemProvider(null).run()
            OverlayProvider(null).run()
            NpcProvider(null).run()
            TextureProvider(null).run()
        }

        val texturedModels = texturesToModels()

        val progress = progress("Writing Textures Mappings", texturedModels.size.toLong())

        Application.overlays().filter { it.textureId != -1 }.forEach {
            mappedTextures[it.textureId] = TextureMapped()
            try {
                getOrCreate(it.textureId).spriteID = textures()[it.textureId].fileIds[0]
            }catch (e: Exception) {
                getOrCreate(it.textureId).spriteID = -1
            }
            if (!getOrCreate(it.textureId).overlayIds.contains(it.id)) {
                getOrCreate(it.textureId).overlayIds.add(it.id)
            }
        }

        texturesToModels().forEach {
            val textureMapped = getOrCreate(it.key.toInt())


            try {
                println(it.key.toInt())
                textureMapped.spriteID = textures()[it.key.toInt()].fileIds[0]
            }catch (e: Exception) {
                println(it.key.toInt())
                textureMapped.spriteID = -1
            }

            textureMapped.modelIds = it.value.toList().toMutableList()

            it.value.forEach { modelID ->
                 textureMapped.named.add(getNameForModel(modelID))
            }

            progress.step()
        }
        progress.close()

        val data = GsonBuilder().setPrettyPrinting().create().toJson(mappedTextures.toSortedMap())
        FileUtil.getFile("types/","texture-mappings-complete.json").writeText(data)
    }

    private fun getOrCreate(id : Int) : TextureMapped {
        if (!mappedTextures.containsKey(id)) {
            mappedTextures[id] = TextureMapped()
        }
        return mappedTextures[id]!!
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


    fun dumpFishingSpotNpcs() {
        val textures = texturesToModels()[17]
        val listIds = emptyList<Int>().toMutableList()
        npcs().filter { npc ->
            npc.name.contains("fishing spot", ignoreCase = true) && textures != null &&
                    npc.models!!.any { modelId -> textures.contains(modelId) }
        }.forEach {
            println(it.name)
            listIds.add(it.id)
        }
        println(listIds.joinToString(separator = ", "))
    }

}

fun main() {
    Application.revision = 220
    CacheManager.initialize()
    NpcProvider(null).run()
    TextureProvider(null).run()
    Textures.dumpFishingSpotNpcs()
}