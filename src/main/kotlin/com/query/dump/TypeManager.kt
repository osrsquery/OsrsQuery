package com.query.dump

import com.query.cache.download.UpdateCache

abstract class TypeManager {

    fun test() {
        UpdateCache.initialize()
        onTest()
    }

    abstract fun load()

    abstract fun onTest()


}