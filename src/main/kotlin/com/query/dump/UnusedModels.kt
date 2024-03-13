package com.query.dump

import com.query.Application
import com.query.Constants
import com.query.cache.definitions.impl.*
import com.query.utils.IndexType
import com.query.utils.index

object UnusedModels {

    fun init() {
        ObjectProvider(null).run()
        ItemProvider(null).run()
        NpcProvider(null).run()
        KitProvider(null).run()
        SpotAnimationProvider(null).run()

        val usedModels : MutableList<Int> = mutableListOf()

        Application.objects().filter { it.objectModels != null }.forEach {
            it.objectModels!!.forEach { model ->  usedModels.add(model) }
        }

        Application.kits().forEach {
            it.models!!.forEach { model ->
                usedModels.add(model)
            }
            it.chatheadModels.forEach { model ->
                usedModels.add(model)
            }
        }

        Application.spotanimations().forEach {
            usedModels.add(it.modelId)
        }

        Application.npcs().forEach {
            it.models?.forEach { model ->
                usedModels.add(model)
            }
            it.chatheadModels?.forEach { model ->
                usedModels.add(model)
            }
        }

        Application.items().forEach {
            usedModels.add(it.female_equip_main)
            usedModels.add(it.female_equip_emblem)
            usedModels.add(it.female_equip_attachment)
            usedModels.add(it.female_dialogue_head)
            usedModels.add(it.equipped_model_female_2)
            usedModels.add(it.equipped_model_female_dialogue_2)

            usedModels.add(it.male_equip_main)
            usedModels.add(it.male_dialogue_head)
            usedModels.add(it.male_equip_attachment)
            usedModels.add(it.male_equip_emblem)
            usedModels.add(it.inventoryModel)

        }
        val table = Constants.library.index(IndexType.MODELS)

        val models = LinkedHashSet(usedModels).toMutableList()

        println("Used Models: ${models.size}")
        println("Total Models: ${table.archives().size}")
        println("Unused Models: ${table.archives().size - models.size}")

    }

    @JvmStatic
    fun main(args : Array<String>) {
        init()
    }

}