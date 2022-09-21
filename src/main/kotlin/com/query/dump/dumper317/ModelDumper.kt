package com.query.dump.dumper317

import com.displee.compress.decompress
import com.query.Constants
import com.query.cache.download.CacheLoader
import com.query.utils.*

object ModelDumper {

    fun init() {
        val table = Constants.library.index(IndexType.MODELS)
        val models = table.archives().filter { table.readArchiveSector(it.id) != null }
        val progress = progress("Dumping Models", models.size)
        models.forEach {
            val location =  FileUtils.getFile("dump317/index1/", "${it.id}.gz")
            gzip(location, table.readArchiveSector(it.id)!!.decompress())
            progress.step()
        }
        progress.close()

    }

}

fun main(){
    CacheLoader.initialize()
    ModelDumper.init()
}