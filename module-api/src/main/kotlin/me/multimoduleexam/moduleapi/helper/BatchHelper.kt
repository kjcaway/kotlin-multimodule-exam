package me.multimoduleexam.moduleapi.helper

import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.PreparedStatement

object BatchHelper {

    fun executeBatchUpdate(
        jdbcTemplate: JdbcTemplate,
        sql: String,
        list: List<Any>,
        setter: (ps: PreparedStatement, i: Int) -> Unit
    ) {
        jdbcTemplate.batchUpdate(sql, object : BatchPreparedStatementSetter {
            override fun setValues(ps: PreparedStatement, i: Int) {
                setter(ps, i)
            }

            override fun getBatchSize() = list.size
        })
    }
}