package com.query.dump.dumper317

import com.displee.cache.index.ReferenceTable
import com.displee.compress.decompress
import com.query.Constants
import com.query.cache.definitions.impl.SpriteDefinition
import com.query.utils.IndexType
import com.query.utils.index
import java.io.DataOutputStream
import java.io.FileOutputStream
import java.nio.file.FileStore


object ModelDumper {


    fun init() {




		val table = Constants.library.index(IndexType.MODELS)
		println("Model Size : ${table.archives().size}")
		for (i in 0 until table.archives().size) {
			val sector = table.readArchiveSector(i) ?: continue
			val data = sector.decompress()

		}


		//Cache(FileStore.open(Constants.CACHE_PATH)).use { cache ->
		//	val table: ReferenceTable = cache.getReferenceTable(7)
	//		for (i in 0 until table.capacity()) {
	//			if (table.getEntry(i) == null) continue
//				val container: Container = cache.read(7, i)
//				val bytes = ByteArray(container.getData().limit())
//				container.getData().get(bytes)
//				DataOutputStream(FileOutputStream(File(directory, "$i.dat"))).use { dos ->
//					dos.write(
//						bytes
//					)
//				}
//				val progress: Double = i.toDouble() / table.capacity() * 100
//				System.out.printf("%.2f%s\n", progress, "%")
//			}
//		}
	}

}