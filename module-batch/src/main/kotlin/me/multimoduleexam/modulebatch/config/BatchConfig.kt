package me.multimoduleexam.modulebatch.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class BatchConfig (
    @Autowired val dataSource: DataSource
) {


}