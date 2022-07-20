package com.query.dump.dumper317

object Dumper {

    fun dumpAll() {
        AreaDumper.init()
        KitDumper.init()
        MapSceneDumper.init()
        OverlayDumper.init()
        SpotAnimDumper.init()
        UnderlayDumper.init()
        VarbitDumper.init()
        SeqDumper.init()
    }

}