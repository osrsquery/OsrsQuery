package com.query

import javax.swing.JOptionPane
import javax.swing.LookAndFeel
import javax.swing.SwingUtilities
import javax.swing.UIManager

object ThemeManager {

    val packages = listOf(
        "com.formdev.flatlaf.intellijthemes.",
        "com.formdev.flatlaf.intellijthemes.materialthemeuilite.",
        "com.formdev.flatlaf.",
        "com.formdev.flatlaf.themes."
    )

    fun switchToTheme(text : String, onLoad : Boolean = false) {
        Application.appSettings.settings.theme = text

        val theme = findTheme(text)
        if (theme.isNotEmpty()) {
            val themeClassName = "${theme}$text"
            val themeClass = Class.forName(themeClassName)
            val look = themeClass.getDeclaredConstructor().newInstance() as LookAndFeel
            UIManager.setLookAndFeel(look)
            SwingUtilities.updateComponentTreeUI(Application.frame)
            if (!onLoad) {
                JOptionPane.showMessageDialog(Application.frame, "Theme Applied: $themeClassName")
                Application.appSettings.settings.theme = text
                Application.appSettings.save()
            }
        }
    }


    fun findTheme(text: String): String {
        var validTheme = ""



        for (pkg in packages) {
            try {
                val themeClass = Class.forName("$pkg$text")
                val theme = themeClass.getDeclaredConstructor().newInstance() as? LookAndFeel
                if (theme != null) {
                    validTheme = pkg
                    break // Stop searching if a valid theme is found
                }
            } catch (e: ClassNotFoundException) {

            }
        }

        return validTheme
    }

}