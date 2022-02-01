package com.query.utils

import java.util.concurrent.TimeUnit

object TimeUtils {

    fun millsToFormat(mills : Long) : String = String.format("%d min, %d sec",
        TimeUnit.MILLISECONDS.toMinutes(mills),
        TimeUnit.MILLISECONDS.toSeconds(mills) -
        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mills))
    );

}