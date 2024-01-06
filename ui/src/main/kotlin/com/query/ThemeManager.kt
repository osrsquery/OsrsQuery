package com.query

import javax.swing.JOptionPane
import javax.swing.LookAndFeel
import javax.swing.SwingUtilities
import javax.swing.UIManager

object ThemeManager {

    private val validPackages = listOf(
        "com.formdev.flatlaf.intellijthemes.",
        "com.formdev.flatlaf.intellijthemes.materialthemeuilite.",
        "com.formdev.flatlaf.",
        "com.formdev.flatlaf.themes."
    )

    fun switchToTheme(text: String, onLoad: Boolean = false) {
        val themeClassName = findThemeClassName(text)

        if (themeClassName.isNotEmpty()) {
            applyTheme(themeClassName, onLoad)
        }
    }

    private fun findThemeClassName(text: String): String {
        for (pkg in validPackages) {
            try {
                val themeClass = Class.forName("$pkg$text")
                val theme = themeClass.getDeclaredConstructor().newInstance() as? LookAndFeel
                if (theme != null) {
                    return "$pkg$text"
                }
            } catch (e: ClassNotFoundException) {
                // Continue searching if the theme is not found in this package
            }
        }

        return ""
    }

    private fun applyTheme(themeClassName: String, onLoad: Boolean) {
        UIManager.setLookAndFeel(themeClassName)
        SwingUtilities.updateComponentTreeUI(Application.frame)

        if (!onLoad) {
            JOptionPane.showMessageDialog(Application.frame, "Theme Applied: $themeClassName")
            ApplicationSettings.settings.theme = themeClassName
            ApplicationSettings.save()
        }
    }
}