package com.query.views.dashboard

import com.query.utils.ImageUtils
import com.query.views.dashboard.impl.MainViewer
import com.query.views.dashboard.impl.MapViewer
import java.awt.*
import javax.swing.*
import javax.swing.border.Border
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener


class Dashboard : JPanel() {

    val tabs : MutableList<TabItem> = emptyList<TabItem>().toMutableList()
    var tabJList = JList(tabs.toTypedArray())
    val contentPane = JPanel()
    var bottomPane = JPanel()
    var currentToolOpen : ContentView? = null

    init {

        layout = BorderLayout()


        val containerPanel = JPanel(BorderLayout())

        val sideNavPanel = JPanel(BorderLayout())
        sideNavPanel.background = Color.RED
        sideNavPanel.preferredSize = Dimension(140, 0)

        val contentAndBottomPanel = JPanel(BorderLayout())

        bottomPane = JPanel()
        val customBorder: Border = BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY)
        bottomPane.border = customBorder
        bottomPane.preferredSize = Dimension(0, 50)


        val label = JLabel("Content Pane")
        contentPane.add(label)

        Tools.values().forEach {
            tabs.add(TabItem(it.displayName, ImageIcon(ImageUtils.loadIconImage(it.icon))))
        }

        tabJList = JList(tabs.toTypedArray())
        tabJList.selectionMode = ListSelectionModel.SINGLE_SELECTION

        // Set a custom cell renderer for the JList
        tabJList.cellRenderer = TabListCellRenderer()
        tabJList.setSelectedIndex(0)
        // Add a ListSelectionListener to get the selected tab text
        tabJList.addListSelectionListener(ListSelectionListener { e: ListSelectionEvent ->
            if (!e.valueIsAdjusting) {
                val selectedTab = tabJList.selectedValue
                if (selectedTab != null) {
                    val selectedText = selectedTab.text
                    switchTool(Tools.values().first { it.displayName == selectedText })
                }
            }
        })

        // Add some components to the contentPane (e.g., labels, buttons, etc.)
        contentAndBottomPanel.add(contentPane, BorderLayout.CENTER)
        contentAndBottomPanel.add(bottomPane, BorderLayout.SOUTH)

        // Add the tabJList to the WEST of the sideNavPanel
        sideNavPanel.add(JScrollPane(tabJList), BorderLayout.CENTER)

        // Add the contentAndBottomPanel (with contentPane and bottomPane) to the CENTER of the container panel
        containerPanel.add(sideNavPanel, BorderLayout.WEST)
        containerPanel.add(contentAndBottomPanel, BorderLayout.CENTER)

        add(containerPanel)

        add(containerPanel)

        switchTool(Tools.MAP_VIEWER)
    }

    fun switchTool(tool : Tools) {
        if (currentToolOpen != null) {
            currentToolOpen!!.onClose()
        }
        val content = when(tool) {
            Tools.MAP_VIEWER -> MapViewer()
            Tools.HOME -> MainViewer()
        }
        content.onOpen()
        contentPane.removeAll()
        contentPane.add(content.contentPane())
        bottomPane.removeAll()
        bottomPane.add(content.contentPane())
        currentToolOpen = content
        repaint()
        revalidate()
    }

}


data class TabItem(val text: String, val icon: ImageIcon)

class TabListCellRenderer : DefaultListCellRenderer() {
    override fun getListCellRendererComponent(
        list: JList<*>,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        val label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
        if (value is TabItem) {
            label.text = value.text
            label.icon = value.icon
            label.border = BorderFactory.createEmptyBorder(5, 5, 5, 5) // Add padding
            label.font = label.font.deriveFont(Font.BOLD, 16f) // Increase font size
            label.verticalAlignment = SwingConstants.CENTER // Vertically center text and icon
        }
        return label
    }
}