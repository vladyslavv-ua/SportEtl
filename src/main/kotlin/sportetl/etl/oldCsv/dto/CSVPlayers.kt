package io.vladyslavvua.sportetl.etl.oldCsv.dto

import org.jetbrains.kotlinx.dataframe.annotations.DataSchema

@DataSchema
data class CSVPlayers(
    val birth_date: String?,
    val family_name: String,
    val female: Boolean,
    val given_name: String,
    val player_id: String
)