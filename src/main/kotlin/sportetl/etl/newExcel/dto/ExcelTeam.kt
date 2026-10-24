package io.vladyslavvua.sportetl.etl.newExcel.dto

import org.jetbrains.kotlinx.dataframe.annotations.DataSchema

@DataSchema
interface ExcelTeam {
    val Confed: String
    val Federation: String?
    val Team: String
    val TeamID: String
}