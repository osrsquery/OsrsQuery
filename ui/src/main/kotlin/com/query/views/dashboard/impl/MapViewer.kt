package com.query.views.dashboard.impl

import com.query.views.dashboard.ContentView
import javax.swing.JLabel
import javax.swing.JPanel

class MapViewer : ContentView {

    override fun onOpen() {
        System.out.println("Open")
    }

    override fun onClose() {
        System.out.println("Close")
    }

    override fun contentPane(): JPanel {
        val pane = JPanel()
        pane.add(JLabel("CONTENT PANE"))
        return pane
    }

    override fun bottomPane(): JPanel {
        val pane = JPanel()
        pane.add(JLabel("PANE MAP"))
        return pane
    }

}