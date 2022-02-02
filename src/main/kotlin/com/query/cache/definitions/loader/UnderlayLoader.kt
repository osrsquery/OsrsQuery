package com.query.cache.definitions.loader

import com.query.Application
import com.query.cache.definitions.provider.*
import java.util.concurrent.CountDownLatch

class UnderlayLoader(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Runnable {

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(UnderlayDefinition::class.java, UnderlayProvider().load(writeTypes).definition)
        Application.prompt(UnderlayLoader::class.java, start)
        latch?.countDown()
    }

}