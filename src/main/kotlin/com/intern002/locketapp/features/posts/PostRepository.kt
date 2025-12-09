package com.intern002.locketapp.features.posts

import com.intern002.locketapp.core.database.DatabaseFactory.dbQuery
import com.intern002.locketapp.core.database.tables.*
import org.jetbrains.exposed.sql.*
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
    suspend fun getPosts(userId: UUID, page: Int, pageSize: Int): List<PostResponse>
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

        if (request.recipientIds.isNotEmpty()) {
            PostRecipientsTable.batchInsert(request.recipientIds) { recipientIdString ->
                this[PostRecipientsTable.postId] = newId
                this[PostRecipientsTable.recipientId] = UUID.fromString(recipientIdString)
            }
        }
        PostsTable.select { PostsTable.id eq newId }
            .map(::toPost)
            .singleOrNull()
    }

    override suspend fun getPostById(postId: UUID): Post? = dbQuery {
        PostsTable.select { PostsTable.id eq postId }
            .map(::toPost)
            .singleOrNull()
    }

    override suspend fun getPosts(userId: UUID, page: Int, pageSize: Int): List<PostResponse> = dbQuery {
        val offset = ((page - 1) * pageSize).toLong()

        val query = PostsTable
            .innerJoin(UsersTable, { PostsTable.authorId }, { UsersTable.id })
            .leftJoin(PostRecipientsTable, { PostsTable.id }, { PostRecipientsTable.postId })

        val postsRows = query
            .slice(
                PostsTable.columns +
                        UsersTable.username +
                        UsersTable.avatarUrl
            )
            .select {
                (PostsTable.authorId eq userId) or (PostRecipientsTable.recipientId eq userId)
            }
            .orderBy(PostsTable.createdAt to SortOrder.DESC)
            .limit(pageSize, offset = offset)
            .withDistinct()
            .toList()

        if (postsRows.isEmpty()) return@dbQuery emptyList()

        val postIds = postsRows.map { it[PostsTable.id] }

        val reactionsRows = (PostReactionsTable innerJoin UsersTable innerJoin ReactionTypesTable)
            .select { PostReactionsTable.postId inList postIds }
            .orderBy(PostReactionsTable.createdAt to SortOrder.DESC)
            .toList()

        val reactionsMap = reactionsRows.groupBy { it[PostReactionsTable.postId] }

        postsRows.map { row ->
            val postId = row[PostsTable.id]
            val reactionsForThisPost = reactionsMap[postId] ?: emptyList()

            PostResponse(
                id = postId.toString(),
                authorId = row[PostsTable.authorId].toString(),
                mediaUrl = row[PostsTable.mediaUrl],
                authorName = row[UsersTable.username],
                authorAvatar = row[UsersTable.avatarUrl],
                mediaType = row[PostsTable.mediaType],
                caption = row[PostsTable.caption],
                createdAt = row[PostsTable.createdAt].toString(),

                reactionCount = reactionsForThisPost.size,
                latestReactions = reactionsForThisPost.map { r ->
                    PostReactionDto(
                        userId = r[UsersTable.id].toString(),
                        username = r[UsersTable.username],
                        avatarUrl = r[UsersTable.avatarUrl],
                        emoji = r[ReactionTypesTable.emoji] ?: "👍"
                    )
                }
            )
        }
    }
}