package com.query.dump.dumper317

import com.query.Application.kits
import com.query.Application.spotanimations
import com.query.utils.FileUtils.getDir
import com.query.utils.progress
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream


object SpotAnimDumper {

    fun init() {
        val dat = DataOutputStream(FileOutputStream(File(getDir("/dump317/"), "spotanim.dat")))

        val anims = spotanimations()

        dat.writeShort(anims.size)

        val pb = progress("Dumping 317 Spot Animations",anims.size.toLong())
        anims.forEach {
            it.encode(dat)
            pb.step()
        }
        pb.close()

    }
}
