package com.query.dump.dumper317

import com.displee.compress.decompress
import com.query.Constants
import com.query.utils.*
import com.query.utils.FileUtils.getFile

object ModelDumper {


    fun init() {
		val table = Constants.library.index(IndexType.MODELS)
		val progress = progress("Dumping Models", table.archives().size.toLong())
		for (index in 0 until table.archives().size) {
			val sector = table.readArchiveSector(index) ?: continue
			val data = sector.decompress()
			gzip(getFile("cache317/index1/","$index.gz"),data)
			progress.step()
		}
		progress.close()

	}

}