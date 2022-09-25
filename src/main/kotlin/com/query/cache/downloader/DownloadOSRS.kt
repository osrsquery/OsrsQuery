package com.query.cache.downloader

import com.google.gson.Gson
import com.query.Application
import com.query.Application.gameType
import com.query.Application.revision
import com.query.Application.saveProperties
import com.query.Constants
import com.query.Constants.properties
import com.query.cache.CacheInfo
import com.query.cache.CacheManager
import com.query.cache.CacheManager.saveXteas
import com.query.utils.FileUtil
import com.query.utils.ZipUtils
import io.guthix.js5.*
import io.guthix.js5.container.Js5Container
import io.guthix.js5.container.Js5Store
import io.guthix.js5.container.disk.Js5DiskStore
import io.guthix.js5.container.net.Js5NetReadStore
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import me.tongfei.progressbar.DelegatingProgressBarConsumer
import me.tongfei.progressbar.ProgressBarBuilder
import me.tongfei.progressbar.ProgressBarStyle
import mu.KotlinLogging
import java.io.File
import java.io.IOException
import java.net.InetSocketAddress
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

object DownloadOSRS {

    val logger = KotlinLogging.logger {}

    fun init() {
        logger.info { "Finding Latest Revision" }

        if(revision == 0) {
            val caches = Gson().fromJson(URL(Constants.CACHE_DOWNLOAD_LOCATION).readText(), Array<CacheInfo>::class.java)
            val cacheInfo = CacheManager.getLatest(caches)
            revision = cacheInfo.builds.first().major
            if(!File(FileUtil.getCacheLocation(),"main_file_cache.dat2").exists()) {
                DownloadOpenRS2.downloadCache(cacheInfo)
                ZipUtils.unZip()
                saveXteas(cacheInfo.id)
                properties.setProperty("${gameType.getName()}-cache-version-${revision}", cacheInfo.timestamp)
                saveProperties(properties)
                File(FileUtil.getBase(),"/cache.zip").delete()
            }
        }

        var outputDir: Path = File(FileUtil.getBase(),"cache/").toPath()
        var address: String = "oldschool274.runescape.com"
        var includeVersions = true

        logger.info { "Downloading cache to $outputDir" }
        if (!Files.isDirectory(outputDir)) outputDir.toFile().mkdirs()
        val ds = Js5DiskStore.open(outputDir)
        val sr = Js5NetReadStore.open(
            sockAddr = InetSocketAddress(address, 43594),
            revision = Application.revision,
            priorityMode = false
        )
        logger.info { "Downloading validator" }
        val validator = Js5CacheValidator.decode(Js5Container.decode(
            sr.read(Js5Store.MASTER_INDEX, Js5Store.MASTER_INDEX)
        ).data, whirlpoolIncluded = false, sizeIncluded = false)
        logger.info { "Downloading archive settings" }
        val archiveCount = validator.archiveValidators.size
        val progressBarSettings = ProgressBarBuilder()
            .setInitialMax(archiveCount.toLong())
            .setTaskName("Downloader")
            .setStyle(ProgressBarStyle.ASCII)
            .setConsumer(DelegatingProgressBarConsumer(logger::info))
            .build()
        progressBarSettings.extraMessage = "Downloading settings"
        val settingsData = progressBarSettings.use { pb ->
            Array(archiveCount) {
                pb.step()
                sr.read(Js5Store.MASTER_INDEX, it)
            }
        }

        val archiveSettings = checkSettingsData(validator, settingsData)
        settingsData.mapIndexed { archiveId, data ->
            ds.write(Js5Store.MASTER_INDEX, archiveId, data)
        }
        val amountOfDownloads = archiveSettings.sumOf { it.groupSettings.keys.size }
        logger.info { "Downloading archives" }
        val readThread = Thread { // start thread that sends requests
            archiveSettings.forEachIndexed { archiveId, archiveSettings ->
                archiveSettings.groupSettings.forEach { (groupId, _) ->
                    sr.sendFileRequest(archiveId, groupId)
                    Thread.sleep(20) // requesting to fast makes the server close the connection
                }
            }
            logger.info("Done sending requests")
        }
        val writeThread = Thread { // start thread that reads requests
            val progressBarGroups = ProgressBarBuilder()
                .setInitialMax(amountOfDownloads.toLong())
                .setTaskName("Downloader")
                .setStyle(ProgressBarStyle.ASCII)
                .setConsumer(DelegatingProgressBarConsumer(logger::info))
                .build()
            progressBarGroups.use { pb ->
                archiveSettings.forEachIndexed { archiveId, archiveSettings ->
                    pb.extraMessage = "Downloading archive $archiveId"
                    archiveSettings.groupSettings.forEach { (_, groupSettings) ->
                        val response = sr.readFileResponse()
                        if (response.data.crc() != groupSettings.compressedCrc) throw IOException(
                            "Response index file ${response.indexFileId} container ${response.containerId} corrupted."
                        )
                        val writeData = if (groupSettings.version != -1 && includeVersions) { // add version if exists
                            val versionBuffer = Unpooled.buffer(2).apply { writeShort(groupSettings.version) }
                            Unpooled.compositeBuffer(2).addComponents(true, response.data, versionBuffer)
                        } else response.data
                        pb.step()
                        ds.write(archiveId, response.containerId, writeData)
                    }
                }
            }
            logger.info("Done writing responses")
        }
        readThread.start()
        writeThread.start()
        readThread.join()
        writeThread.join()
        ds.close()
        sr.close()
    }

    private fun checkSettingsData(
        readValidator: Js5CacheValidator,
        settingsData: Array<ByteBuf>
    ): MutableList<Js5ArchiveSettings> {
        val newFormat = readValidator.newFormat
        val containsWhirlool = readValidator.containsWhirlpool
        val archiveSettings = mutableListOf<Js5ArchiveSettings>()
        val archiveValidators = settingsData.map { data ->
            val settings = Js5ArchiveSettings.decode(
                Js5Container.decode(data)
            )
            archiveSettings.add(settings)
            val whirlpoolHash = if (containsWhirlool) data.whirlPoolHash() else null
            val fileCount = if (newFormat) settings.groupSettings.size else null
            val uncompressedSize = if (newFormat) settings.groupSettings.values.sumOf {
                it.sizes?.uncompressed ?: 0
            } else null
            data.readerIndex(0)
            Js5ArchiveValidator(data.crc(), settings.version ?: 0, whirlpoolHash, fileCount, uncompressedSize)
        }.toTypedArray()
        val calcValidator = Js5CacheValidator(archiveValidators)
        if (readValidator != calcValidator) throw IOException(
            "Checksum does not match, archive settings are corrupted."
        )
        return archiveSettings
    }

}
