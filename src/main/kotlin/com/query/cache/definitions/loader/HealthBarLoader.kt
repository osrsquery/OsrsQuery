package com.query.cache.definitions.loader

import com.query.Application
import com.query.cache.definitions.provider.*
import java.util.concurrent.CountDownLatch

class HealthBarLoader(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Runnable {

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(HealthBarDefinition::class.java, HealthBarProvider().load(writeTypes).definition)
        Application.prompt(HealthBarLoader::class.java, start)
        latch?.countDown()
    }

}