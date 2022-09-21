package com.query.dump.dumper317

import com.query.Application
import com.query.utils.FileUtils
import com.query.utils.progress
import java.io.DataOutputStream
import java.io.FileOutputStream

object VarbitDumper {

    fun init() {
        val progress = progress("Writing Varbit", Application.varbits().size.toLong())


        val dat = DataOutputStream(FileOutputStream(FileUtils.getFile("dump317/configs/","varbit.dat")))

        dat.writeShort(Application.varbits().size)

        Application.varbits().forEach {
            it.encode(dat)
            progress.step()
        }

        dat.close()
        progress.close()
    }

}