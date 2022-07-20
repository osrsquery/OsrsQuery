package com.query.dump.dumper317

import com.query.Application.kits
import com.query.utils.FileUtils.getDir
import com.query.utils.progress
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream


object KitDumper {

    fun init() {
        val dat = DataOutputStream(FileOutputStream(File(getDir("/dump317/"), "idk.dat")))

        val kits = kits()

        dat.writeShort(kits.size)

        val pb = progress("Dumping 317 Kits",kits.size.toLong())
        kits.forEach {
            it.encode(dat)
            pb.step()
        }
        pb.close()

    }
}
