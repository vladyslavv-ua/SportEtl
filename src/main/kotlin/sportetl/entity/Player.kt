package io.vladyslavvua.sportetl.entity

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.date

object Player: Table("player") {
    val id = integer("id").autoIncrement()
    val familyName = varchar("family_name", 100)
    val givenName = varchar("given_name", 100).nullable()
    val birthDate = date("birth_date").nullable()
    val sex = enumeration("sex", Sex::class)

    override val primaryKey = PrimaryKey(id)
}