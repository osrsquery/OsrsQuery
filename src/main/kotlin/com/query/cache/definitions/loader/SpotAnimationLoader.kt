package com.query.cache.definitions.loader

import com.query.Application
import com.query.cache.definitions.provider.*
import java.util.concurrent.CountDownLatch

class SpotAnimationLoader(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Runnable {

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(SpotAnimationDefinition::class.java, SpotAnimationProvider().load(writeTypes).definition)
        Application.prompt(SpotAnimationLoader::class.java, start)
        latch?.countDown()
    }

}