package com.query.dump.dumper317

import com.query.Application
import com.query.utils.FileUtils
import com.query.utils.progress
import java.io.DataOutputStream
import java.io.FileOutputStream

object KitDumper {

    fun init() {
        val progress = progress("Writing Identity Kit", Application.areas().size)

        val dat = DataOutputStream(FileOutputStream(FileUtils.getFile("dump317/configs/","idk.dat")))

        dat.writeShort(Application.kits().size)

        Application.kits().forEach {
            it.encode(dat)
            progress.step()
        }


    }

}