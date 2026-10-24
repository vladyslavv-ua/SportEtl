package io.vladyslavvua.sportetl.etl.newExcel.dto

import org.jetbrains.kotlinx.dataframe.annotations.DataSchema


@DataSchema
interface ExcelTournament {
    val Tournament: String
    val TournamentID: String
}