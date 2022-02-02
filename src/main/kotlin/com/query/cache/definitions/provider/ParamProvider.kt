package com.query.cache.definitions.provider

import com.displee.cache.index.archive.Archive
import com.query.Application.logger
import com.query.Constants.library
import com.query.cache.Loader
import com.query.cache.Serializable
import com.query.cache.definitions.Definition
import com.query.dump.CacheType
import com.query.utils.ByteBufferExt
import com.query.utils.ConfigType
import com.query.utils.IndexType
import com.query.utils.index
import java.nio.ByteBuffer


data class ParamDefinition(
    override val id: Int = 0,
    var type: Char? = null,
    var isMembers: Boolean = true,
    var defaultInt: Int = 0,
    var defaultString: String? = null
): Definition

class ParamProvider : Loader {

    override fun load(writeTypes : Boolean): Serializable {
        val archive: Archive = library.index(IndexType.CONFIGS).archive(ConfigType.PARAMS.id)!!
        val definitions = archive.fileIds().map {
            decode(ByteBuffer.wrap(archive.file(it)?.data), ParamDefinition(it))
        }
        return Serializable(CacheType.PARAMS,this, definitions,writeTypes)
    }

    fun decode(buffer: ByteBuffer, definition: ParamDefinition): Definition {
        do when (val opcode: Int = buffer.get().toInt() and 0xff) {
            1 -> definition.type = (buffer.get().toInt() and 0xff).toChar()
            2 -> definition.defaultInt = buffer.int
            4 -> definition.isMembers = false
            5 -> definition.defaultString = ByteBufferExt.getString(buffer)
            0 -> break
            else -> logger.warn { "Unhandled param definition opcode with id: ${opcode}." }
        } while (true)
        return definition
    }



}