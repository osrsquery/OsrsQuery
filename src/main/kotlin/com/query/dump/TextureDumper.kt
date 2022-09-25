package com.query.dump

import com.query.Application
import com.query.utils.FileUtil
import com.query.utils.progress

object TextureDumper {

    val sprites317 = mutableMapOf<Int,Int>()

    fun init() {
        val progress = progress("Writing Textures", Application.textures().size.toLong())
        FileUtil.getDir("textures/").delete()
        var index = 0
        Application.textures().forEach {
            if(!sprites317.contains(it.fileIds[0])) {
                sprites317.putIfAbsent(it.fileIds[0],index)
                FileUtil.getFile("sprites/pink/", "${it.fileIds[0]}.png").copyTo(FileUtil.getFile("textures/","${it.fileIds[0]}.png"),true)
                FileUtil.getFile("sprites/pink/", "${it.fileIds[0]}.png").copyTo(FileUtil.getFile("dump317/textures/","${index}.png"),true)
                index++
            }
            progress.step()
        }
        progress.close()

    }

}