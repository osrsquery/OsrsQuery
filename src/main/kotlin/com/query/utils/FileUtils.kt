package com.query.utils

import com.query.Application.cacheInfo
import com.query.Constants
import java.io.File

object FileUtils {

    fun getDir(dir : String) : File {
        val file = File(getBase(),"/${dir}")
        if(!file.exists()) file.mkdirs()
        return file
    }

    fun getBase() : File {
        val file = File("${Constants.BASE_DIR}/${cacheInfo.builds[0].major}")
        if(!file.exists()) file.mkdirs()
        return file
    }

    fun getCacheLocation() = File(getBase() ,"cache/osrs/cache/")

    fun getFile(dir: String,file : String) = File(getDir(dir),file)

}