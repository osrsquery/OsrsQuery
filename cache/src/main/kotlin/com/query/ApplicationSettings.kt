package com.query

import com.beust.klaxon.Klaxon
import com.query.openrs2.CacheInfo
import com.query.utils.timedLoad
import java.io.File

data class Settings(
    var theme: String = "FlatDarkLaf",
    var saveLocation: String = "",
    var installed: MutableList<String> = mutableListOf(),
    var filterExistingCaches: Boolean = false
)

object ApplicationSettings {
    private val SAVE_LOCATION = File(System.getProperty("user.home"), "runescapeQuerySetting.json")
    private val APP_SAVE = File(System.getProperty("user.home"), "rsQuery")
    var settings: Settings = Settings()

    // Save application settings to a JSON file
    fun save() {
        timedLoad("Application Settings Saved") {
            if (!SAVE_LOCATION.exists()) {
                SAVE_LOCATION.createNewFile()
            }
            SAVE_LOCATION.writeText(Klaxon().toJsonString(settings))
        }
    }

    // Get the base save location, either from settings or a default location
    fun getBaseSaveLocation(): File {
        if (settings.saveLocation.isEmpty()) {
            if (!APP_SAVE.exists()) {
                APP_SAVE.mkdirs()
            }
            return APP_SAVE
        }
        return File(settings.saveLocation)
    }

    // Find cache folder based on CacheInfo
    fun findCache(cache: CacheInfo): File {
        val cacheFolder = File(getBaseSaveLocation(), "/${cache.game}/${cache.builds.first().major}")
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs()
        }
        return cacheFolder
    }

    // Find cache folder based on a File object (no operation, just returns the input file)
    fun findCache(cache: File): File {
        return cache
    }

    // Load application settings from a JSON file
    fun load() {
        timedLoad("Application Settings Loaded") {
            if (SAVE_LOCATION.exists()) {
                settings = Klaxon().parse<Settings>(SAVE_LOCATION.readText()) ?: Settings()
            }
        }
    }
}