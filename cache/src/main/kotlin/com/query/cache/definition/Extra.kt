package com.query.cache.definition

import com.query.buffer.read.Reader
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap

@Suppress("UNCHECKED_CAST")
interface Extra {
    var stringId: String
    var extras: Map<String, Any>?

    operator fun <T: Any> get(key: String): T = extras!!.getValue(key) as T

    fun contains(key: String?) = extras?.containsKey(key) ?: false

    fun <T : Any> getOrNull(key: String) = extras?.get(key) as? T

    operator fun <T : Any> get(key: String, defaultValue: T) = getOrNull(key) as? T ?: defaultValue

    fun readParameters(buffer: Reader, parameters: Parameters) {
        val length = buffer.readUnsignedByte()
        if (length == 0) {
            return
        }
        val extras = Object2ObjectOpenHashMap<String, Any>()
        for (i in 0 until length) {
            val string = buffer.readUnsignedBoolean()
            val id = buffer.readUnsignedMedium()
            val name = parameters.parameters.getOrDefault(id, id.toString())
            parameters.set(extras, name, if (string) buffer.readString() else buffer.readInt())
        }
        this.extras = extras
    }
}