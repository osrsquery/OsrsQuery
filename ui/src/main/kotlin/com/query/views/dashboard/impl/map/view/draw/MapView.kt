package world.gregs.voidps.tools.map.view.draw

import kotlinx.coroutines.*
import world.gregs.voidps.tools.map.view.draw.WorldMap.Companion.flipRegionY
import world.gregs.voidps.tools.map.view.graph.AreaSet
import world.gregs.voidps.tools.map.view.interact.MouseDrag
import world.gregs.voidps.tools.map.view.interact.MouseHover
import world.gregs.voidps.tools.map.view.interact.MouseZoom
import world.gregs.voidps.tools.map.view.interact.ResizeListener
import world.gregs.voidps.tools.map.view.ui.OptionsPane
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Graphics
import javax.swing.JPanel
import javax.swing.SwingUtilities

class MapView() : JPanel() {

    private val options = OptionsPane(this)
    private val highlight = HighlightedTile(this, options)


    private val drag = MouseDrag(this)
    private val zoom = MouseZoom(this, MouseZoom.ZoomType.Mouse)

    private val map = WorldMap(this)
    private val resize = ResizeListener(map)


    /*
        Offset from view 0, 0 to top left of world map
     */
    var offsetX = 0
    var offsetY = 0
    var level = 0
        private set

    /*
        View bounds
     */
    val minX = debugBorder
    val minY = debugBorder
    val maxX: Int
        get() = width - debugBorder
    val maxY: Int
        get() = height - debugBorder
    private val viewWidth: Int
        get() = width - debugBorder * 2
    private val viewHeight: Int
        get() = height - debugBorder * 2

    init {
        layout = FlowLayout(FlowLayout.LEFT)
        SwingUtilities.invokeLater {
            centreOn(3221, flipMapY(3218))
            options.updatePosition(3221, 3218, 0)
        }
//        addMouseListener(click)
        addMouseListener(drag)
        addMouseMotionListener(drag)
        addMouseWheelListener(zoom)

        addComponentListener(resize)
        add(options)

        repaint()
    }

    val scale: Int
        get() = zoom.scale

    fun updateZoom(x: Int, y: Int) {
        highlight.update(x, y)

    }

    fun updateLevel(level: Int) {
        if (this.level != level) {
            this.level = level
            highlight.update()
            repaint()
        }
    }

    fun getCentreX() = minX + viewWidth / 2

    fun getCentreY() = minY + viewHeight / 2

    fun mapToViewX(mapX: Int) = imageToViewX(mapToImageX(mapX))

    fun mapToViewY(mapY: Int) = imageToViewY(mapToImageY(mapY))

    fun viewToMapX(viewX: Int) = imageToMapX(viewToImageX(viewX))

    fun viewToMapY(viewY: Int) = imageToMapY(viewToImageY(viewY))

    fun viewToRegionX(viewX: Int) = mapToRegionX(viewToMapX(viewX))

    fun viewToRegionY(viewY: Int) = mapToRegionY(viewToMapY(viewY))

    fun regionToImageX(regionX: Int) = mapToImageX(regionToMapX(regionX))

    fun regionToImageY(regionY: Int) = mapToImageY(regionToMapY(regionY))

    fun regionToViewX(regionX: Int) = imageToViewX(regionToImageX(regionX))

    fun regionToViewY(regionY: Int) = imageToViewY(regionToImageY(regionY))

    fun imageToMapX(imageX: Int) = imageX / scale

    fun imageToMapY(imageY: Int) = imageY / scale

    fun mapToImageX(mapX: Int) = mapX * scale

    fun mapToImageY(mapY: Int) = mapY * scale

    fun viewToImageX(viewX: Int) = viewX - offsetX

    fun viewToImageY(viewY: Int) = viewY - offsetY

    fun imageToViewX(imageX: Int) = imageX + offsetX

    fun imageToViewY(imageY: Int) = imageY + offsetY

    fun mapToRegionX(mapX: Int) = mapX / 64

    fun mapToRegionY(mapY: Int) = mapY / 64

    fun regionToMapX(regionX: Int) = regionX * 64

    fun regionToMapY(regionY: Int) = regionY * 64

    fun flipMapY(mapY: Int) = regionToMapX(flipRegionY(mapToRegionY(mapY) - 1)) - (mapY % 64)

    fun resetDrag() {
//        lc.reset()
    }

    fun drag(mouseX: Int, mouseY: Int, mapStartX: Int, mapStartY: Int, offsetX: Int, offsetY: Int) {

        val node = null//nav.nodes.firstOrNull { it is Tile && it.id == Tile.getId(mapStartX, flipMapY(mapStartY), level) }
        when {
            node != null -> {
//                lc.update(mapStartX, mapStartY, mouseX, mouseY)
                highlight.update(mouseX, mouseY)
            }

            else -> {
                this.offsetX = offsetX
                this.offsetY = offsetY
                update(mouseX, mouseY)
            }
        }
    }

    /**
     * Repaint a single region
     */
    fun repaintRegion(regionX: Int, regionY: Int) {
        val viewX = regionToViewX(regionX)
        val viewY = regionToViewY(flipRegionY(regionY))
        repaint(viewX, viewY, regionToImageX(1), regionToImageY(1))
    }

    /**
     * Move [mapY], [mapY] to the center of the view
     * @param mapY Flipped y map coordinate
     */
    fun centreOn(mapX: Int, mapY: Int, level: Int = this.level) = align(width / 2, height / 2, mapX, mapY, level)

    /**
     * Aligns the [mapX], [mapY] with a position in the view [viewX], [viewY]
     */
    fun align(viewX: Int, viewY: Int, mapX: Int, mapY: Int, level: Int = this.level) {
        offsetX = viewX - mapToImageX(mapX)
        offsetY = viewY - mapToImageY(mapY)
        this.level = level
        update()
    }

    fun offset(mapX: Int, mapY: Int, level: Int = 0) {
        offsetX += mapToImageX(mapX)
        offsetY += mapToImageY(mapY)
        this.level += level
        update()
    }

    /**
     * Full repaint due to view change
     */
    fun update() {
        map.updateView()
        repaint()
    }

    /**
     * Full repaint due to view change
     */
    fun update(viewX: Int, viewY: Int) {
        map.updateView()
        highlight.update(viewX, viewY)
        repaint()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        g.color = Color.BLACK
        g.fillRect(0, 0, width, height)
        map.draw(g)
        highlight.draw(g)

        if (debugBorder > 0) {
            g.color = Color.RED
            g.drawRect(minX, minY, viewWidth, viewHeight)
            g.drawRect(getCentreX(), getCentreY(), 1, 1)
            g.color = Color.BLACK
        }
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(1280, 768)
    }

    companion object {
        private const val debugBorder = 0
    }
}