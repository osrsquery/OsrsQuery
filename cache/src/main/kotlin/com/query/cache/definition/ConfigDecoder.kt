package com.query.cache.definition

import com.query.buffer.read.Reader
import com.query.cache.Cache
import com.query.cache.Definition
import com.query.cache.DefinitionDecoder
import com.query.cache.Index
import com.query.cache.active.ActiveCache

abstract class ConfigDecoder<T : Definition>(internal val archive: Int) : DefinitionDecoder<T>(Index.CONFIGS) {

    override fun getArchive(id: Int) = archive

    override fun fileName() = ActiveCache.configFile(archive)

    override fun readId(reader: Reader): Int {
        return reader.readShort()
    }

    override fun size(cache: Cache): Int {
        return cache.lastFileId(Index.CONFIGS, archive)
    }
}