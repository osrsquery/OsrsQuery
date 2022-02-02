package com.query.dump.impl

import SpriteData
import com.query.Application
import com.query.Application.objects
import com.query.Constants.library
import com.query.dump.TypeManager
import com.query.utils.FileUtils.getFile
import com.query.utils.IndexType
import com.query.utils.progress
import java.nio.ByteBuffer
import javax.imageio.ImageIO





class MapScene : TypeManager() {

    override fun load() {
        writeImage()
    }

    override fun onTest() {
        if(objects() == null) {
            //ObjectLoader(null,false).run()
        }
        if(Application.sprites() == null) {
            //SpriteLoader(null,false).run()
        }
        writeImage()
    }


    fun getIdentifier(file: Int, frame: Int): Int {
        return file shl 16 or (frame and 0xFFFF)
    }


    private fun writeImage() {
        val progress = progress("Writing Area Sprites",  objects()!!.filter { it.mapSceneID != -1 }.distinctBy { it.mapSceneID }.size.toLong())
        objects()!!.filter { it.mapSceneID != -1 }.distinctBy { it.mapSceneID }.forEachIndexed { index, objects ->
            try {
                val container: ByteArray = library.data(IndexType.SPRITES.number, 317)!!
                val sprite = SpriteData.decode(ByteBuffer.wrap(container))
                val img = sprite.getFrame(objects.mapSceneID)
                val outputfile = getFile("mapsSences/","${index}.png")
                ImageIO.write(img, "png", outputfile)
                progress.step()
            }catch (e : Exception) {
                progress.step()
            }
        }
        progress.close()

    }

    companion object {
        @JvmStatic
        fun main(args : Array<String>) {
            MapScene().test()
        }
    }

}