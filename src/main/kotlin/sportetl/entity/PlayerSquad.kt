package io.vladyslavvua.sportetl.entity

import org.jetbrains.exposed.v1.core.Table

object PlayerSquad : Table("player_squad") {
    val id = integer("id").autoIncrement()
    val playerId = integer("player_id")
    val squadId = integer("squad_id")

    val position = enumeration("position", Position::class).nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        foreignKey(playerId to Player.id)
        foreignKey(squadId to Squad.id)
    }
}