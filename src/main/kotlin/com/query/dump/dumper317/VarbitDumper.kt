package com.query.dump.dumper317

import com.query.Application.overlays
import com.query.Application.varbits
import com.query.utils.FileUtils.getDir
import com.query.utils.progress
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream


object VarbitDumper {

    fun init() {
        val dat = DataOutputStream(FileOutputStream(File(getDir("/dump317/"), "varbit.dat")))

        val varbits = varbits()

        dat.writeShort(varbits.size)

        val pb = progress("Dumping 317 Varbits",varbits.size.toLong())
        varbits.forEach {
            it.encode(dat)
            pb.step()
        }
        pb.close()

    }
}
