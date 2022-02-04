package com.query.cache.definitions.impl

import TrackLoader
import com.beust.klaxon.Klaxon
import com.displee.compress.decompress
import com.query.Application
import com.query.Constants
import com.query.Constants.client
import com.query.cache.Loader
import com.query.cache.Serializable
import com.query.cache.definitions.Definition
import com.query.dump.DefinitionsTypes
import com.query.utils.IndexType
import com.query.utils.Position
import com.query.utils.index
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import org.apache.commons.lang.StringUtils
import java.net.URL
import java.util.concurrent.CountDownLatch

data class MusicDefinition(
    override var id: Int,
    var midi: ByteArray = ByteArray(0),
    val wikiLink : String = ""
) : Definition

class MusicProvider(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Loader, Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(MusicDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {

        val table = Constants.library.index(IndexType.MUSIC)
        val definitions = listOf<Definition>().toMutableList()
        for (i in 0 until table.archives().size) {
            val sector = table.readArchiveSector(i) ?: continue
            definitions.add(decode(sector.decompress(),MusicDefinition(i)))
        }

        return Serializable(DefinitionsTypes.MUSIC,this, definitions,false)
    }



    private fun decode(buffer: ByteArray, definition: MusicDefinition): Definition {
        definition.midi = TrackLoader().decode(buffer)
        return definition
    }

}

data class MusicData(
    val results : MutableMap<String,Results> = emptyMap<String, Results>().toMutableMap(),
)

data class Results(
    val fullurl : String = "",
)

