package com.intern002.locketapp.features.reaction

import com.intern002.locketapp.core.database.DatabaseFactory.dbQuery
import com.intern002.locketapp.core.database.tables.PostReactionsTable
import com.intern002.locketapp.core.database.tables.ReactionTypesTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

interface ReactionRepository {
    suspend fun getReactionTypes(): List<ReactionTypeResponse>
    suspend fun reactToPost(userId: UUID, postId: UUID, reactionTypeId: Int): Boolean
    suspend fun removeReaction(userId: UUID, postId: UUID): Boolean
}

class ReactionRepositoryImpl : ReactionRepository {

    override suspend fun getReactionTypes(): List<ReactionTypeResponse> = dbQuery {
        ReactionTypesTable.selectAll().orderBy(ReactionTypesTable.id).map {
            ReactionTypeResponse(
                id = it[ReactionTypesTable.id],
                name = it[ReactionTypesTable.name],
                emoji = it[ReactionTypesTable.emoji],
                imageUrl = it[ReactionTypesTable.imageUrl]
            )
        }
    }

    override suspend fun reactToPost(userId: UUID, postId: UUID, reactionTypeId: Int): Boolean = dbQuery {
        val existingReaction = PostReactionsTable.select {
            (PostReactionsTable.postId eq postId) and (PostReactionsTable.reactorId eq userId)
        }.singleOrNull()

        if (existingReaction != null) {
            PostReactionsTable.update({ PostReactionsTable.id eq existingReaction[PostReactionsTable.id] }) {
                it[this.reactionTypeId] = reactionTypeId
            }
        } else {
            PostReactionsTable.insert {
                it[this.postId] = postId
                it[this.reactorId] = userId
                it[this.reactionTypeId] = reactionTypeId
            }
        }
        true
    }

    override suspend fun removeReaction(userId: UUID, postId: UUID): Boolean = dbQuery {
        PostReactionsTable.deleteWhere {
            (this.postId eq postId) and (this.reactorId eq userId)
        } > 0
    }
}