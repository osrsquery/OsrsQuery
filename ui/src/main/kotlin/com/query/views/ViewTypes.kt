package com.query.views

import java.awt.Dimension

enum class ViewTypes(val size : Dimension, val resizable : Boolean) {
    CACHE_SELECTOR(Dimension(417, 490),false),
    DASH(Dimension(1406, 842),true)
}