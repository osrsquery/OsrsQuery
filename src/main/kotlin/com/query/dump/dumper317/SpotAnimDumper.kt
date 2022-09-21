package com.query.dump.dumper317

import com.query.Application
import com.query.utils.FileUtils
import com.query.utils.progress
import java.io.DataOutputStream
import java.io.FileOutputStream

object SpotAnimDumper {

    fun init() {
        val progress = progress("Writing Spotanims", Application.spotanimations().size)

        val dat = DataOutputStream(FileOutputStream(FileUtils.getFile("dump317/configs/","spotanim.dat")))

        dat.writeShort(Application.spotanimations().size)

        Application.spotanimations().forEach {
            it.encode(dat)
            progress.step()
        }


    }

}