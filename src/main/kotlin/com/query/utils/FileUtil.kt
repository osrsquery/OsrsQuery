package com.query.utils

import com.query.Application.BASE_DIR
import com.query.Application.gameType
import com.query.Application.revision
import java.io.File


object FileUtil {

    fun getDir(dir : String) : File {
        val file = File(getBase(),"/${dir}")
        if(!file.exists()) file.mkdirs()
        return file
    }

    fun getBase() : File {
        val file = File("${BASE_DIR}/${gameType.getName()}/${revision}")
        if(!file.exists()) file.mkdirs()
        return file
    }

    fun getCacheLocation() = File(getBase() ,"cache/")

    fun getFile(dir: String,file : String) = File(getDir(dir),file)

}