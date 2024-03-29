package com.query.dump

import com.query.Constants
import com.query.cache.CacheManager
import com.query.cache.XteaLoader
import com.query.game.map.region.RegionLoader.Companion.MAX_REGION
import com.query.utils.*
import mu.KotlinLogging
import java.io.FileOutputStream
import java.io.RandomAccessFile


private val logger = KotlinLogging.logger {}

object MapDumper {

    fun init() {
        val mapArchive = Constants.library.index(5)
        val mapProgress = progress("Dumping Maps", MAX_REGION)
        val failed : MutableList<Int> = emptyArray<Int>().toMutableList()

        RandomAccessFile(FileUtil.getFile("dump317/","map_index").path, "rw").use { raf ->
            logger.info { "Generating map_index..." }

            var total = 0
            raf.seek(2L)

            var end: Int

            var mapCount = 0
            var landCount = 0

            for (end in 0 until 256) {
                for (i in 0 until 256) {
                    val var17 = end shl 8 or i
                    val x = mapArchive.archiveId( "m$end$i")
                    val y = mapArchive.archiveId( "l$end$i")

                    if (x != -1 && y != -1) {
                        raf.writeShort(var17.toInt())
                        raf.writeShort(x)
                        raf.writeShort(y)
                        total++
                    }
                }
            }

            end = raf.filePointer.toInt()
            raf.seek(0L)
            raf.writeShort(total)
            raf.seek(end.toLong())

            for (i in 0 until MAX_REGION) {
                val keys = XteaLoader.getKeys(i)
                val x = i shr 8 and 255
                val y = i and 255
                val map = mapArchive.archiveId( "m" + x + "_" + y)
                val land = mapArchive.archiveId("l" + x + "_" + y)


                if (map != -1) {
                    try {
                        val data = Constants.library.data(5, "m" + x + "_" + y)

                        FileOutputStream(FileUtil.getFile("dump317/index4/", "${map}.gz")).use { fos ->
                            fos.write(gzip(data))
                        }

                        mapCount++
                    } catch (ex: Exception) {
                        failed.add(map)
                    }
                }

                if (land != -1) {
                    try {
                        val data = Constants.library.data(5, "l" + x + "_" + y,keys)

                        FileOutputStream(FileUtil.getFile("dump317/index4/", "${land}.gz")).use { fos ->
                            fos.write(gzip(data))
                        }

                        landCount++
                    } catch (ex: Exception) {
                        failed.add(map)
                    }
                }

                mapProgress.step()
            }
            mapProgress.close()

            val totalCount = mapCount + landCount

            logger.info { "Dumped $mapCount map count $landCount land count $totalCount total count"}
            logger.info { "Missing Xteas: $failed"}
        }
    }

}

fun main() {
    CacheManager.initialize()
    MapDumper.init()
}