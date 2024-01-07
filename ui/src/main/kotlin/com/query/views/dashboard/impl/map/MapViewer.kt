package com.query.views.dashboard.impl.map

import com.query.map.render.WorldMapDumper
import com.query.views.dashboard.ContentView
import world.gregs.voidps.tools.map.view.draw.MapView
import javax.swing.*


class MapViewer : ContentView {


    private val loadingBar = OSRSLoadingBar()
    var mapDumper : WorldMapDumper? = null

    val contentPanel = JPanel()

    override fun onOpen() {
        contentPanel.add(JLabel("Press Start to Open World Map"))
        if (mapDumper == null) {
            mapDumper = WorldMapDumper(onProgress = {
                println(it)
                if (it.first == "Creating Maps") {
                    val currentValue = it.second.split(" / ")[0].toInt()
                    val minValue = 0
                    val maxValue = it.second.split(" / ")[1].toInt()
                    loadingBar.setProgress("${it.first} - ${it.second}",calculateProgress(currentValue, minValue, maxValue))
                } else {
                    val progress = it.second.toInt()
                    loadingBar.setProgress("${it.first} - $progress",progress)
                }
            })
        }
        mapDumper!!.init()
    }

    private fun calculateProgress(currentValue: Int, minValue: Int, maxValue: Int): Int {
        val progress = ((currentValue - minValue).toDouble() / (maxValue - minValue) * 100).toInt()
        return if (progress < 0) 0 else if (progress > 100) 100 else progress
    }


    override fun onClose() {
        System.out.println("Close")
    }

    override fun contentPane(): JPanel {
        return contentPanel
    }


    private fun addWorldMap() : JPanel {
        val panel = JPanel()
        panel.add(MapView())
        return panel
    }

    private fun addProgress() : JPanel {
        val panel = JPanel()
        panel.add(loadingBar)
        return panel
    }

    private fun swapContentPane(panel : JPanel) {
        contentPanel.removeAll()
        contentPanel.add(panel)
        contentPanel.repaint()
        contentPanel.revalidate()
    }

    override fun bottomPane(): JPanel {
        val pane = JPanel()
        val start = JButton("Start")
        start.addActionListener {
            swapContentPane(addProgress())
            Thread {
                mapDumper?.dumpMaps {
                    SwingUtilities.invokeLater {
                        swapContentPane(addWorldMap())
                    }
                }
            }.start()
        }
        pane.add(start)
        return pane
    }

}