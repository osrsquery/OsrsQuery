package com.query.cache

import com.query.cache.definitions.Definition
import com.query.dump.DefinitionsTypes
import com.query.dump.PrintTypes
import com.query.utils.revisionID

data class Serializable(
    val type : DefinitionsTypes,
    val loader: Loader,
    val definition : List<Definition>,
    val writeTypes : Boolean
) {
    init {
        if(writeTypes) {
            PrintTypes(type, definition)
        }
    }
}

interface Loader {
    fun load(): Serializable

    val revisionMin : Int

    fun ignore() = revisionMin > revisionID()

}