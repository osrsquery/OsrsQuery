package com.query.cache.definitions.loader

import com.query.Application
import com.query.cache.definitions.provider.*
import java.util.concurrent.CountDownLatch

class InvLoader(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Runnable {

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(InvDefinition::class.java, InvProvider().load(writeTypes).definition)
        Application.prompt(InvLoader::class.java, start)
        latch?.countDown()
    }

}