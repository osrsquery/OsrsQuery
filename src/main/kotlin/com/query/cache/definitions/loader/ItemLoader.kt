package com.query.cache.definitions.loader

import com.query.Application
import com.query.cache.definitions.provider.*
import java.util.concurrent.CountDownLatch

class ItemLoader(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Runnable {

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(ItemDefinition::class.java, ItemProvider().load(writeTypes).definition)
        Application.prompt(ItemLoader::class.java, start)
        latch?.countDown()
    }

}