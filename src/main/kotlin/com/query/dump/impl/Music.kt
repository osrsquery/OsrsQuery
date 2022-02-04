package com.query.dump.impl

import com.beust.klaxon.Klaxon
import com.query.Application.music
import com.query.dump.DefinitionsTypes
import com.query.dump.TypeManager
import com.query.utils.progress
import com.query.Application
import com.query.Constants
import com.query.cache.definitions.impl.MusicData
import com.query.utils.FileUtils.getFile
import com.query.utils.Position
import com.query.utils.getInt
import com.query.utils.writeJson
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import org.apache.commons.lang.StringUtils
import java.net.URL

class Music : TypeManager {

    private val musicData = mapOf(
        0 to MusicDef(
            duration = "03:10",
            hint = "This track was unlocked automatically",
            release  = "11 April 2005",
            id  = 0,
            quest  = "no",
            composer  = "Ian Taylor",
            name  = "Scape Main Original",
            map = listOf(
                MapArea(
                    bottomLeft = Position(1600,5632),
                    topLeft = Position(1600,5760),
                    topRight = Position(1728,5760),
                    bottomRight = Position(1728,5632)
                )
            ).toMutableList()
        ),
        30 to MusicDef(
            duration = "05:19",
            hint = "This track unocks at the cave under the Kharazi Jungle.",
            release  = "15 March 2005",
            id  = 30,
            quest = "Legends' Quest",
            composer  = "Ian Taylor",
            name  = "Scape Main Original",
            map = listOf(
                MapArea(
                    bottomLeft = Position(2368,4672),
                    topLeft = Position(2368,4736),
                    topRight = Position(2432,4736),
                    bottomRight = Position(2432,4672)
                ),
                MapArea(
                    bottomLeft = Position(2880,9280),
                    topLeft = Position(2880,9344),
                    topRight = Position(2944,9344),
                    bottomRight = Position(2944,9280)
                )
            ).toMutableList()
        ),
        87 to MusicDef(
            duration = "02:17",
            hint = "This track unlocks at White Wolf Mountain.",
            release  = "25 March 2004",
            id  = 87,
            quest = "No",
            composer  = "Ian Taylor",
            name  = "Ice Melody Original",
            map = listOf(
                MapArea(
                    bottomLeft = Position(2816,3456),
                    topLeft = Position(2816,3520),
                    topRight = Position(2880,3520),
                    bottomRight = Position(2880,3456)
                ),
                MapArea(
                    bottomLeft = Position(2880,9280),
                    topLeft = Position(2880,9344),
                    topRight = Position(2944,9344),
                    bottomRight = Position(2944,9280)
                )
            ).toMutableList()
        ),
        441 to MusicDef(
            duration = "06:36",
            id  = 441,
            name  = "Sire",
        ),
    ).toMutableMap()

    override val requiredDefs = listOf(DefinitionsTypes.MUSIC)
    private val pages = listOf(
        "https://oldschool.runescape.wiki/w/Special:Ask/format%3Djson/link%3Dall/headers%3Dshow/searchlabel%3DJSON/class%3Dsortable-20wikitable-20smwtable/sort%3D/order%3Dasc/offset%3D0/limit%3D500/-5B-5BCategory:Music-20tracks-5D-5D/-3FMusic-20ID/mainlabel%3D/prettyprint%3Dtrue/unescape%3Dtrue",
        "https://oldschool.runescape.wiki/w/Special:Ask/format%3Djson/link%3Dall/headers%3Dshow/searchlabel%3DJSON/class%3Dsortable-20wikitable-20smwtable/sort%3D/order%3Dasc/offset%3D500/limit%3D500/-5B-5BCategory:Music-20tracks-5D-5D/-3FMusic-20ID/mainlabel%3D/prettyprint%3Dtrue/unescape%3Dtrue"
    )

    private val brokenMusic = emptyList<String>().toMutableList()

    override fun load() {
        writeMusic()
    }

    override fun onTest() {
        writeMusic()
    }

    private fun writeMusic() {
        loadMusicLinks()
        val progress = progress("Writing Music", music().size.toLong())
        music().forEach {
            val dest = getFile("/sounds/music/","${name(it.id)}.midi")
            dest.writeBytes(it.midi)
            progress.step()
        }
        progress.close()

        writeJson(getFile("/sounds/","music-complete.json"),musicData)

    }

    private fun name(id : Int) : String {
        if(!musicData.contains(id)) {
            return "unknown_${id}"
        }
        if(musicData[id]!!.name == "Unknown") {
            return "unknown_${id}"
        }
        return musicData[id]!!.name.dropLastWhile { it.isWhitespace() }
    }

    private fun loadMusicLinks() {
        val musicData = MusicData()
        pages.forEach {
            val data = Klaxon().parse<MusicData>(URL(it).readText())
            musicData.results.putAll(data!!.results)
        }

        val progressBar = progress("Dumping Wiki Music",musicData.results.size.toLong())
        musicData.results.values.forEach {
            getMusicBox(it.fullurl)
            progressBar.step()
        }

        progressBar.close()

    }

    private fun getMusicBox(name : String) {
        val request: Request = Request.Builder().url("https://oldschool.runescape.wiki/api.php?action=query&format=json&prop=revisions&rvprop=content&titles=${name.substringAfterLast("/")}").build()
        val responses : Response = Constants.client.newCall(request).execute()
        val infoBoxText = StringUtils.substringBetween(responses.body().string(),"\"*\":\"{{","}]}}}}")
        writeData(infoBoxText)
    }

    private fun writeData(data : String) {

        StringUtils.substringsBetween(data,"Infobox Music","'''").forEach {
            if(it.contains("Catacombs and Tombs")) return
            val def = MusicDef()
            def.id = it.getInt("|cacheid = ", listOf(" ","\\n"))
            def.name = StringUtils.substringBetween(it,"|name = ","\\n")
            def.quest = StringUtils.substringBetween(it,"|quest = ","\\n").replace("[[","").replace("]]","")
            def.hint = StringUtils.substringBetween(it,"|hint = ","\\n")
            if(StringUtils.substringBetween(it,"|composer = ","\\n") != null) {
                def.composer = StringUtils.substringBetween(it,"|composer = ","\\n")
            }
            def.duration = StringUtils.substringBetween(it,"|duration = ","\\n")

            if(it.contains("release")) {
                def.release = StringUtils.substringBetween(it,"|release = ","\\n").replace("[[","").replace("]] "," ").replace("]]","")
            }
            if(musicData.filter { data -> data.value.name == def.name }.isNotEmpty()) {
                def.name = "${def.name}_${def.id}"
            }

            musicData[def.id] = def
        }

    }

    data class MusicDef(
        var duration : String = "Unknown",
        var hint : String = "Unknown",
        var release : String  = "Unknown",
        var id : Int = -1,
        var quest : String = "Unknown",
        var height : Int = 0,
        var composer : String = "Unknown",
        var name : String = "Unknown",
        val map : MutableList<MapArea> = emptyList<MapArea>().toMutableList()
    )

    data class MapArea(
        var bottomLeft : Position = Position(0,0),
        var topLeft : Position = Position(0,0),
        var topRight : Position = Position(0,0),
        var bottomRight : Position = Position(0,0),
    )

    companion object {
        @JvmStatic
        fun main(args : Array<String>) {
            val parser = ArgParser("app")
            val rev by parser.option(ArgType.Int, description = "The revision you wish to dump").default(0)
            parser.parse(args)
            Application.revision = rev

            Music().test()

        }
    }

}