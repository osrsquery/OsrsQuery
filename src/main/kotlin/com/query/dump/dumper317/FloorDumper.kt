package com.query.dump.dumper317

import com.query.Application
import com.query.utils.FileUtils
import com.query.utils.progress
import java.io.DataOutputStream
import java.io.FileOutputStream

object FloorDumper {

    fun init() {
        val progress = progress("Writing Floors", Application.underlays().size + Application.overlays().size)


        val dat = DataOutputStream(FileOutputStream(FileUtils.getFile("dump317/configs/","flo.dat")))

        dat.writeShort(Application.underlays().size)

        Application.underlays().forEach {
            it.encode(dat)
            progress.step()
        }

        dat.writeShort(Application.overlays().size)

        Application.overlays().forEach {
            it.encode(dat)
            progress.step()
        }


    }

}