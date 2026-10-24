package io.vladyslavvua.sportetl.etl.oldCsv

import io.vladyslavvua.sportetl.entity.Player
import io.vladyslavvua.sportetl.entity.PlayerSquad
import io.vladyslavvua.sportetl.entity.Position
import io.vladyslavvua.sportetl.entity.Sex
import io.vladyslavvua.sportetl.entity.Squad
import io.vladyslavvua.sportetl.entity.Team
import io.vladyslavvua.sportetl.entity.Tournament
import io.vladyslavvua.sportetl.etl.ImportMappingInfo
import io.vladyslavvua.sportetl.etl.oldCsv.dto.*
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.io.readCsv
import java.io.File


class OldCsvETL {
    private val regex = Regex("\\d{4}-\\d{2}-\\d{2}")
    private fun extract(file: File): ImportMappingInfo {
        val df = DataFrame.readCsv(file = file)

        return transform(df.cast())
    }

    private fun transform(dataFrame: DataFrame<OldCsvSchema>): ImportMappingInfo {
        val transformedPlayers = dataFrame.select {
            player_id and family_name and given_name and birth_date and female
        }.distinctBy { player_id }.convert { female }.with { it == 1 }

        println("Transformed players")
        transformedPlayers.print()

        val playersIdMapping = loadPlayers(transformedPlayers.cast())

        val transformedTeams = dataFrame.select {
            team_id and team_name and federation_name and confederation_code
        }.distinct().sortBy { team_id }


        println("Transformed teams")
        transformedTeams.print()

        val teamsIdMapping = loadTeams(transformedTeams.cast())


        val transformedTournaments = dataFrame.select {
            tournament_id and tournament_name
        }.distinct().sortBy { tournament_id }


        println("Transformed tournaments")
        transformedTournaments.print()

        val tournamentsIdMapping = loadTournament(transformedTournaments.cast())


        val transformedSquads = dataFrame.select {
            player_id and team_id and position_name and tournament_id and tournament_name
        }
            .convert { player_id }.with { playersIdMapping[it]!! }
            .convert { team_id }.with { teamsIdMapping[it]!! }
            .convert { tournament_id }.with { tournamentsIdMapping[it]!! }

        println("Transformed squads")
        transformedSquads.print()


        val squadsIdMapping = loadSquad(transformedSquads.distinctBy { team_id and tournament_id }.cast())

        loadPlayerSquad(transformedSquads.distinctBy { player_id and tournament_id }.cast(), squadsIdMapping)

        return ImportMappingInfo(
            playerIdMap = playersIdMapping,
            teamIdMap = teamsIdMapping,
            tournamentIdMap = tournamentsIdMapping,
            squadIdMap = squadsIdMapping
        )

    }

    private fun loadPlayers(df: DataFrame<CSVPlayers>): Map<String, Int> {
        val playerIds = mutableMapOf<String, Int>()
        transaction {
            df.forEach { player ->
                val playerId = Player.insert {
                    it[Player.familyName] = player.family_name
                    it[Player.givenName] = prepareString(player.given_name)
                    it[Player.birthDate] = prepareDate(player.birth_date)
                    it[Player.sex] = if (player.female) Sex.FEMALE else Sex.MALE
                }[Player.id]

                playerIds[player.player_id] = playerId
            }

        }

        return playerIds

    }

    private fun loadTeams(df: DataFrame<CSVTeams>): Map<String, Int> {
        val teamIds = mutableMapOf<String, Int>()

        transaction {
            df.forEach { team ->
                val teamId = Team.insert {
                    it[Team.name] = team.team_name
                    it[Team.confederationCode] = team.confederation_code
                }[Team.id]

                teamIds[team.team_id] = teamId
            }
        }

        return teamIds
    }

    private fun loadTournament(df: DataFrame<CSVTournaments>): Map<String, Int> {
        val tournamentsId = mutableMapOf<String, Int>()

        transaction {
            df.forEach { tournament ->
                val tournamentId = Tournament.insert {
                    it[Tournament.name] = tournament.tournament_name
                }[Tournament.id]

                tournamentsId[tournament.tournament_id] = tournamentId
            }
        }

        return tournamentsId

    }

    private fun loadSquad(df: DataFrame<CSVSquads>): Map<Pair<Int, Int>, Int> {
        val squadsId = mutableMapOf<Pair<Int, Int>, Int>()
        transaction {
            df.forEach { squad ->
                val squadId = Squad.insert {
                    it[Squad.teamId] = squad.team_id
                    it[Squad.tournamentId] = squad.tournament_id
                    it[Squad.year] = squad.tournament_name.take(4).toInt()
                }[Squad.id]

                squadsId[Pair(squad.team_id, squad.tournament_id)] = squadId
            }
        }
        return squadsId
    }

    private fun loadPlayerSquad(df: DataFrame<CSVSquads>, squadsIdMapping: Map<Pair<Int, Int>, Int>) {
        transaction {
            df.forEach { squad ->
                val squadId = squadsIdMapping[Pair(squad.team_id, squad.tournament_id)]!!
                PlayerSquad.insert {
                    it[PlayerSquad.playerId] = squad.player_id
                    it[PlayerSquad.squadId] = squadId
                    it[PlayerSquad.position] = preparePosition(squad.position_name)
                }
            }
        }
    }

    private fun prepareDate(date: String?): LocalDate? {
        if (date == null) return null
        return regex.find(date)?.let { LocalDate.parse(it.value) }
    }

    private fun prepareString(value: String): String? {
        if (value == "not applicable")
            return null
        return value
    }

    private fun preparePosition(position: String): Position? {
        return when (position) {
            "goal keeper" -> Position.GOALKEEPER
            "forward" -> Position.FORWARD
            "defender" -> Position.DEFENDER
            "midfielder" -> Position.MIDFIELDER
            else -> null
        }
    }

    fun process(file: File): ImportMappingInfo {
        return extract(file)
    }

}