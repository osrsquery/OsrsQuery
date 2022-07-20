package com.query.dump.dumper317

import com.query.Application.areas
import com.query.Application.sequences
import com.query.utils.FileUtils.getDir
import com.query.utils.progress
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream


object SeqDumper {

    fun init() {
        val dat = DataOutputStream(FileOutputStream(File(getDir("/dump317/"), "seq.dat")))

        val seq = sequences()

        dat.writeShort(seq.size)

        val pb = progress("Dumping 317 Seq",seq.size.toLong())
        seq.forEach {
            it.encode(dat)
            pb.step()
        }
        pb.close()

    }
}

