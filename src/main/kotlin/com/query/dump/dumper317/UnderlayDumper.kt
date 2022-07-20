package com.query.dump.dumper317

import com.query.Application.underlays
import com.query.utils.FileUtils.getDir
import com.query.utils.progress
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream


object UnderlayDumper {

    fun init() {
        val dat = DataOutputStream(FileOutputStream(File(getDir("/dump317/"), "flo.dat")))

        val underlays = underlays()

        dat.writeShort(underlays.size)

        val pb = progress("Dumping 317 Underlay",underlays.size.toLong())
        underlays.forEach {
            it.encode(dat)
            pb.step()
        }
        pb.close()

    }
}
