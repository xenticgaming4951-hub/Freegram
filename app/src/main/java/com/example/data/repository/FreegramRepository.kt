package com.example.data.repository

import com.example.data.local.FreegramDao
import com.example.data.local.SeedData
import com.example.data.local.UserPreferences
import com.example.data.model.ChatMessage
import com.example.data.model.ChatThread
import com.example.data.model.FeedPost
import com.example.data.model.MediaFilter
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationType
import com.example.data.model.PostComment
import com.example.data.model.PostMediaType
import com.example.data.model.ReelItem
import com.example.data.model.StoryItem
import com.example.data.model.UserAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class FreegramRepository(
    private val dao: FreegramDao,
    private val prefs: UserPreferences,
) {
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val isDarkMode: Flow<Boolean?> = prefs.isDarkMode
    val loggedInUserId: Flow<String?> = prefs.loggedInUserId

    fun observeUsers(): Flow<List<UserAccount>> = dao.observeAllUsers()
    fun observeUser(userId: String): Flow<UserAccount?> = dao.observeUserById(userId)
    fun observeActiveStories(): Flow<List<StoryItem>> =
        dao.observeActiveStories(System.currentTimeMillis())

    fun observePosts(): Flow<List<FeedPost>> = dao.observeAllPosts()
    fun observeComments(postId: String): Flow<List<PostComment>> = dao.observeCommentsForPost(postId)
    fun observeReels(): Flow<List<ReelItem>> = dao.observeAllReels()
    fun observeChatThreads(): Flow<List<ChatThread>> = dao.observeChatThreads()
    fun observeMessages(threadId: String): Flow<List<ChatMessage>> = dao.observeMessages(threadId)
    fun observeNotifications(): Flow<List<NotificationItem>> = dao.observeNotifications()

    suspend fun ensureSeeded() {
        if (dao.countUsers() == 0) {
            val now = System.currentTimeMillis()
            dao.insertUsers(SeedData.users())
            dao.insertStories(SeedData.stories(now))
            dao.insertPosts(SeedData.posts(now))
            dao.insertComments(SeedData.comments(now))
            dao.insertReels(SeedData.reels(now))
            dao.insertChatThreads(SeedData.threads(now))
            dao.insertMessages(SeedData.messages(now))
            dao.insertNotifications(SeedData.notifications(now))
        }
    }

    suspend fun setDarkMode(enabled: Boolean) = prefs.setDarkMode(enabled)

    suspend fun login(identifier: String, password: String): Result<UserAccount> {
        ensureSeeded()
        val cleanId = identifier.trim().removePrefix("@")
        val user = dao.findUserByLogin(cleanId)
            ?: return Result.failure(IllegalArgumentException("No account found for \"$identifier\""))
        if (password.isNotBlank() && user.password != password && password != "demo") {
            return Result.failure(IllegalArgumentException("Incorrect password. Try again or reset password."))
        }
        prefs.setLoggedInUserId(user.id)
        return Result.success(user)
    }

    suspend fun loginAsDemo(): UserAccount {
        ensureSeeded()
        val demo = dao.getUserById(SeedData.DEMO_USER_ID) ?: SeedData.users().first().also {
            dao.insertUser(it)
        }
        prefs.setLoggedInUserId(demo.id)
        return demo
    }

    suspend fun signUp(
        username: String,
        fullName: String,
        emailOrPhone: String,
        password: String,
        bio: String,
        avatarKey: String,
    ): Result<UserAccount> {
        ensureSeeded()
        val cleanUser = username.trim().lowercase().removePrefix("@").replace(" ", "_")
        if (cleanUser.length < 3) {
            return Result.failure(IllegalArgumentException("Username must be at least 3 characters."))
        }
        if (dao.findUserByLogin(cleanUser) != null) {
            return Result.failure(IllegalArgumentException("Username @$cleanUser is already taken."))
        }
        val isEmail = emailOrPhone.contains("@")
        val newUser = UserAccount(
            id = "user_${UUID.randomUUID().toString().take(8)}",
            username = cleanUser,
            fullName = fullName.trim().ifBlank { cleanUser },
            bio = bio.trim().ifBlank { "New on Freegram" },
            avatarKey = avatarKey.ifBlank { "avatar:1" },
            email = if (isEmail) emailOrPhone.trim() else "",
            phone = if (!isEmail) emailOrPhone.trim() else "",
            password = password.ifBlank { "freegram123" },
            followersCount = 1,
            followingCount = 3,
            isActiveNow = true,
        )
        dao.insertUser(newUser)
        prefs.setLoggedInUserId(newUser.id)
        return Result.success(newUser)
    }

    suspend fun resetPassword(identifier: String, newPassword: String): Result<Unit> {
        ensureSeeded()
        val cleanId = identifier.trim().removePrefix("@")
        val user = dao.findUserByLogin(cleanId)
            ?: return Result.failure(IllegalArgumentException("Account not found for \"$identifier\""))
        if (newPassword.length < 4) {
            return Result.failure(IllegalArgumentException("Password must be at least 4 characters."))
        }
        dao.updateUser(user.copy(password = newPassword))
        return Result.success(Unit)
    }

    suspend fun logout() {
        prefs.setLoggedInUserId(null)
    }

    suspend fun updateProfile(
        userId: String,
        fullName: String,
        username: String,
        bio: String,
        website: String,
        avatarKey: String,
    ) {
        val current = dao.getUserById(userId) ?: return
        val cleanUser = username.trim().lowercase().removePrefix("@").ifBlank { current.username }
        dao.updateUser(
            current.copy(
                fullName = fullName.trim().ifBlank { current.fullName },
                username = cleanUser,
                bio = bio.trim(),
                website = website.trim(),
                avatarKey = avatarKey.ifBlank { current.avatarKey },
            )
        )
    }

    suspend fun togglePrivateAccount(userId: String) {
        val current = dao.getUserById(userId) ?: return
        dao.updateUser(current.copy(isPrivate = !current.isPrivate))
    }

    suspend fun toggleFollowUser(targetUserId: String, currentUserId: String) {
        val target = dao.getUserById(targetUserId) ?: return
        val me = dao.getUserById(currentUserId)
        val nowFollowing = !target.isFollowing
        val delta = if (nowFollowing) 1 else -1
        dao.updateUser(
            target.copy(
                isFollowing = nowFollowing,
                followersCount = (target.followersCount + delta).coerceAtLeast(0),
            )
        )
        if (me != null && me.id != target.id) {
            dao.updateUser(
                me.copy(followingCount = (me.followingCount + delta).coerceAtLeast(0))
            )
        }
    }

    suspend fun toggleLikePost(postId: String, likedOnly: Boolean = false) {
        val post = dao.getPostById(postId) ?: return
        if (likedOnly && post.isLiked) return
        val nextLiked = if (likedOnly) true else !post.isLiked
        val nextCount = (post.likesCount + if (nextLiked) 1 else -1).coerceAtLeast(0)
        dao.updatePost(post.copy(isLiked = nextLiked, likesCount = nextCount))
    }

    suspend fun toggleSavePost(postId: String) {
        val post = dao.getPostById(postId) ?: return
        dao.updatePost(post.copy(isSaved = !post.isSaved))
    }

    suspend fun addComment(
        postId: String,
        author: UserAccount,
        text: String,
        parentCommentId: String? = null,
    ) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        val comment = PostComment(
            id = "c_${UUID.randomUUID().toString().take(8)}",
            postId = postId,
            parentCommentId = parentCommentId,
            userId = author.id,
            username = author.username,
            userAvatarKey = author.avatarKey,
            text = clean,
            likesCount = 0,
            createdAtMillis = System.currentTimeMillis(),
        )
        dao.insertComment(comment)
        dao.getPostById(postId)?.let { post ->
            dao.updatePost(post.copy(commentsCount = post.commentsCount + 1))
        }
    }

    suspend fun toggleCommentLike(comment: PostComment) {
        val next = !comment.isLiked
        dao.updateComment(
            comment.copy(
                isLiked = next,
                likesCount = (comment.likesCount + if (next) 1 else -1).coerceAtLeast(0),
            )
        )
    }

    suspend fun createPost(
        author: UserAccount,
        mediaKeys: List<String>,
        isVideo: Boolean,
        filter: MediaFilter,
        caption: String,
        location: String,
    ) {
        val tags = Regex("#([A-Za-z0-9_]+)")
            .findAll(caption)
            .map { it.groupValues[1].lowercase() }
            .distinct()
            .joinToString(",")
        val mediaType = when {
            isVideo -> PostMediaType.VIDEO.name
            mediaKeys.size > 1 -> PostMediaType.CAROUSEL.name
            else -> PostMediaType.PHOTO.name
        }
        val post = FeedPost(
            id = "post_${UUID.randomUUID().toString().take(8)}",
            userId = author.id,
            username = author.username,
            userAvatarKey = author.avatarKey,
            isUserVerified = author.isVerified,
            location = location.trim(),
            caption = caption.trim(),
            mediaKeysJoined = mediaKeys.joinToString("|"),
            mediaType = mediaType,
            filterName = filter.name,
            likesCount = 0,
            commentsCount = 0,
            hashtagsJoined = tags,
            createdAtMillis = System.currentTimeMillis(),
        )
        dao.insertPost(post)
    }

    suspend fun createStory(
        author: UserAccount,
        mediaKey: String,
        filter: MediaFilter,
        textOverlay: String,
        musicTrack: String,
    ) {
        val now = System.currentTimeMillis()
        val story = StoryItem(
            id = "story_${UUID.randomUUID().toString().take(8)}",
            userId = author.id,
            username = author.username,
            avatarKey = author.avatarKey,
            mediaKey = mediaKey,
            filterName = filter.name,
            textOverlay = textOverlay.trim(),
            musicTrack = musicTrack.trim(),
            createdAtMillis = now,
            expiresAtMillis = now + 24L * 60L * 60L * 1000L,
            isViewed = false,
        )
        dao.insertStory(story)
    }

    suspend fun markStoryViewed(storyId: String) {
        dao.markStoryViewed(storyId)
    }

    suspend fun createReel(
        author: UserAccount,
        mediaKey: String,
        isVideoUri: Boolean,
        filter: MediaFilter,
        caption: String,
        audioTrack: String,
    ) {
        val reel = ReelItem(
            id = "reel_${UUID.randomUUID().toString().take(8)}",
            userId = author.id,
            username = author.username,
            userAvatarKey = author.avatarKey,
            isUserVerified = author.isVerified,
            mediaKey = mediaKey,
            isVideoUri = isVideoUri,
            filterName = filter.name,
            caption = caption.trim(),
            audioTrackName = audioTrack.ifBlank { "${author.username} • Original Audio" },
            likesCount = 0,
            commentsCount = 0,
            sharesCount = 0,
            isFollowing = false,
            createdAtMillis = System.currentTimeMillis(),
        )
        dao.insertReel(reel)
    }

    suspend fun toggleReelLike(reelId: String, likedOnly: Boolean = false) {
        val reel = dao.getReelById(reelId) ?: return
        if (likedOnly && reel.isLiked) return
        val next = if (likedOnly) true else !reel.isLiked
        dao.updateReel(
            reel.copy(
                isLiked = next,
                likesCount = (reel.likesCount + if (next) 1 else -1).coerceAtLeast(0),
            )
        )
    }

    suspend fun toggleReelSave(reelId: String) {
        val reel = dao.getReelById(reelId) ?: return
        dao.updateReel(reel.copy(isSaved = !reel.isSaved))
    }

    suspend fun toggleReelFollow(reelId: String) {
        val reel = dao.getReelById(reelId) ?: return
        dao.updateReel(reel.copy(isFollowing = !reel.isFollowing))
    }

    suspend fun getOrCreateThreadWithUser(peer: UserAccount): ChatThread {
        dao.getThreadByPeerUserId(peer.id)?.let { return it }
        val newThread = ChatThread(
            id = "thread_${peer.id}",
            peerUserId = peer.id,
            peerUsername = peer.username,
            peerFullName = peer.fullName,
            peerAvatarKey = peer.avatarKey,
            isPeerActive = peer.isActiveNow,
            lastMessage = "Say hello to @${peer.username}",
            lastTimestampMillis = System.currentTimeMillis(),
            unreadCount = 0,
        )
        dao.insertChatThread(newThread)
        return newThread
    }

    suspend fun markThreadRead(threadId: String) {
        val thread = dao.getThreadById(threadId) ?: return
        if (thread.unreadCount > 0) {
            dao.updateChatThread(thread.copy(unreadCount = 0))
        }
    }

    suspend fun sendMessage(
        threadId: String,
        senderId: String,
        text: String = "",
        mediaKey: String = "",
        sharedPostId: String = "",
        voiceDurationSec: Int = 0,
        voiceFilePath: String = "",
    ) {
        val now = System.currentTimeMillis()
        val msg = ChatMessage(
            id = "msg_${UUID.randomUUID().toString().take(8)}",
            threadId = threadId,
            senderId = senderId,
            text = text.trim(),
            mediaKey = mediaKey,
            sharedPostId = sharedPostId,
            voiceDurationSec = voiceDurationSec,
            voiceFilePath = voiceFilePath,
            isSeen = true,
            createdAtMillis = now,
        )
        dao.insertMessage(msg)

        val preview = when {
            sharedPostId.isNotBlank() -> "Shared a post"
            voiceDurationSec > 0 -> "Voice note (0:${voiceDurationSec.toString().padStart(2, '0')})"
            mediaKey.isNotBlank() -> "Sent a photo"
            else -> text.trim()
        }
        val thread = dao.getThreadById(threadId) ?: return
        dao.updateChatThread(
            thread.copy(
                lastMessage = preview,
                lastTimestampMillis = now,
                unreadCount = 0,
            )
        )

        // Simulate realistic typing indicator & peer reply on DM messages
        if (text.isNotBlank() && !text.startsWith("Missed ") && !text.startsWith("Ended ")) {
            repoScope.launch {
                delay(700)
                dao.getThreadById(threadId)?.let { t ->
                    dao.updateChatThread(t.copy(isTyping = true))
                }
                delay(1500)
                val replyText = when {
                    text.contains("?", ignoreCase = true) -> "Just saw this — looks awesome! Let's shoot soon."
                    sharedPostId.isNotBlank() -> "Love this frame! The colors are super clean."
                    else -> "Got it! Talk soon on Freegram."
                }
                val replyTime = System.currentTimeMillis()
                dao.insertMessage(
                    ChatMessage(
                        id = "msg_${UUID.randomUUID().toString().take(8)}",
                        threadId = threadId,
                        senderId = thread.peerUserId,
                        text = replyText,
                        isSeen = true,
                        createdAtMillis = replyTime,
                    )
                )
                dao.getThreadById(threadId)?.let { t ->
                    dao.updateChatThread(
                        t.copy(
                            isTyping = false,
                            lastMessage = replyText,
                            lastTimestampMillis = replyTime,
                        )
                    )
                }
            }
        }
    }

    suspend fun toggleMessageHeart(messageId: String) {
        val msg = dao.getMessageById(messageId) ?: return
        dao.updateMessage(msg.copy(isLiked = !msg.isLiked))
    }

    suspend fun markAllNotificationsRead() {
        dao.markAllNotificationsRead()
    }

    suspend fun addNotification(
        type: NotificationType,
        actor: UserAccount,
        message: String,
        postId: String = "",
    ) {
        dao.insertNotification(
            NotificationItem(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                type = type.name,
                actorUserId = actor.id,
                actorUsername = actor.username,
                actorAvatarKey = actor.avatarKey,
                message = message,
                postId = postId,
                isRead = false,
                createdAtMillis = System.currentTimeMillis(),
            )
        )
    }
}
