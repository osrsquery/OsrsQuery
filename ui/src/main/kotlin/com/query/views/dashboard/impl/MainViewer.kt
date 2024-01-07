package com.query.views.dashboard.impl

import com.query.ApplicationSettings
import com.query.ApplicationSettings.findCache
import com.query.cache
import com.query.cache.CacheDelegate
import com.query.currentCache
import com.query.views.dashboard.ContentView
import javax.swing.JLabel
import javax.swing.JPanel

class MainViewer : ContentView {

    override fun onOpen() {

        System.out.println("Cache: ${ApplicationSettings.findCache(currentCache!!).absolutePath + "./cache/"}")
        cache = CacheDelegate(ApplicationSettings.findCache(currentCache!!).absolutePath + "/cache/")

    }

    override fun onClose() {
        System.out.println("Close")
    }

    override fun contentPane(): JPanel {
        val pane = JPanel()
        pane.add(JLabel("CONTENT PANE Other"))
        return pane
    }

    override fun bottomPane(): JPanel {
        val pane = JPanel()
        pane.add(JLabel("Other View"))
        return pane
    }

}