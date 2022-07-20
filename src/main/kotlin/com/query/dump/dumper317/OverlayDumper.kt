package com.query.dump.dumper317

import com.query.Application.overlays
import com.query.utils.FileUtils.getDir
import com.query.utils.progress
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream


object OverlayDumper {

    fun init() {
        val dat = DataOutputStream(FileOutputStream(File(getDir("/dump317/"), "flo2.dat")))

        val overlays = overlays()

        dat.writeShort(overlays.size)

        val pb = progress("Dumping 317 Overlays",overlays.size.toLong())
        overlays.forEach {
            it.encode(dat)
            pb.step()
        }
        pb.close()

    }
}
