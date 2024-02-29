package com.query.utils

import com.beust.klaxon.Klaxon

fun Any.jsonToString(prettyPrint: Boolean): String{
    var thisJsonString = Klaxon().toJsonString(this)
    var result = thisJsonString
    if(prettyPrint) {
        if(thisJsonString.startsWith("[")){
            result = Klaxon().parseJsonArray(thisJsonString.reader()).toJsonString(true)
        } else {
            result = Klaxon().parseJsonObject(thisJsonString.reader()).toJsonString(true)
        }
    }
    return result
}