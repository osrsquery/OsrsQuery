package com.query.dump.dumper317

import com.query.Application
import com.query.utils.FileUtils
import com.query.utils.progress
import java.io.DataOutputStream
import java.io.FileOutputStream


object ItemDumper {

    fun init() {
        val progress = progress("Writing Items", Application.items().size)

        val dat = DataOutputStream(FileOutputStream(FileUtils.getFile("dump317/configs/","obj.dat")))
        val idx = DataOutputStream(FileOutputStream(FileUtils.getFile("dump317/configs/","obj.idx")))

        dat.writeShort(Application.items().size)
        idx.writeShort(Application.items().size)

        Application.items().forEach {
            val start = dat.size()

            it.encode(dat)

            val end = dat.size()
            idx.writeShort(end - start)
            progress.step()
        }


    }

}