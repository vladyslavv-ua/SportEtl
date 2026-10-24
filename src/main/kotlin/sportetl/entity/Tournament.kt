package io.vladyslavvua.sportetl.entity

import org.jetbrains.exposed.v1.core.Table

object Tournament: Table("tournament") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    override val primaryKey = PrimaryKey(id)
}