package com.query.dump.dumper317

import com.query.Application
import com.query.utils.FileUtils
import com.query.utils.progress
import java.io.DataOutputStream
import java.io.FileOutputStream


object NpcDumper {

    fun init() {
        val progress = progress("Writing Npcs", Application.npcs().size)

        val dat = DataOutputStream(FileOutputStream(FileUtils.getFile("dump317/configs/","npc.dat")))
        val idx = DataOutputStream(FileOutputStream(FileUtils.getFile("dump317/configs/","npc.idx")))

        dat.writeShort(Application.npcs().size)
        idx.writeShort(Application.npcs().size)

        Application.npcs().forEach {
            val start = dat.size()

            it.encode(dat)

            val end = dat.size()
            idx.writeShort(end - start)
            progress.step()
        }


    }

}