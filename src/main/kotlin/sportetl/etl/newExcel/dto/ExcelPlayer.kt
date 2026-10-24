package io.vladyslavvua.sportetl.etl.newExcel.dto

import org.jetbrains.kotlinx.dataframe.annotations.DataSchema


@DataSchema
interface ExcelPlayer {
    val DOB: String?
    val FullName: String
    val PlayerID: String
    val Sex: String
}