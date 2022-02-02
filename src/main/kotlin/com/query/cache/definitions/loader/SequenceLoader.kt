package com.query.cache.definitions.loader

import com.query.Application
import com.query.cache.definitions.provider.*
import java.util.concurrent.CountDownLatch

class SequenceLoader(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Runnable {

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(SequenceDefinition::class.java, SequenceProvider().load(writeTypes).definition)
        Application.prompt(SequenceLoader::class.java, start)
        latch?.countDown()
    }

}