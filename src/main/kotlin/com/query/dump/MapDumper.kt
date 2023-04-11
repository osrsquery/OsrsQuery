package com.query.dump

import com.query.Constants
import com.query.cache.CacheManager
import com.query.cache.XteaLoader
import com.query.game.map.region.RegionLoader
import com.query.utils.*
import mu.KotlinLogging
import java.io.FileOutputStream
import java.io.RandomAccessFile


private val logger = KotlinLogging.logger {}

object MapDumper {

    fun init() {
        val mapProgress = progress("Dumping Maps", RegionLoader.MAX_REGION)

        val missingKeys = listOf<Int>().toMutableList()
        val failed = listOf<Int>().toMutableList()

        val raf = RandomAccessFile(FileUtil.getFile("dump317/","map_index").toPath().toString(), "rw")
        val mapArchive = Constants.library.index(5)

        var total = 0
        raf.seek(2L)

        for (xLoc in 0..255) {
            for (yLoc in 0..255) {
                val regionID = xLoc shl 8 or yLoc

                val x = mapArchive.archiveId("m${xLoc}_${yLoc}")
                val y = mapArchive.archiveId("l${xLoc}_${yLoc}")

                if (x != -1 && y != -1) {
                    raf.writeShort(regionID)
                    raf.writeShort(x)
                    raf.writeShort(y)
                    total++
                }
            }
        }


        raf.seek(0L)
        raf.writeShort(total)
        raf.seek(raf.filePointer)
        raf.close()

        var mapCount = 0
        var landCount = 0

        for (regionID in 0 until RegionLoader.MAX_REGION) {
            val x = regionID shr 8
            val y = regionID and 0xFF

            val map = mapArchive.archiveId("m${x}_${y}")
            val land = mapArchive.archiveId("l${x}_${y}")

            if (map != -1) {
                try {
                    val objects = Constants.library.data(5, "m${x}_${y}")
                    gzip(FileUtil.getFile("dump317/index4/", "${map}.gz"), objects!!)
                    mapCount++
                } catch (ex: Exception) {
                    failed.add(regionID)
                    println(String.format("Failed to decrypt map: %d", map))
                }
            }
            if (land != -1) {
                try {
                    XteaLoader.getKeys(regionID).whenNonNull {
                        val objects = Constants.library.data(5, "l${x}_${y}",this)
                        gzip(FileUtil.getFile("dump317/index4/", "${land}.gz"), objects!!)
                    }.whenNull { missingKeys.add(regionID) }

                    landCount++
                } catch (ex: Exception) {
                    println(String.format("Failed to decrypt landscape: %d", land))
                }
            }
            mapProgress.step()
        }

        mapProgress.close()

        val totalCount = mapCount + landCount

        if (missingKeys.isNotEmpty()) {
            logger.info("Missing Keys ({}) : {}", missingKeys.size, missingKeys)
        }

        if (failed.isNotEmpty()) {
            logger.info("Failed to decrypt ({}) : {}", failed.size, failed)
        }

        logger.info("Dumped {} map count {} land count {} total count", mapCount, landCount, totalCount)

    }

}

fun main() {
    CacheManager.initialize()
    MapDumper.init()
}