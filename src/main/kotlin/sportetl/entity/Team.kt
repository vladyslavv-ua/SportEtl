package io.vladyslavvua.sportetl.entity

import org.jetbrains.exposed.v1.core.Table

object Team: Table("team") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)
    val confederationCode = varchar("confederation_code", length = 255)

    override val primaryKey = PrimaryKey(id)
}