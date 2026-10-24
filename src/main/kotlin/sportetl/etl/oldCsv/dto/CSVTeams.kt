package io.vladyslavvua.sportetl.etl.oldCsv.dto

import org.jetbrains.kotlinx.dataframe.annotations.DataSchema

@DataSchema
interface CSVTeams {
    val federation_name: String
    val team_id: String
    val team_name: String
    val confederation_code: String
}