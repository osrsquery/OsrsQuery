package com.query.dump.dumper317

import com.query.Application
import com.query.utils.FileUtils
import com.query.utils.progress
import java.io.DataOutputStream
import java.io.FileOutputStream


object ObjectDumper {

    fun init() {
        val progress = progress("Writing Objects", Application.objects().size)

        val dat = DataOutputStream(FileOutputStream(FileUtils.getFile("dump317/configs/","loc.dat")))
        val idx = DataOutputStream(FileOutputStream(FileUtils.getFile("dump317/configs/","loc.idx")))

        dat.writeShort(Application.objects().size)
        idx.writeShort(Application.objects().size)

        Application.objects().forEach {
            val start = dat.size()

            it.encode(dat)

            val end = dat.size()
            idx.writeShort(end - start)
            progress.step()
        }


    }

}