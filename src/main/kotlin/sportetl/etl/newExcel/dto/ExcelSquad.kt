package io.vladyslavvua.sportetl.etl.newExcel.dto

import org.jetbrains.kotlinx.dataframe.annotations.DataSchema

@DataSchema
interface ExcelSquad {
    val PlayerID: Int
    val Pos: String?
    val TeamID: Int
    val Tournament: String
    val TournamentID: Int
}