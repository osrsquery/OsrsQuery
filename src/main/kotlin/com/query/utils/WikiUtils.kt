package com.query.utils

import org.apache.commons.lang.StringUtils.substringBetween

fun getBoolean(text : String) : Boolean {
    if (text.contains("yes",true)) {
        return true
    }
    if (text.contains("no",true)) {
        return false
    }
    return false
}

fun getInt(text : String) = text.filter { it.isDigit() }.toInt()


fun String.getInt(open : String, close : List<String>, default : Int = -1) : Int {
    close.forEach { 
        try {
            return getInt(substringBetween(this,open, it))
        } catch (e : Exception) {}
    }
    return default
}
