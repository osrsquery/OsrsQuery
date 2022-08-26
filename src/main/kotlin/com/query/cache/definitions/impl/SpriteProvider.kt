package com.query.cache.definitions.impl

import com.query.cache.Sprite
import com.displee.compress.decompress
import com.query.Application
import com.query.Constants
import com.query.cache.Loader
import com.query.cache.Serializable
import com.query.cache.definitions.Definition
import com.query.cache.download.CacheLoader
import com.query.dump.DefinitionsTypes
import com.query.utils.IndexType
import com.query.utils.index
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch


data class SpriteDefinition(
    override var id: Int,
    var sprite: BufferedImage
) : Definition

class SpriteProvider(val latch: CountDownLatch?, val writeTypes : Boolean = true) : Loader, Runnable {

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
                val sprite = Sprite.decode(ByteBuffer.wrap(sector.decompress()))
                for (frame in 0 until sprite.size()) {
                    sprites.add(SpriteDefinition(it.id,sprite.getFrame(frame)!!))
                }
            }
        }
        return Serializable(DefinitionsTypes.SPRITES,this, sprites,writeTypes)
    }




}

fun main() {

    CacheLoader.initialize()
    SpriteProvider(null,false).run()
}