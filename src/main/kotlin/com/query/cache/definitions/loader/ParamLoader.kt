package com.query.cache.definitions.loader

import com.query.Application
import com.query.cache.definitions.provider.*
import java.util.concurrent.CountDownLatch

class ParamLoader(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Runnable {

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(ParamDefinition::class.java, ParamProvider().load(writeTypes).definition)
        Application.prompt(ParamLoader::class.java, start)
        latch?.countDown()
    }

}