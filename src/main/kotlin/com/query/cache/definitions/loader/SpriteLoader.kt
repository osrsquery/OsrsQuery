package com.query.cache.definitions.loader

import com.query.Application
import com.query.cache.definitions.provider.SpriteDefinition
import com.query.cache.definitions.provider.SpriteProvider
import com.query.cache.definitions.provider.TextureDefinition
import com.query.cache.definitions.provider.TextureProvider
import java.util.concurrent.CountDownLatch

class SpriteLoader(val latch: CountDownLatch?) : Runnable {

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(SpriteDefinition::class.java, SpriteProvider().load().definitions)
        Application.prompt(SpriteLoader::class.java, start)
        latch?.countDown()
    }

}