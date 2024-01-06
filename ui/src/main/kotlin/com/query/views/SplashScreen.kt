package com.query.views

import java.awt.*
import java.awt.geom.Rectangle2D
import javax.swing.JPanel
import javax.swing.JWindow

class SplashScreen : JWindow() {

    init {
        size = Dimension(400, 200)

        // Center the splash screen on the screen
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val x = (screenSize.width - width) / 2
        val y = (screenSize.height - height) / 2
        setLocation(x, y)

        // Create a custom content panel with a background color and message
        val content = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                val g2d = g as Graphics2D
                g2d.color = Color.WHITE
                g2d.fill(Rectangle2D.Double(0.0, 0.0, width.toDouble(), height.toDouble()))

                // You can customize the splash screen message here
                val message = "Loading..."
                g2d.color = Color.BLACK
                g2d.font = Font("Arial", Font.BOLD, 20)
                val fm = g2d.fontMetrics
                val messageWidth = fm.stringWidth(message)
                val messageHeight = fm.height
                val messageX = (width - messageWidth) / 2
                val messageY = (height - messageHeight) / 2 + fm.ascent
                g2d.drawString(message, messageX, messageY)
            }
        }
        content.isOpaque = false
        content.background = Color(0, 0, 0, 0)
        content.preferredSize = size
        content.layout = BorderLayout()
        add(content)
    }

    fun showSplashScreen() {
        isVisible = true
    }

    fun closeSplashScreen() {
        isVisible = false
        dispose()
    }
}