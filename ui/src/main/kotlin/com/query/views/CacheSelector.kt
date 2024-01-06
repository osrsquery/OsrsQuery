package com.query.views

import com.query.Application
import com.query.ApplicationSettings
import com.query.openrs2.CacheInfo
import com.query.openrs2.CacheManager
import com.query.openrs2.DownloadWorker
import com.query.utils.ImageUtils
import com.query.utils.formatBytes
import java.awt.*
import java.awt.event.*
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.border.EmptyBorder


class CacheSelector : JPanel() {

    private var recentlyUsedPanel = JPanel(BorderLayout())
    private var downloadOSRSPanel = JPanel(BorderLayout())
    private var downloadRS2Panel = JPanel(BorderLayout())

    private var cacheDownloading = false
    private val progressBar = JProgressBar()
    private val ICON_BOX_OSRS: Icon = ImageIcon(ImageUtils.loadIconImage("icon_box_osrs.png"))
    private val ICON_BOX_RS2: Icon = ImageIcon(ImageUtils.loadIconImage("icon_box_rs2.png"))
    private val tabbedPane = JTabbedPane()

    init {
        layout = BorderLayout()

        recentlyUsedPanel = createTabContent("Recently Used")
        downloadOSRSPanel = createTabContent("Download OSRS")
        downloadRS2Panel = createTabContent("Download RS2")

        tabbedPane.addTab("Recently Used", recentlyUsedPanel)
        tabbedPane.addTab("Download OSRS", downloadOSRSPanel)
        tabbedPane.addTab("Download RS2", downloadRS2Panel)

        add(tabbedPane, BorderLayout.CENTER)
        add(createBottomPanel(), BorderLayout.SOUTH)
    }

    private fun createTabContent(content: String): JPanel {
        val panel = JPanel(BorderLayout())
        val scrollPane = JScrollPane(populateCaches(content))
        scrollPane.preferredSize = Dimension(498, 372)
        panel.add(scrollPane, BorderLayout.CENTER)
        return panel
    }

    private fun populateCaches(content: String): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        val data = when (content) {
            "Recently Used" -> CacheManager.getInstalled()
            "Download OSRS" -> CacheManager.getOldSchoolCaches()
            else -> CacheManager.getRS2()
        }

        val installedMode = content == "Recently Used"
        val filterExistingCaches = !installedMode && ApplicationSettings.settings.filterExistingCaches

        val filteredData = if (filterExistingCaches) {
            val installedCaches = CacheManager.getInstalled()
            data.filter { cache ->
                !installedCaches.any { it.getRev() == cache.getRev() && it.game == cache.game }
            }
        } else {
            data
        }

        if (filteredData.isEmpty()) {
            panel.add(errorPane("No Caches Found"))
        }

        filteredData.forEach { cache ->
            panel.add(createCacheContainer(cache, installedMode))
        }

        return panel
    }

    private fun errorPane(message : String) : JPanel {
        val panel = JPanel()
        panel.layout = GridBagLayout()
        val constraints = GridBagConstraints()

        val label = JLabel(message)

        constraints.gridx = 0
        constraints.gridy = 0
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.insets = Insets(10, 10, 10, 10) // Optional: Add padding

        panel.add(label, constraints)
        return panel
    }

    private fun updateTab(title : String) {
        val recentlyUsedIndex = tabbedPane.indexOfTab(title)

        if (recentlyUsedIndex != -1) {
            val updatedPanel = createTabContent(title)
            tabbedPane.setComponentAt(recentlyUsedIndex, updatedPanel)
        }
    }

    private fun createCacheContainer(info: CacheInfo, existingCache: Boolean): JPanel {
        val transparentBorder = EmptyBorder(2, 2, 2, 2)

        val contentPanel = JPanel().apply {
            layout = null
            border = transparentBorder
            preferredSize = Dimension(388, 90)
        }

        val timeStamp = info.timestamp?.let { "<br> Release: ${iso8601ToReadable(it)}" } ?: ""
        val infoLabel = JLabel("<html><body style='margin:0; padding:0;'><div style='vertical-align:top;'>Size: ${formatBytes(info.size)}<br> Language: ${info.language}$timeStamp</div></body></html>").apply {
            bounds = Rectangle(78, -15, 237, 86)
        }
        contentPanel.add(infoLabel)

        val buttonText = if (existingCache) "Open" else "Download"
        val installButton = JButton(buttonText).apply {
            bounds = Rectangle(291, 61, 92, 22)
            addActionListener(InstallButtonActionListener(info, existingCache))
        }
        contentPanel.add(installButton)

        val rev = JLabel("${info.builds.first().major}").apply {
            bounds = Rectangle(3, 69, 69, 18)
            horizontalAlignment = SwingConstants.CENTER
            verticalAlignment = SwingConstants.CENTER
        }
        contentPanel.add(rev)

        var iconImg = if (info.game == "oldschool") ICON_BOX_OSRS else ICON_BOX_RS2

        if (existingCache) {
            val iconLoc = File(ApplicationSettings.findCache(info),"icon.png")
            if (iconLoc.exists()) {
                try {
                    iconImg = ImageIcon(ImageIO.read(iconLoc))
                }catch (e : Exception){}
            }
        }

        val icon = JLabel().apply {
            icon = iconImg
            bounds = Rectangle(2, 2, 71, 86)
        }
        contentPanel.add(icon)

        contentPanel.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) {
                contentPanel.border = BorderFactory.createLineBorder(Color.decode("#4C87C8"), 1)
            }

            override fun mouseExited(e: MouseEvent) {
                contentPanel.border = transparentBorder
            }
        })

        return contentPanel
    }

    private inner class InstallButtonActionListener(private val info: CacheInfo, private val existingCache: Boolean) : ActionListener {
        override fun actionPerformed(e: ActionEvent) {
            if (existingCache) {
                Application.setView(ViewTypes.DASH)
            } else {
                if (cacheDownloading) return
                cacheDownloading = true
                progressBar.isVisible = true
                val worker = DownloadWorker(info, ApplicationSettings.findCache(info), onProgress = {
                    progressBar.value = it.toInt()
                }, onComplete = {
                    progressBar.isVisible = false
                    if (!CacheManager.getInstalled().contains(info)) {
                        ApplicationSettings.settings.installed.add(ApplicationSettings.findCache(info).absolutePath + "/")
                        CacheManager.localCaches.add(info)
                    }
                    ApplicationSettings.save()
                    cacheDownloading = false
                    updateTab("Recently Used")
                })
                worker.execute()
            }
        }
    }

    private fun iso8601ToReadable(iso8601Timestamp: String, zoneId: ZoneId = ZoneId.of("UTC")): String {
        val instant = Instant.parse(iso8601Timestamp)
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH).withZone(zoneId)
        return formatter.format(instant)
    }

    private fun createBottomPanel(): JPanel {
        val bottomPanel = JPanel(BorderLayout())

        progressBar.isStringPainted = true
        progressBar.value = 0
        bottomPanel.add(progressBar, BorderLayout.NORTH)

        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        val button1 = JButton("Load Custom")
        val checkBox = JCheckBox("Hide Installed Caches")
        checkBox.setSelected(ApplicationSettings.settings.filterExistingCaches)
        checkBox.addItemListener { e: ItemEvent ->
            if (e.stateChange == ItemEvent.SELECTED) {
                ApplicationSettings.settings.filterExistingCaches = true
                updateTab(if (tabbedPane.selectedIndex == 1) "Download OSRS" else "Download RS2")
            } else {
                ApplicationSettings.settings.filterExistingCaches = false
                updateTab(if (tabbedPane.selectedIndex == 1) "Download OSRS" else "Download RS2")
            }
        }
        buttonPanel.add(button1)
        buttonPanel.add(checkBox)
        bottomPanel.add(buttonPanel, BorderLayout.CENTER)

        return bottomPanel
    }
}