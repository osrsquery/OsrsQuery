package com.query

import javax.swing.JMenu
import javax.swing.JMenuItem

abstract class MenuOptionData(
    open val text: String,
    open val subItems: List<MenuOptionData> = emptyList(),
    open var action: () -> Unit = {}
)

data class MenuOption(
    override val text: String,
    override val subItems: List<MenuOptionData> = emptyList(),
    override var action: () -> Unit = {}
) : MenuOptionData(text, subItems, action)

data class MenuOptionTheme(
    override val text: String,
    override val subItems: List<MenuOptionData> = emptyList(),
) : MenuOptionData(text, subItems) {
    init {
        action = { ThemeManager.switchToTheme(text.replace(" ", "")) }
    }
}

fun MenuOption.withSubItems(vararg items: MenuOptionData): MenuOption {
    return this.copy(subItems = items.toList())
}

fun MenuOptionTheme.withSubItems(vararg items: MenuOptionData): MenuOptionTheme {
    return this.copy(subItems = items.toList())
}

fun createMenu(name: String, menuOptions: List<MenuOptionData>): JMenu {
    val menu = JMenu(name)
    createMenuParent(menu, menuOptions)
    return menu
}

fun createMenuParent(menu: JMenu, menuOptions: List<MenuOptionData>) {
    for (menuItem in menuOptions) {
        if (menuItem.subItems.isNotEmpty()) {
            val submenu = createMenu("", menuItem.subItems)
            submenu.text = menuItem.text
            submenu.addActionListener { menuItem.action.invoke() }
            menu.add(submenu)
        } else {
            val menuItemComponent = JMenuItem(menuItem.text)
            menuItemComponent.addActionListener { menuItem.action.invoke() }
            menu.add(menuItemComponent)
        }
    }
}


enum class MenuItemEnum(
    val menuName : String,
    vararg val menuItems: MenuOptionData
) {
    SETTINGS("Settings",
        MenuOption("Edit Save Location")
    ),
    THEMES("Themes",
        MenuOption("Dark").withSubItems(
            MenuOptionTheme("Flat Dark Laf"),
            MenuOptionTheme("Flat IntelliJ Laf"),
            MenuOptionTheme("Flat Darcula Laf"),
            MenuOptionTheme("Flat Mac Dark Laf")
        ),
        MenuOption("Light").withSubItems(
            MenuOptionTheme("Flat Light Laf"),
            MenuOptionTheme("Flat Mac Light Laf")
        ),
        MenuOption("Extras").withSubItems(
            MenuOption("Material Themes").withSubItems(
                MenuOptionTheme("FlatArcDarkIJTheme"),
                MenuOptionTheme("FlatAtomOneDarkIJTheme"),
                MenuOptionTheme("FlatAtomOneLightIJTheme"),
                MenuOptionTheme("FlatDraculaIJTheme"),
                MenuOptionTheme("FlatGitHubIJTheme"),
                MenuOptionTheme("FlatGitHubDarkIJTheme"),
                MenuOptionTheme("FlatLightOwlIJTheme"),
                MenuOptionTheme("FlatMaterialDarkerIJTheme"),
                MenuOptionTheme("FlatMaterialDeepOceanIJTheme"),
                MenuOptionTheme("FlatMaterialLighterIJTheme"),
                MenuOptionTheme("FlatMaterialOceanicIJTheme"),
                MenuOptionTheme("FlatMaterialPalenightIJTheme"),
                MenuOptionTheme("FlatMonokaiProIJTheme"),
                MenuOptionTheme("FlatMoonlightIJTheme"),
                MenuOptionTheme("FlatSolarizedDarkIJTheme"),
                MenuOptionTheme("FlatSolarizedLightIJTheme")
            ),
            MenuOption("Others").withSubItems(
                MenuOptionTheme("FlatArcIJTheme"),
                MenuOptionTheme("FlatArcOrangeIJTheme"),
                MenuOptionTheme("FlatArcDarkIJTheme"),
                MenuOptionTheme("FlatArcDarkOrangeIJTheme"),
                MenuOptionTheme("FlatCarbonIJTheme"),
                MenuOptionTheme("FlatCobalt2IJTheme"),
                MenuOptionTheme("FlatCyanLightIJTheme"),
                MenuOptionTheme("FlatDarkFlatIJTheme"),
                MenuOptionTheme("FlatDarkPurpleIJTheme"),
                MenuOptionTheme("FlatDraculaIJTheme"),
                MenuOptionTheme("FlatGradiantoDarkFuchsiaIJTheme"),
                MenuOptionTheme("FlatGradiantoDeepOceanIJTheme"),
                MenuOptionTheme("FlatGradiantoMidnightBlueIJTheme"),
                MenuOptionTheme("FlatGradiantoNatureGreenIJTheme"),
                MenuOptionTheme("FlatGrayIJTheme"),
                MenuOptionTheme("FlatGruvboxDarkHardIJTheme"),
                MenuOptionTheme("FlatGruvboxDarkMediumIJTheme"),
                MenuOptionTheme("FlatGruvboxDarkSoftIJTheme"),
                MenuOptionTheme("FlatHiberbeeDarkIJTheme"),
                MenuOptionTheme("FlatHighContrastIJTheme"),
                MenuOptionTheme("FlatLightFlatIJTheme"),
                MenuOptionTheme("FlatMaterialDesignDarkIJTheme"),
                MenuOptionTheme("FlatMonocaiIJTheme"),
                MenuOptionTheme("FlatMonokaiProIJTheme"),
                MenuOptionTheme("FlatNordIJTheme"),
                MenuOptionTheme("FlatOneDarkIJTheme"),
                MenuOptionTheme("FlatSolarizedDarkIJTheme"),
                MenuOptionTheme("FlatSolarizedLightIJTheme"),
                MenuOptionTheme("FlatSpacegrayIJTheme"),
                MenuOptionTheme("FlatVuesionIJTheme"),
                MenuOptionTheme("FlatXcodeDarkIJTheme")
            )
        )
    );

}