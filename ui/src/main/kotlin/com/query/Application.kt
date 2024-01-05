package com.query

import com.query.openrs2.OpenRS2Manager
import com.query.utils.ImageUtils
import com.query.views.StartScreen
import com.query.views.ViewTypes
import java.awt.BorderLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.WindowConstants.EXIT_ON_CLOSE
import kotlin.system.exitProcess


object Application {

    lateinit var frame: JFrame

    lateinit var startScreen: StartScreen
    var currentView: ViewTypes = ViewTypes.START

    var appSettings: ApplicationSettings = ApplicationSettings()

    fun init() {
        appSettings.load()
        OpenRS2Manager.init()
        frame = JFrame("RSQuery")
        ThemeManager.switchToTheme(appSettings.settings.theme,true)

        val iconImage = ImageUtils.loadIconImage("icon.png")
        frame.iconImage = iconImage

        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                appSettings.save()
                exitProcess(0)
            }
        })


        frame.defaultCloseOperation = EXIT_ON_CLOSE
        frame.setSize(417, 490)
        frame.isResizable = false

        frame.jMenuBar = createMenuBar()

        startScreen = StartScreen()

        val view = when (currentView) {
            ViewTypes.START -> startScreen
            else -> startScreen
        }


        frame.add(view, BorderLayout.NORTH);

        frame.isVisible = true
    }

    private fun createMenuBar(): JMenuBar {
        val menuBar = JMenuBar()

        // Create menu items using the enum and add them to the menu bar
        for (menuItemEnum in MenuItemEnum.values()) {
            val menu = createMenu(menuItemEnum.menuName, menuItemEnum.menuItems.toList())
            menu.text = menuItemEnum.menuName
            menuBar.add(menu)
        }

        return menuBar

    }


}


fun main() {
    SwingUtilities.invokeLater {
        Application.init()
    }
}