package io.vladyslavvua.sportetl.etl.oldCsv.dto

import org.jetbrains.kotlinx.dataframe.annotations.DataSchema


@DataSchema
interface CSVTournaments {
    val tournament_id: String
    val tournament_name: String
}