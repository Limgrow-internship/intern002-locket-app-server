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
    suspend fun getPostById(postId: UUID): Post?
}


class PostRepositoryImpl : PostRepository {

    private fun toPost(row: ResultRow): Post = Post(
        id = row[PostsTable.id],
        authorId = row[PostsTable.authorId],
        mediaUrl = row[PostsTable.mediaUrl],
        mediaType = row[PostsTable.mediaType],
        caption = row[PostsTable.caption],
        createdAt = row[PostsTable.createdAt].toString()
    )

    override suspend fun createPost(userId: UUID, request: CreatePostRequest): Post? = dbQuery {
        val insertStatement = PostsTable.insert {
            it[authorId] = userId
            it[mediaUrl] = request.mediaUrl
            it[mediaType] = request.mediaType
            it[caption] = request.caption
        }
        val newId = insertStatement[PostsTable.id]

        PostsTable.select { PostsTable.id eq newId }
            .map(::toPost)
            .singleOrNull()
    }

    override suspend fun getPostById(postId: UUID): Post? = dbQuery {
        PostsTable.select { PostsTable.id eq postId }
            .map(::toPost)
            .singleOrNull()
    }
}