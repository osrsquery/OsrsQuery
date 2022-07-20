package com.query.dump.dumper317

import com.query.Application
import com.query.Application.mapScene
import com.query.Application.overlays
import com.query.Application.varbits
import com.query.utils.FileUtils.getDir
import com.query.utils.progress
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream


object MapSceneDumper {

    fun init() {

        val dat = DataOutputStream(FileOutputStream(File(getDir("/dump317/"), "mapscene.dat")))
        val idx = DataOutputStream(FileOutputStream(File(getDir("/dump317/"), "mapscene.idx")))

        val mapscene = mapScene()

        idx.writeShort(mapscene.size)
        dat.writeShort(mapscene.size)

        val pb = progress("Dumping 317 Map Scene",mapscene.size.toLong())
        mapscene.forEach {
            val start = dat.size()

            it.encode(dat)
            dat.writeByte(0)

            val end = dat.size()
            idx.writeShort(end - start)

            pb.step()
        }
        pb.close()

        dat.writeShort(mapscene.size)


    }
}
