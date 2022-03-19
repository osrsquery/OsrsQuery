package com.query

import com.query.cache.definitions.impl.AreaProvider
import com.query.cache.definitions.impl.ObjectProvider
import com.query.cache.download.CacheLoader
import com.query.dump.impl.LabelDumper

object LabelDumperTest {

    fun extract() {
        CacheLoader.initialize()


        AreaProvider(null,false).run()
        ObjectProvider(null,false).run()
        LabelDumper().load()


    }

}

fun main() {
    LabelDumperTest.extract()
}