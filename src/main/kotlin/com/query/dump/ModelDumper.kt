package com.query.dump

import com.displee.compress.decompress
import com.query.Constants
import com.query.utils.*

object ModelDumper {

    fun init() {
        val table = Constants.library.index(IndexType.MODELS)
        val models = table.archives().filter { table.readArchiveSector(it.id) != null }
        val progress = progress("Dumping Models", models.size)
        models.forEach {
            val location =  FileUtil.getFile("dump317/index1/", "${it.id}.gz")
            gzip(location, table.readArchiveSector(it.id)!!.decompress())
            progress.step()
        }
        progress.close()

    }

    fun initRaw() {
        val table = Constants.library.index(IndexType.MODELS)
        val models = table.archives().filter { table.readArchiveSector(it.id) != null }
        val progress = progress("Dumping Models", models.size)
        models.forEach {
            val location =  FileUtil.getFile("dump317/index1/", "${it.id}.gz")
            gzip(location, table.readArchiveSector(it.id)!!.decompress())
            progress.step()
        }
        progress.close()

    }

}
