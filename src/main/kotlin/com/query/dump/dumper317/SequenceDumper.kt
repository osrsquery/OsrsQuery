package com.query.dump.dumper317

import com.query.Application
import com.query.utils.FileUtils
import com.query.utils.progress
import java.io.DataOutputStream
import java.io.FileOutputStream

object SequenceDumper {

    fun init() {
        val progress = progress("Writing Sequence", Application.areas().size)

        val dat = DataOutputStream(FileOutputStream(FileUtils.getFile("dump317/configs/","seq.dat")))

        dat.writeShort(Application.sequences().size)

        Application.sequences().forEach {
            it.encode(dat)
            progress.step()
        }


    }

}