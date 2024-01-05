package com.query.utils

import com.github.michaelbull.logging.InlineLogger

private val logger = InlineLogger("TimedLoader")

fun timedLoadDef(name: String, block: () -> Int) {
    val start = System.currentTimeMillis()
    val result = block.invoke()
    timedLoad(name, result, start)
}

fun timedLoad(name: String, result: Int, start: Long) {
    val duration = System.currentTimeMillis() - start
    logger.info { "Loaded $result ${name.plural(result)} in ${duration}ms" }
}

fun timedLoad(name: String, block: () -> Unit) {
    val start = System.currentTimeMillis()
    block.invoke()
    timedLoad(name, start)
}

fun timedLoad(name: String, start: Long) {
    val duration = System.currentTimeMillis() - start
    logger.info { "Loaded $name in ${duration}ms" }
}