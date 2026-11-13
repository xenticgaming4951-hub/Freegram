package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChatMessage
import com.example.data.model.ChatThread
import com.example.data.model.FeedPost
import com.example.data.model.NotificationItem
import com.example.data.model.PostComment
import com.example.data.model.ReelItem
import com.example.data.model.StoryItem
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface FreegramDao {
    // Users
    @Query("SELECT * FROM users")
    fun observeAllUsers(): Flow<List<UserAccount>>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun observeUserById(userId: String): Flow<UserAccount?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserAccount?

    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:identifier) OR LOWER(email) = LOWER(:identifier) OR phone = :identifier LIMIT 1")
    suspend fun findUserByLogin(identifier: String): UserAccount?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun countUsers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserAccount>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount)

    @Update
    suspend fun updateUser(user: UserAccount)

    // Stories (auto-filtered for 24-hour expiry)
    @Query("SELECT * FROM stories WHERE expiresAtMillis > :nowMillis ORDER BY createdAtMillis DESC")
    fun observeActiveStories(nowMillis: Long): Flow<List<StoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryItem)

    @Query("UPDATE stories SET isViewed = 1 WHERE userId = :userId")
    suspend fun markStoriesViewedByUser(userId: String)

    @Query("UPDATE stories SET isViewed = 1 WHERE id = :storyId")
    suspend fun markStoryViewed(storyId: String)

    // Posts
    @Query("SELECT * FROM posts ORDER BY createdAtMillis DESC")
    fun observeAllPosts(): Flow<List<FeedPost>>

    @Query("SELECT * FROM posts WHERE id = :postId LIMIT 1")
    suspend fun getPostById(postId: String): FeedPost?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<FeedPost>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: FeedPost)

    @Update
    suspend fun updatePost(post: FeedPost)

    // Comments
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAtMillis ASC")
    fun observeCommentsForPost(postId: String): Flow<List<PostComment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<PostComment>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: PostComment)

    @Update
    suspend fun updateComment(comment: PostComment)

    // Reels
    @Query("SELECT * FROM reels ORDER BY createdAtMillis DESC")
    fun observeAllReels(): Flow<List<ReelItem>>

    @Query("SELECT * FROM reels WHERE id = :reelId LIMIT 1")
    suspend fun getReelById(reelId: String): ReelItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReels(reels: List<ReelItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReel(reel: ReelItem)

    @Update
    suspend fun updateReel(reel: ReelItem)

    // Chats
    @Query("SELECT * FROM chat_threads ORDER BY lastTimestampMillis DESC")
    fun observeChatThreads(): Flow<List<ChatThread>>

    @Query("SELECT * FROM chat_threads WHERE id = :threadId LIMIT 1")
    suspend fun getThreadById(threadId: String): ChatThread?

    @Query("SELECT * FROM chat_threads WHERE peerUserId = :peerUserId LIMIT 1")
    suspend fun getThreadByPeerUserId(peerUserId: String): ChatThread?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatThreads(threads: List<ChatThread>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatThread(thread: ChatThread)

    @Update
    suspend fun updateChatThread(thread: ChatThread)

    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY createdAtMillis ASC")
    fun observeMessages(threadId: String): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE id = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: String): ChatMessage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessage>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Update
    suspend fun updateMessage(message: ChatMessage)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY createdAtMillis DESC")
    fun observeNotifications(): Flow<List<NotificationItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationItem)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsRead()
}
