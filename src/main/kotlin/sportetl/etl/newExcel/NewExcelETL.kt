package io.vladyslavvua.sportetl.etl.newExcel

import io.vladyslavvua.sportetl.entity.Player
import io.vladyslavvua.sportetl.entity.PlayerSquad
import io.vladyslavvua.sportetl.entity.Position
import io.vladyslavvua.sportetl.entity.Sex
import io.vladyslavvua.sportetl.entity.Squad
import io.vladyslavvua.sportetl.entity.Tournament
import io.vladyslavvua.sportetl.etl.ImportMappingInfo
import io.vladyslavvua.sportetl.etl.newExcel.dto.ExcelPlayer
import io.vladyslavvua.sportetl.etl.newExcel.dto.ExcelSquad
import io.vladyslavvua.sportetl.etl.newExcel.dto.ExcelStructure
import io.vladyslavvua.sportetl.etl.newExcel.dto.ExcelStructure.*
import io.vladyslavvua.sportetl.etl.newExcel.dto.ExcelTeam
import io.vladyslavvua.sportetl.etl.newExcel.dto.ExcelTournament
import io.vladyslavvua.sportetl.etl.oldCsv.dto.birth_date
import io.vladyslavvua.sportetl.etl.oldCsv.dto.confederation_code
import io.vladyslavvua.sportetl.etl.oldCsv.dto.family_name
import io.vladyslavvua.sportetl.etl.oldCsv.dto.federation_name
import io.vladyslavvua.sportetl.etl.oldCsv.dto.female
import io.vladyslavvua.sportetl.etl.oldCsv.dto.given_name
import io.vladyslavvua.sportetl.etl.oldCsv.dto.player_id
import io.vladyslavvua.sportetl.etl.oldCsv.dto.team_id
import io.vladyslavvua.sportetl.etl.oldCsv.dto.team_name
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.char
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.cast
import org.jetbrains.kotlinx.dataframe.api.convert
import org.jetbrains.kotlinx.dataframe.api.distinct
import org.jetbrains.kotlinx.dataframe.api.distinctBy
import org.jetbrains.kotlinx.dataframe.api.drop
import org.jetbrains.kotlinx.dataframe.api.filter
import org.jetbrains.kotlinx.dataframe.api.forEach
import org.jetbrains.kotlinx.dataframe.api.generateInterfaces
import org.jetbrains.kotlinx.dataframe.api.map
import org.jetbrains.kotlinx.dataframe.api.print
import org.jetbrains.kotlinx.dataframe.api.select
import org.jetbrains.kotlinx.dataframe.api.sortBy
import org.jetbrains.kotlinx.dataframe.api.with
import org.jetbrains.kotlinx.dataframe.io.readExcel
import java.io.File
import kotlin.collections.set

class NewExcelETL(private val mappingInfo: ImportMappingInfo? = null) {
    private val regex = Regex("\\d{2}\\.\\d{2}\\.\\d{4}")
    private val formatter = LocalDate.Format {
        day()
        char('.')
        monthNumber()
        char('.')
        year()
    }

    private fun extract(file: File) {
        val df = DataFrame.readExcel(file, skipRows = 3)
        transform(df.cast())
    }

    private fun transform(df: DataFrame<ExcelStructure>) {


        val transformedPlayers = df. drop {
            mappingInfo?.playerIdMap?.get(PlayerID) != null
        }
            .convert { Sex }.with {
                val possibleSex = it?.trim()?.firstOrNull()?.lowercase()
                if (possibleSex.isNullOrBlank() || possibleSex.isEmpty()) {
                    val sex = Tournament.split(" ")[2][0].lowercase()
                    if (sex == "m") {
                        "m"
                    } else {
                        "f"
                    }
                } else {
                    possibleSex
                }
            }
            .distinctBy { PlayerID }
            .select { PlayerID and FullName and DOB and Sex }

        println("Transformed players")
        transformedPlayers.print()

        val playersIdMapping = loadPlayers(transformedPlayers.cast(), mappingInfo?.playerIdMap)

        val transformedTeams = df.select {
            TeamID and Team and Federation and Confed
        }
            .drop {
                Federation.isNullOrEmpty()
            }
            .distinctBy { TeamID }
            .drop {
                mappingInfo?.teamIdMap?.containsKey(it.TeamID) ?: false
            }
            .sortBy { TeamID }

        println("Transformed teams")
        transformedTeams.print()

        val teamsIdMapping = loadTeams(transformedTeams.cast(), mappingInfo?.teamIdMap)

        val transformedTournaments = df.select {
            TournamentID and Tournament
        }.distinct()
            .drop {
                mappingInfo?.tournamentIdMap?.containsKey(it.TournamentID) ?: false
            }

        println("Transformed tournaments")
        transformedTournaments.print()

        val tournamentMapping = loadTournaments(transformedTournaments.cast(), mappingInfo?.tournamentIdMap)

        val transformedSquads = df.select {
            PlayerID and TeamID and Pos and TournamentID and Tournament
        }
            .convert { Pos }.with { it?.lowercase() }
            .convert { PlayerID }.with { playersIdMapping[it]!! }
            .convert { TeamID }.with { teamsIdMapping[it]!! }
            .convert { TournamentID }.with { tournamentMapping[it]!! }
            .drop { mappingInfo?.squadIdMap?.containsKey(Pair(TeamID, TournamentID)) ?: false }

        println("Transformed squads")
        transformedSquads.print()


        val squadMappings =
            loadSquads(transformedSquads.distinctBy { TeamID and TournamentID }.cast(), mappingInfo?.squadIdMap)
        loadPlayerSquad(transformedSquads.distinctBy { PlayerID and TeamID and TournamentID }.cast(), squadMappings)

    }

    private fun loadPlayers(players: DataFrame<ExcelPlayer>, playerMapping: Map<String, Int>?): Map<String, Int> {
        val idMappings = playerMapping?.toMutableMap() ?: mutableMapOf()

        transaction {
            players.forEach { player ->
                val playerId = Player.insert {
                    it[Player.familyName] = player.FullName.replace(" not applicable", "").trim()
                    it[Player.givenName] = null
                    it[Player.birthDate] = prepareDate(player.DOB)
                    it[Player.sex] =
                        if (player.Sex.contains('f')) io.vladyslavvua.sportetl.entity.Sex.FEMALE else io.vladyslavvua.sportetl.entity.Sex.MALE
                }[Player.id]

                idMappings[player.PlayerID] = playerId

            }
        }

        return idMappings
    }

    private fun loadTeams(teams: DataFrame<ExcelTeam>, mappingInfo: Map<String, Int>?): Map<String, Int> {
        val idMapping = mappingInfo?.toMutableMap() ?: mutableMapOf()
        transaction {
            teams.forEach { team ->
                val teamId = io.vladyslavvua.sportetl.entity.Team.insert {
                    it[io.vladyslavvua.sportetl.entity.Team.name] = team.Team
                    it[io.vladyslavvua.sportetl.entity.Team.confederationCode] = team.Confed
                }[io.vladyslavvua.sportetl.entity.Team.id]

                idMapping[team.TeamID] = teamId
            }
        }
        return idMapping
    }

    private fun loadTournaments(
        tournaments: DataFrame<ExcelTournament>,
        idMappings: Map<String, Int>?
    ): Map<String, Int> {
        val idMapping = idMappings?.toMutableMap() ?: mutableMapOf()

        transaction {
            tournaments.forEach { tournament ->
                val tournamentId = io.vladyslavvua.sportetl.entity.Tournament.insert {
                    it[io.vladyslavvua.sportetl.entity.Tournament.name] = tournament.Tournament
                }[io.vladyslavvua.sportetl.entity.Tournament.id]

                idMapping[tournament.TournamentID] = tournamentId
            }
        }

        return idMapping

    }

    private fun loadSquads(
        squads: DataFrame<ExcelSquad>,
        mappings: Map<Pair<Int, Int>, Int>?
    ): Map<Pair<Int, Int>, Int> {
        val idMappings = mappings?.toMutableMap() ?: mutableMapOf()

        transaction {
            squads.forEach { squad ->
                val squadId = Squad.insert {
                    it[Squad.teamId] = squad.TeamID
                    it[Squad.tournamentId] = squad.TournamentID
                    it[Squad.year] = squad.Tournament.take(4).toInt()
                }[Squad.id]

                idMappings[Pair(squad.TeamID, squad.TournamentID)] = squadId
            }
        }

        return idMappings

    }

    private fun loadPlayerSquad(squads: DataFrame<ExcelSquad>, squadMappings: Map<Pair<Int, Int>, Int>) {
        transaction {
            squads.forEach { squad ->
                val squadId = squadMappings[Pair(squad.TeamID, squad.TournamentID)]!!
                PlayerSquad.insert {
                    it[PlayerSquad.playerId] = squad.PlayerID
                    it[PlayerSquad.squadId] = squadId
                    it[PlayerSquad.position] = preparePosition(squad.Pos)
                }
            }
        }
    }

    private fun preparePosition(position: String?) = when (position?.get(0)) {
        'g' -> Position.GOALKEEPER
        'd' -> Position.DEFENDER
        'f' -> Position.FORWARD
        'm' -> Position.MIDFIELDER
        else -> null

    }

    private fun prepareDate(date: String?): LocalDate? {
        if (date == null) return null
        return regex.find(date)?.let { formatter.parse(it.value) }
    }

    fun process(file: File) {
        extract(file)

    }
}