package com.query.cache.definitions

import com.query.Application
import com.query.dump.DefinitionsTypes
import com.query.dump.PrintTypes
import com.query.utils.revisionID

data class Serializable(
    val type : DefinitionsTypes,
    val loader: Loader,
    val definition : List<Definition>,
    val dontWrite : Boolean = false
) {
    init {
        if(Application.writeData && !dontWrite) {
            PrintTypes(type, definition)
        }
    }
}

interface Loader {
    fun load(): Serializable

    val revisionMin : Int

    fun ignore() = revisionMin > revisionID()

}