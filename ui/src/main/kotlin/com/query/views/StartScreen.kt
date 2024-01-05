package com.query.views

import com.query.openrs2.CacheInfo
import com.query.openrs2.OpenRS2Manager
import com.query.utils.ImageUtils
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.AbstractBorder


class StartScreen : JPanel() {

    var ICON_BOX_OSRS : Icon

    init {

        ICON_BOX_OSRS = ImageIcon(ImageUtils.loadIconImage("icon_box_osrs.png"))


        // Create a JPanel to hold the entire content using BorderLayout
        this.layout = BorderLayout()


        // Create a JTabbedPane to manage the tabbed view
        val tabbedPane = JTabbedPane()


        // Create tabs with scrollable content
        val tab1Panel = createTabContent("Downloaded")
        val tab2Panel = createTabContent("Download OSRS")
        val tab3Panel = createTabContent("Download RS2")


        // Add tabs to the tabbedPane
        tabbedPane.addTab("Recently Used", tab1Panel)
        tabbedPane.addTab("Download OSRS", tab2Panel)
        tabbedPane.addTab("Download RS2", tab3Panel)
        tabbedPane.selectedIndex = 1

        // Add the tabbedPane to the contentPanel in the CENTER position
        add(tabbedPane, BorderLayout.CENTER)


        // Add the bottomPanel to the contentPanel in the SOUTH position
        add(createBottomPanel(), BorderLayout.SOUTH)

    }

    private fun createTabContent(content: String): JPanel {
        val panel = JPanel(BorderLayout())
        val scrollPane = JScrollPane(populateCaches(content))
        scrollPane.preferredSize = Dimension(401, 372)
        scrollPane.horizontalScrollBarPolicy = 31
        panel.add(scrollPane, BorderLayout.CENTER)
        return panel
    }

    private fun populateCaches(content: String): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS) // Set the layout to vertical

        when (content) {
            "Recently Used" -> {
                // Add your components for "Recently Used" here
            }

            "Download OSRS" -> {
                OpenRS2Manager.getOldSchoolCaches().forEach {
                    panel.add(createCacheContainer(it))
                }
            }

            "Download RS2" -> {
                OpenRS2Manager.getRS2().forEach {
                    panel.add(createCacheContainer(it))
                }
            }
        }

        return panel
    }


    private fun createCacheContainer(info: CacheInfo): JPanel {
        // Create a custom transparent border
        val transparentBorder = object : AbstractBorder() {
            override fun paintBorder(c: Component?, g: Graphics?, x: Int, y: Int, width: Int, height: Int) {
                // Do nothing to make the border transparent
            }

            override fun getBorderInsets(c: Component?): Insets {
                return Insets(2, 2, 2, 2) // No insets
            }
        }

        val contentPanel = JPanel()
        contentPanel.layout = BorderLayout()

        val textPanel = JPanel()
        textPanel.layout = GridLayout(3, 1) // 3 rows, 1 column
        textPanel.background = null

        val cacheLabel = JLabel("Rev: ${info.builds.first().major}", SwingConstants.LEFT)
        textPanel.add(cacheLabel)

        val otherLabelText = "Size: ${info.size}"
        val otherLabel = JLabel(otherLabelText, SwingConstants.LEFT)
        textPanel.add(otherLabel)

        val otherLabelText2 = "Language: ${info.language}"
        val otherLabel2 = JLabel(otherLabelText2, SwingConstants.LEFT)
        textPanel.add(otherLabel2)

        val buttonPanel = JPanel()
        buttonPanel.layout = FlowLayout(FlowLayout.RIGHT)

        val button3 = JButton("Install")

        //buttonPanel.add(button1)
        //buttonPanel.add(button2)
        buttonPanel.add(button3)
        contentPanel.add(textPanel, BorderLayout.NORTH)
        contentPanel.add(buttonPanel, BorderLayout.SOUTH)


        val mainPanel = JPanel()
        mainPanel.border = transparentBorder
        mainPanel.layout = BorderLayout()
        mainPanel.preferredSize = Dimension(390, 90)

        val buttons = arrayOf(button3)
        for (button in buttons) {
            button.addMouseListener(object : MouseAdapter() {
                override fun mouseEntered(e: MouseEvent) {
                    mainPanel.border = BorderFactory.createLineBorder(Color.GREEN, 2)
                }

                override fun mouseExited(e: MouseEvent) {
                    mainPanel.border = transparentBorder
                }
            })
        }


        mainPanel.add(contentPanel, BorderLayout.CENTER)

        // Add a mouse listener to change the border color when hovering
        mainPanel.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) {
                mainPanel.border = BorderFactory.createLineBorder(Color.GREEN, 2)
            }

            override fun mouseExited(e: MouseEvent) {
                mainPanel.border = transparentBorder
            }
        })

        return mainPanel

    }

    private fun createBottomPanel(): JPanel {

        val bottomPanel = JPanel(BorderLayout())

        val progressBar = JProgressBar()
        progressBar.isStringPainted = true
        progressBar.value = 50 // Set an initial value
        bottomPanel.add(progressBar, BorderLayout.NORTH)
        progressBar.isVisible = false
        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        val button1 = JButton("Load Custom")
        val button2 = JButton("Load Previous (190-OSRS)")
        buttonPanel.add(button1)
        buttonPanel.add(button2)
        bottomPanel.add(buttonPanel, BorderLayout.CENTER)
        return bottomPanel
    }

}