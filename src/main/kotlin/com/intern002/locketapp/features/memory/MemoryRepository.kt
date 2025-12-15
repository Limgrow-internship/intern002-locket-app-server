package com.intern002.locketapp.features.memory

import com.intern002.locketapp.core.database.DatabaseFactory.dbQuery
import com.intern002.locketapp.core.database.tables.MemoriesTable
import org.jetbrains.exposed.sql.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

interface MemoryRepository {
    suspend fun getMemoriesByMonth(userId: UUID, month: Int, year: Int): List<MemoryResponse>
    suspend fun upsertMemory(userId: UUID, request: CreateMemoryRequest): Boolean
}

class MemoryRepositoryImpl : MemoryRepository {

    private fun toMemory(row: ResultRow): Memory = Memory(
        id = row[MemoriesTable.id],
        postId = row[MemoriesTable.postId],
        authorId = row[MemoriesTable.authorId],
        mediaUrl = row[MemoriesTable.mediaUrl],
        date = row[MemoriesTable.date].toString(),
        createdAt = row[MemoriesTable.createdAt].toString()
    )

    override suspend fun getMemoriesByMonth(userId: UUID, month: Int, year: Int): List<MemoryResponse> = dbQuery {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.plusMonths(1).minusDays(1)

        MemoriesTable
            .select {
                (MemoriesTable.authorId eq userId) and
                        (MemoriesTable.date greaterEq startDate) and
                        (MemoriesTable.date lessEq endDate)
            }
            .orderBy(MemoriesTable.date to SortOrder.ASC)
            .map(::toMemory)
            .map { memory ->
                MemoryResponse(
                    id = memory.id.toString(),
                    postId = memory.postId.toString(),
                    authorId = memory.authorId.toString(),
                    mediaUrl = memory.mediaUrl,
                    date = memory.date,
                    createdAt = memory.createdAt
                )
            }
    }

    override suspend fun upsertMemory(userId: UUID, request: CreateMemoryRequest): Boolean = dbQuery {
        val memoryDate = LocalDate.parse(request.date, DateTimeFormatter.ISO_LOCAL_DATE)
        val newPostId = UUID.fromString(request.postId)

        val existingRecord = MemoriesTable.select {
            (MemoriesTable.authorId eq userId) and (MemoriesTable.date eq memoryDate)
        }.singleOrNull()

        if (existingRecord != null) {
            MemoriesTable.update({ (MemoriesTable.authorId eq userId) and (MemoriesTable.date eq memoryDate) }) {
                it[postId] = newPostId
                it[mediaUrl] = request.mediaUrl
            }
        } else {
            MemoriesTable.insert {
                it[authorId] = userId
                it[date] = memoryDate
                it[postId] = newPostId
                it[mediaUrl] = request.mediaUrl
            }
        }
        true
    }
}