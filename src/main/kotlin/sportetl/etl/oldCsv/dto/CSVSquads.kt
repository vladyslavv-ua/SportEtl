package io.vladyslavvua.sportetl.etl.oldCsv.dto

import org.jetbrains.kotlinx.dataframe.annotations.DataSchema

@DataSchema
interface CSVSquads {
    val player_id: Int
    val position_name: String
    val team_id: Int
    val tournament_id: Int
    val tournament_name: String
}