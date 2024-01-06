package com.query

import com.query.openrs2.CacheManager
import com.query.utils.ImageUtils
import com.query.views.CacheSelector
import com.query.views.dashboard.Dashboard
import com.query.views.SplashScreen
import com.query.views.ViewTypes
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.WindowConstants.EXIT_ON_CLOSE
import kotlin.system.exitProcess


object Application {

    private val splashScreen = SplashScreen()
    lateinit var frame: JFrame
    private lateinit var startScreen: CacheSelector
    private lateinit var dash: Dashboard
    private var currentView: ViewTypes = ViewTypes.CACHE_SELECTOR

    fun init() {
        // Create and display the splash screen while initializing
        splashScreen.showSplashScreen()

        ApplicationSettings.load()
        CacheManager.init()
        frame = createMainFrame()
        startScreen = CacheSelector()
        dash = Dashboard()

        frame.add(JPanel(), BorderLayout.NORTH)
        setView(ViewTypes.CACHE_SELECTOR)
        // Close the splash screen when the main window is ready
        splashScreen.closeSplashScreen()

        frame.isVisible = true
    }

    fun setView(type : ViewTypes) {
        val view = when (type) {
            ViewTypes.CACHE_SELECTOR -> startScreen
            ViewTypes.DASH -> dash
            else -> startScreen
        }

        frame.contentPane = view
        frame.size = type.size
        val screenSize: Dimension = Toolkit.getDefaultToolkit().getScreenSize()
        val screenWidth = screenSize.width
        val screenHeight = screenSize.height
        val frameWidth = frame.width
        val frameHeight = frame.height

        val centerX = (screenWidth - frameWidth) / 2
        val centerY = (screenHeight - frameHeight) / 2
        // Set the frame's location to the center of the screen
        frame.setLocation(centerX, centerY)
        frame.isResizable = type.resizable
        frame.repaint()
        frame.revalidate()
    }

    private fun createMainFrame(): JFrame {
        frame = JFrame("RSQuery")
        ThemeManager.switchToTheme(ApplicationSettings.settings.theme, true)
        val iconImage = ImageUtils.loadIconImage("icon.png")
        frame.iconImage = iconImage
        frame.defaultCloseOperation = EXIT_ON_CLOSE

        frame.jMenuBar = createMenuBar()

        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                ApplicationSettings.save()
                exitProcess(0)
            }
        })

        return frame
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
    Application.init()
}