package me.multimoduleexam.util

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag(value = "unit")
class DateNewUtilTest {

    @Test
    fun `test_all_dateutil_method`(){
        println("DateNewUtil.getNowTimeStr(): ${DateNewUtil.getNowTimeStr()}")
        println("DateNewUtil.getNowTimeStrKst(): ${DateNewUtil.getNowTimeStrKst()}")
        println("DateNewUtil.getNowTimeStrByFormat(\"yyyy-MM-dd HH:mm\"): ${DateNewUtil.getNowTimeStrByFormat("yyyy-MM-dd HH:mm")}")
        println("DateNewUtil.getNowTimeStrKstByFormat(\"yyyy-MM-dd HH:mm\"): ${DateNewUtil.getNowTimeStrKstByFormat("yyyy-MM-dd HH:mm")}")


    }
}