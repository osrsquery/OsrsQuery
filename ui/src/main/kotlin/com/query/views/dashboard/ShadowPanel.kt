package com.query.views.dashboard

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.geom.Area
import java.awt.geom.Rectangle2D
import javax.swing.JPanel

class ShadowPanel(private val contentPanel: JPanel) : JPanel() {
    var shadowColor: Color = Color(0, 0, 0, 100)
    var shadowSize: Int = 10

    init {
        layout = BorderLayout()
        add(contentPanel, BorderLayout.CENTER)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2d = g.create() as Graphics2D
        g2d.color = shadowColor

        val x = width - shadowSize
        val y = 0
        val width = shadowSize
        val height = height

        val shadowRect = Rectangle2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
        val area = Area(shadowRect)
        g2d.fill(area)
        g2d.dispose()
    }
}