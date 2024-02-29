package com.query.cache.definitions.impl

import com.query.cache.SpriteDecoder
import com.displee.compress.decompress
import com.query.Application
import com.query.Constants
import com.query.cache.definitions.Loader
import com.query.cache.definitions.Serializable
import com.query.cache.definitions.Definition
import com.query.dump.DefinitionsTypes
import com.query.utils.IndexType
import com.query.utils.index
import java.awt.image.BufferedImage
import java.io.DataOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch


data class SpriteDefinition(
    override var id: Int,
    var sprite: BufferedImage
) : Definition() {
    override fun encode(dos: DataOutputStream) {
        TODO("Not yet implemented")
    }
}

class SpriteProvider(val latch: CountDownLatch?) : Loader, Runnable {

    override val revisionMin = 1

    override fun run() {
        val start: Long = System.currentTimeMillis()
        Application.store(SpriteDefinition::class.java, load().definition)
        Application.prompt(this::class.java, start)
        latch?.countDown()
    }

    override fun load(): Serializable {
        val table = Constants.library.index(IndexType.SPRITES)
        val sprites : MutableList<SpriteDefinition> = emptyList<SpriteDefinition>().toMutableList()
        table.archives().forEach {
            val sector = table.readArchiveSector(it.id)
            if(sector != null) {
                val sprite = SpriteDecoder.decode(ByteBuffer.wrap(sector.decompress()))
                for (frame in 0 until sprite.size()) {
                    sprites.add(SpriteDefinition(it.id,sprite.getFrame(frame)!!))
                }
            }
        }
        return Serializable(DefinitionsTypes.SPRITES,this, sprites,true)
    }




}

