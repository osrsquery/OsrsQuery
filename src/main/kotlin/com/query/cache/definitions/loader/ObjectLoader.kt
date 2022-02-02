package com.query.cache.definitions.loader

import com.query.Application
import com.query.cache.definitions.provider.ObjectDefinition
import com.query.cache.definitions.provider.ObjectProvider
import java.util.concurrent.CountDownLatch

class ObjectLoader(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Runnable {

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(ObjectDefinition::class.java, ObjectProvider().load(writeTypes).definition)
        Application.prompt(ObjectLoader::class.java, start)
        latch?.countDown()
    }

}