package com.query.cache

import com.query.cache.definitions.Definition

data class Serializable(
    val loader: Loader,
    val definitions: List<Definition>,
    val path: String,
)

interface Loader {
    fun load(): Serializable
}