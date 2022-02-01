package com.query.cache.definitions.loader

import com.query.Application
import com.query.cache.definitions.provider.SpriteDefinition
import com.query.cache.definitions.provider.SpriteProvider
import java.util.concurrent.CountDownLatch

class SpriteLoader(val latch: CountDownLatch?, val writeTypes : Boolean = false) : Runnable {

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(SpriteDefinition::class.java, SpriteProvider().load(writeTypes).definition)
        Application.prompt(SpriteLoader::class.java, start)
        latch?.countDown()
    }

}