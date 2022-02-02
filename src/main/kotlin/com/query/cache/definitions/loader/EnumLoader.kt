package com.query.cache.definitions.loader

import com.query.Application
import com.query.cache.definitions.provider.*
import java.util.concurrent.CountDownLatch

class EnumLoader(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Runnable {

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(EnumDefinition::class.java, EnumProvider().load(writeTypes).definition)
        Application.prompt(EnumLoader::class.java, start)
        latch?.countDown()
    }

}