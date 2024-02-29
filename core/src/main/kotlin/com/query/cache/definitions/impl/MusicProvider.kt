package com.query.cache.definitions.impl

import TrackLoader
import com.displee.compress.decompress
import com.query.Application
import com.query.Constants
import com.query.cache.definitions.Loader
import com.query.cache.definitions.Serializable
import com.query.dump.DefinitionsTypes
import com.query.cache.definitions.Definition
import com.query.utils.IndexType
import com.query.utils.index
import java.io.DataOutputStream
import java.util.concurrent.CountDownLatch

data class MusicDefinition(
    override var id: Int,
    var midi: ByteArray = ByteArray(0),
    val wikiLink : String = ""
) : Definition() {
    override fun encode(dos: DataOutputStream) {
        TODO("Not yet implemented")
    }
}

class MusicProvider(val latch: CountDownLatch?) : Loader, Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(MusicDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {

        val table = Constants.library.index(IndexType.MUSIC)
        val definitions = listOf<com.query.cache.definitions.Definition>().toMutableList()
        for (i in 0 until table.archives().size) {
            val sector = table.readArchiveSector(i) ?: continue
            definitions.add(decode(sector.decompress(), MusicDefinition(i)))
        }

        return Serializable(DefinitionsTypes.MUSIC,this, definitions,false)
    }



    private fun decode(buffer: ByteArray, definition: MusicDefinition): Definition {
        definition.midi = TrackLoader().decode(buffer)
        return definition
    }

}

data class MusicData(
    val results : MutableMap<String, Results> = emptyMap<String, Results>().toMutableMap(),
)

data class Results(
    val fullurl : String = "",
)

