package io.vladyslavvua.sportetl.etl.oldCsv.dto

import org.jetbrains.kotlinx.dataframe.annotations.DataSchema

@DataSchema
interface OldCsvSchema {
    val birth_date: String?
    val confederation_code: String
    val family_name: String
    val federation_name: String
    val female: Int
    val given_name: String
    val player_id: String
    val position_name: String
    val region_name: String
    val shirt_number: Int
    val team_code: String
    val team_id: String
    val team_name: String
    val tournament_id: String
    val tournament_name: String
}