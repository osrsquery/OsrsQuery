package com.query.dump.dumper317

import com.query.Application
import com.query.utils.FileUtils
import com.query.utils.progress
import java.io.DataOutputStream
import java.io.FileOutputStream

object TextureDumper {

    fun init() {
        val progress = progress("Writing Textures", Application.textures().size.toLong())

        val sprites317 = mutableMapOf<Int,Int>()
        var index = 0
        Application.textures().forEach {
            if(!sprites317.contains(it.fileIds[0])) {
                sprites317.putIfAbsent(it.fileIds[0],index)
                FileUtils.getFile("sprites/pink/", "${it.fileIds[0]}.png").copyTo(FileUtils.getFile("dump317/textures/","${index}.png"),true)
                index++
            }
            progress.step()
        }
        progress.close()
        encodeTo317(sprites317)
    }

    private fun encodeTo317(sprites317 : Map<Int,Int>) {

        val textures = Application.textures()
        val progress = progress("Writing Textures 317", Application.textures().size.toLong())

        val dat = DataOutputStream(FileOutputStream(FileUtils.getFile("dump317/configs/","textures.dat")))

        dat.writeShort(textures.size)

        textures.forEach {
            it.encode(dat, sprites317)
            progress.step()
        }

        dat.close()
        progress.close()
    }

}