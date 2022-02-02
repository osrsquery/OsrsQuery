package com.query.cache.definitions.loader

import com.query.Application
import com.query.cache.definitions.provider.*
import java.util.concurrent.CountDownLatch

class KitLoader(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Runnable {

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(KitDefinition::class.java, KitProvider().load(writeTypes).definition)
        Application.prompt(KitLoader::class.java, start)
        latch?.countDown()
    }

}