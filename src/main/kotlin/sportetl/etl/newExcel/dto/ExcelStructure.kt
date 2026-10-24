package io.vladyslavvua.sportetl.etl.newExcel.dto

import org.jetbrains.kotlinx.dataframe.annotations.DataSchema


@DataSchema
interface ExcelStructure {
    val Confed: String
    val DOB: String?
    val Federation: String?
    val FullName: String
    val PlayerID: String
    val Pos: String?
    val Sex: String
    val ShirtNo: String
    val Team: String
    val TeamID: String
    val Tournament: String
    val TournamentID: String
}