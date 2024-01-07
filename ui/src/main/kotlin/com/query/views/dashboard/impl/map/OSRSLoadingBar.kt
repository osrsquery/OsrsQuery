package com.query.views.dashboard.impl.map

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JComponent

class OSRSLoadingBar : JComponent() {
    private val borderColor = Color.RED
    private val barBackgroundColor = Color.BLACK
    private val barColor = Color.RED
    private val padding = 1
    private val barWidth = 304
    private val barHeight = 34
    private var progress = 0
    private var message = "Starting"

    fun setProgress(message : String, progress : Int) {
        this.message = message
        this.progress = progress
        repaint()
    }


    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2d = g.create() as Graphics2D

        // Draw black background
        g2d.color = barBackgroundColor
        g2d.fillRect(padding, padding, width - 2 * padding, height - 2 * padding)

        // Draw red border
        g2d.color = borderColor
        g2d.drawRect(0, 0, width - 1, height - 1)

        // Calculate the position and size of the red loading bar with padding
        val barX = padding + 1
        val barY = padding + 1
        val barWidth = (progress / 100.0 * (width - 2 * padding - 2)).toInt()
        val barHeight = height - 2 * padding - 2

        // Draw solid red loading bar
        g2d.color = barColor
        g2d.fillRect(barX, barY, barWidth, barHeight)

        // Draw text in the middle of the loading bar (centered vertically)
        val text = message
        g2d.color = Color.WHITE
        val fontMetrics = g2d.getFontMetrics()
        val textWidth = fontMetrics.stringWidth(text)
        val textHeight = fontMetrics.height
        val x = (width - textWidth) / 2
        val y = barY + (barHeight + textHeight) / 2 // Centered vertically within the progress bar
        g2d.drawString(text, x, y - 5)

        g2d.dispose()
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(barWidth, barHeight)
    }
}
