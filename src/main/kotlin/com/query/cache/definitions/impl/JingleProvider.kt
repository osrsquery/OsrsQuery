package com.query.cache.definitions.impl

import TrackLoader
import com.displee.compress.decompress
import com.query.Application
import com.query.Constants
import com.query.cache.definitions.Loader
import com.query.cache.definitions.Serializable
import com.query.dump.DefinitionsTypes
import com.query.utils.IndexType
import com.query.utils.index
import java.util.concurrent.CountDownLatch
import com.query.cache.definitions.Definition
import java.io.DataOutputStream

data class JingleDefinition(
    override var id: Int,
    var midi: ByteArray = ByteArray(0),
) : Definition() {
    override fun encode(dos: DataOutputStream) {
        TODO("Not yet implemented")
    }
}

class JingleProvider(val latch: CountDownLatch?) : Loader, Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(JingleDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {

        val table = Constants.library.index(IndexType.JINGLE)
        val definitions = listOf<com.query.cache.definitions.Definition>().toMutableList()

        for (i in 0 until table.archives().size) {
            val sector = table.readArchiveSector(i) ?: continue
            definitions.add(decode(sector.decompress(), JingleDefinition(i)))
        }

        return Serializable(DefinitionsTypes.JINGLE,this, definitions,false)
    }

    private fun decode(buffer: ByteArray, definition: JingleDefinition): Definition {
        definition.midi = TrackLoader().decode(buffer)
        return definition
    }

}