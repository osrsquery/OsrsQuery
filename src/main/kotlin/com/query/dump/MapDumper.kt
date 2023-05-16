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
        val progress = progress("Generating Map Index", 255 * 255)

        val raf = RandomAccessFile(FileUtil.getFile("dump317/","map_index").toPath().toString(), "rw")
        var total = 0
        raf.seek(2L)


        for (x in 0..255) {
            for (y in 0..255) {
                val regionId = x shl 8 or y
                val index = Constants.library.index(5)

                val map = index.archiveId("m${x}_${y}")
                val landscape = index.archiveId("l${x}_${y}")

                if (map != -1 && landscape != -1) {
                    raf.writeShort(regionId)
                    raf.writeShort(map)
                    raf.writeShort(landscape)
                    total++
                }
                progress.step()
            }
        }

        progress.close()

        raf.seek(0L)
        raf.writeShort(total)
        raf.seek(raf.filePointer)
        raf.close()
        val mapProgress = progress("Dumping Maps", RegionLoader.MAX_REGION)

        val failedMaps = emptyMap<Int,Pair<Int,Int>>().toMutableMap()
        val missingKeys = listOf<Int>().toMutableList()

        for (x in 0..255) {
            for (y in 0..255) {
                val regionId = getRegion(x,y)
                val index = Constants.library.index(5)
                val mapID = index.archiveId("m${x}_${y}")
                val landscapeID = index.archiveId("l${x}_${y}")

                if (mapID != -1) {
                    val mapData: ByteArray? = Constants.library.data(5, "m${x}_${y}")
                    if (mapData != null) {
                        gzip(FileUtil.getFile("dump317/index4/", "${mapID}.gz"),mapData)
                    } else {
                        failedMaps[regionId] = Pair(mapID,landscapeID)
                    }
                }

                if (landscapeID != -1) {
                    val keys = XteaLoader.getKeys(regionId)
                    if (keys != null) {
                        val landscapeData: ByteArray? = Constants.library.data(5, "l${x}_${y}", keys)
                        if (landscapeData != null) {
                            gzip(FileUtil.getFile("dump317/index4/", "${mapID}.gz"), landscapeData)
                        } else {
                            failedMaps[regionId] = Pair(mapID, landscapeID)
                        }
                    } else {
                        missingKeys.add(regionId)
                    }
                }

                mapProgress.step()
            }
        }

        mapProgress.close()

        if (missingKeys.isNotEmpty()) {
            logger.info { "Missing Keys ${missingKeys.size}${System.lineSeparator()}$missingKeys" }
        }

        if (failedMaps.isNotEmpty()) {
            logger.info { "Failed Maps ${failedMaps.size}${System.lineSeparator()}$failedMaps" }
        }

    }

}

fun main() {
    CacheManager.initialize()
    MapDumper.init()
}