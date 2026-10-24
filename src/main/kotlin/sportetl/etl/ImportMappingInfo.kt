package io.vladyslavvua.sportetl.etl

data class ImportMappingInfo(
    val playerIdMap: Map<String, Int>,
    val teamIdMap: Map<String, Int>,
    val tournamentIdMap: Map<String, Int>,
    val squadIdMap: Map<Pair<Int, Int>, Int>
)
