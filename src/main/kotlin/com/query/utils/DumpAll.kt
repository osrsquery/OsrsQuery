package com.query.utils

import com.query.Application


object DumpAll {

    fun dump() {
        for (index in 1..202) {
            try {
                Application.initialize(index)
            }catch (e : Exception) {
                continue
            }
        }
    }

}

fun main() {
    DumpAll.dump()
}