package com.query.dump

import com.query.Constants
import com.query.cache.XteaLoader
import com.query.cache.CacheManager
import com.query.game.map.region.RegionLoader
import com.query.utils.FileUtil
import com.query.utils.getRegion
import com.query.utils.gzip
import com.query.utils.progress
import mu.KotlinLogging
import java.io.RandomAccessFile
import java.nio.file.Files

private val logger = KotlinLogging.logger {}

object MapDumper {

    fun init() {
        val mapProgress = progress("Dumping Maps", RegionLoader.MAX_REGION)

        val missingKeys = listOf<Int>().toMutableList()

        val raf = RandomAccessFile(FileUtil.getFile("dump317/","map_index").toPath().toString(), "rw")

        raf.seek(2L)
        var totalMaps = 0
        for (x in 0..256) {
            for (y in 0..256) {
                val regionId = x shl 8 or y

                val index = Constants.library.index(5)
                val objectId = index.archiveId("m${x}_${y}")
                val landscapeId = index.archiveId("l${x}_${y}")

                if (objectId != -1 && landscapeId != -1) {
                    val objects = Constants.library.data(5, "m${x}_${y}") ?: continue
                    gzip(FileUtil.getFile("dump317/index4/", "${objectId}.gz"), objects)

                    val keys = XteaLoader.getKeys(regionId)

                    val landscape = Constants.library.data(5, "l${x}_${y}", keys)

                    if (landscape == null) {
                        missingKeys.add(regionId)
                        continue
                    }

                    gzip(FileUtil.getFile("dump317/index4/", "${landscapeId}.gz"), landscape)

                    raf.writeShort(regionId)
                    raf.writeShort(objectId)
                    raf.writeShort(landscapeId)
                    totalMaps++
                }
                mapProgress.step()
            }
        }

        raf.seek(0L)
        raf.writeShort(totalMaps)
        raf.seek(raf.filePointer)
        raf.close()
        mapProgress.close()

        if (missingKeys.isNotEmpty()) {
            logger.info { "Wrong Keys ${missingKeys.size}${System.lineSeparator()}$missingKeys" }
        }


    }

}

fun main() {
    CacheManager.initialize()
    MapDumper.init()
}