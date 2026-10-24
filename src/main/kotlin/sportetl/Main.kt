package io.vladyslavvua.sportetl

import io.vladyslavvua.sportetl.entity.*
import io.vladyslavvua.sportetl.etl.newExcel.NewExcelETL
import io.vladyslavvua.sportetl.etl.oldCsv.OldCsvETL
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.io.File


fun main() {
    Database.connect("jdbc:sqlite:./football.db")

    transaction {
        SchemaUtils.create(Tournament, Player, Team, Squad, PlayerSquad)
    }

    Thread.sleep(1000)

    println("Enter path to old csv file:")
    val oldCsvPath = readln()
//    val oldCsvPath = "/home/user/projects/polytech/data-engineering/sportetl/datasets/squads_archive.csv"


    println("Enter path to new excel file:")
    val newExcelPath = readln()
//    val newExcelPath = "/home/user/projects/polytech/data-engineering/sportetl/datasets/squads_new.xlsx"

    val oldCsvFile = File(oldCsvPath)
    val newExcelFile = File(newExcelPath)


    if (!oldCsvFile.exists() || !newExcelFile.exists()) {
        println("One or more files do not exist.")
        return
    }

    val oldCsvETL = OldCsvETL()
    val mapping = oldCsvETL.process(oldCsvFile)
//    val mapping = null

    val newExcelETL = NewExcelETL(mapping)
    newExcelETL.process(newExcelFile)
}