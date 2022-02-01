package com.query.cache.definitions.loader

import com.query.Application
import com.query.cache.definitions.provider.AreaDefinition
import com.query.cache.definitions.provider.AreaProvider
import com.query.cache.definitions.provider.ObjectDefinition
import com.query.cache.definitions.provider.ObjectProvider
import java.util.concurrent.CountDownLatch

class AreaLoader(val latch: CountDownLatch?, val writeTypes : Boolean = false) : Runnable {

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(AreaDefinition::class.java, AreaProvider().load(writeTypes).definition)
        Application.prompt(AreaLoader::class.java, start)
        latch?.countDown()
    }

}