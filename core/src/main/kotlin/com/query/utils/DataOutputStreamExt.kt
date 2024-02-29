package com.query.utils

import java.io.DataOutputStream

fun DataOutputStream.writeParams(data : Map<Int, String>) {
    writeByte(data.size)
    for (entry in data.entries) {
        writeByte(if (isObjectInteger(entry.value)) 1 else 0)

        writeByte(entry.key shr 16)
        writeByte(entry.key shr 8)
        writeByte(entry.key)

        if (isObjectInteger(entry.value)) {
            writeInt(entry.value.toInt())
        } else {
            writeString(entry.value)
        }
    }
}

fun DataOutputStream.write24bitInt(content : Int) {
    writeByte(content shr 16)
    writeByte(content shr 8)
    writeByte(content)
}

fun DataOutputStream.writeString(content : String) {
    write(content.toByteArray())
    writeByte(10)
}