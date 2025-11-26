package com.intern002.locketapp.features.posts

import com.intern002.locketapp.core.database.DatabaseFactory.dbQuery
import com.intern002.locketapp.core.database.tables.PostsTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.util.*


data class Post(
    val id: UUID,
    val authorId: UUID,
    val mediaUrl: String,
    val mediaType: String,
    val caption: String?,
    val createdAt: String
)

interface PostRepository {
    suspend fun createPost(userId: UUID, request: CreatePostRequest): Post?
}

class PostRepositoryImpl : PostRepository {

    override suspend fun createPost(userId: UUID, request: CreatePostRequest): Post? {
        return dbQuery {
            val insertStatement = PostsTable.insert {
                it[authorId] = userId
                it[mediaUrl] = request.mediaUrl
                it[mediaType] = request.mediaType
                it[caption] = request.caption
            }

            val newId = insertStatement[PostsTable.id]

            PostsTable.select { PostsTable.id eq newId }
                .map { it.toPost() }
                .singleOrNull()
        }
    }

    private fun ResultRow.toPost(): Post = Post(
        id = this[PostsTable.id],
        authorId = this[PostsTable.authorId],
        mediaUrl = this[PostsTable.mediaUrl],
        mediaType = this[PostsTable.mediaType],
        caption = this[PostsTable.caption],
        createdAt = this[PostsTable.createdAt].toString()
    )
}