package com.query

import com.beust.klaxon.Klaxon
import com.query.utils.timedLoad
import java.io.File

data class Settings(var theme : String = "FlatDarkLaf", var saveLocation : String = "")

class ApplicationSettings {

    val SAVE_LOCATION = File(System.getProperty("user.home"),"runescapeQuerySetting.json")

    val APP_SAVE = File(System.getProperty("user.home"),"rsQuery")

    var settings : Settings = Settings()

    fun save() {
        timedLoad("Application Settings Saved") {
            if (!SAVE_LOCATION.exists()) {
                SAVE_LOCATION.createNewFile()
            }

            SAVE_LOCATION.writeText(Klaxon().toJsonString(settings))
        }

    }

    fun getBaseSaveLocation() : File {
        if (settings.saveLocation.isEmpty()) {
            if (!APP_SAVE.exists()) {
                APP_SAVE.mkdirs()
            }
            return APP_SAVE
        }
        return File(settings.saveLocation)
    }

    fun findCacheLocation(type: CacheTypes, rev : Int) : File {
        val cache = File(getBaseSaveLocation(),type.name + "rev")
        if (!cache.exists()) {
            cache.mkdirs()
        }
        return cache;
    }

    fun load() {
        timedLoad("Application Settings Loaded") {
            if (SAVE_LOCATION.exists()) {
                Klaxon().parse<Settings>(SAVE_LOCATION.readText())
            }
        }
    }

}