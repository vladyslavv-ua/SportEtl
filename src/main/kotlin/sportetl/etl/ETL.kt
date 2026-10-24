package io.vladyslavvua.sportetl.etl

import org.jetbrains.kotlinx.dataframe.DataFrame
import java.io.File

interface ETL {
    fun extract(file: File)

    fun transform(dataFrame: DataFrame<*>)

    fun load()
    fun run(file: File)
}