package com.query.utils

import com.query.Constants
import java.io.File

object FileUtils {

    fun getDir(dir : String) : File {
        val file = File("${Constants.BASE_DIR}${dir}")
        if(!file.exists()) file.mkdirs()
        return file
    }

    fun getFile(dir: String,file : String) = File(getDir(dir),file)

}