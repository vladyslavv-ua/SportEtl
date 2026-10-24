package io.vladyslavvua.sportetl.entity

import org.jetbrains.exposed.v1.core.Table

object Squad: Table("squad") {
    val id = integer("id").autoIncrement()
    val teamId = integer("team_id")
    val tournamentId = integer("tournament_id")
    val year = integer("year")

    override val primaryKey = PrimaryKey(id)

    init {
        foreignKey(teamId to Team.id)
        foreignKey(tournamentId to Tournament.id)
    }
}