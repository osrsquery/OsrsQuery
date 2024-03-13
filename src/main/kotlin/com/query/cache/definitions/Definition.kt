package com.query.cache.definitions

import java.io.DataOutputStream

abstract class Definition(@Transient open val id: Int = -1) {

    abstract fun encode(dos: DataOutputStream)


}