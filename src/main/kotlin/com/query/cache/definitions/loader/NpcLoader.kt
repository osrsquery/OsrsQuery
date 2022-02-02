package com.query.cache.definitions.loader

import com.query.Application
import com.query.cache.definitions.provider.*
import java.util.concurrent.CountDownLatch

class NpcLoader(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Runnable {

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(NpcDefinition::class.java, NpcProvider().load(writeTypes).definition)
        Application.prompt(NpcLoader::class.java, start)
        latch?.countDown()
    }

}