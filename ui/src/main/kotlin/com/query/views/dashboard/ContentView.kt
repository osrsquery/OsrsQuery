package com.query.views.dashboard

import javax.swing.JPanel

interface ContentView {

    abstract fun onOpen()

    abstract fun onClose()

    abstract fun contentPane() : JPanel

    abstract fun bottomPane() : JPanel

}