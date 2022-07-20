package com.query.dump.dumper317

import com.query.Application.areas
import com.query.Application.kits
import com.query.utils.FileUtils.getDir
import com.query.utils.progress
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream


object AreaDumper {

    fun init() {
        val dat = DataOutputStream(FileOutputStream(File(getDir("/dump317/"), "areas.dat")))
        val idx = DataOutputStream(FileOutputStream(File(getDir("/dump317/"), "areas.idx")))


        val areas = areas()

        idx.writeShort(areas.size)
        dat.writeShort(areas.size)

        val pb = progress("Dumping 317 Areas",areas.size.toLong())
        areas.forEach {
            val start = dat.size()

            it.encode(dat)
            dat.writeByte(0)

            val end = dat.size()
            idx.writeShort(end - start)

            pb.step()
        }
        pb.close()

    }
}

