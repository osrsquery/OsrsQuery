package com.query.dump.dumper317

import com.query.Application
import com.query.utils.FileUtils
import com.query.utils.progress
import java.io.DataOutputStream
import java.io.FileOutputStream

object AreaDumper {

    fun init() {
        val progress = progress("Writing Areas", Application.areas().size)

        val dat = DataOutputStream(FileOutputStream(FileUtils.getFile("dump317/configs/","areas.dat")))

        dat.writeShort(Application.underlays().size)

        var count = 0
        Application.areas().forEach {
            it.encode(dat,count)
            if (it.spriteId != -1) {
                count += 1
            }
            progress.step()
        }


    }

}