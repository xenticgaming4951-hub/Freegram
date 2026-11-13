package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MediaFilter(val label: String) {
    NORMAL("Normal"),
    CLARENDON("Clarendon"),
    GINGHAM("Gingham"),
    MOON("Moon"),
    LARK("Lark"),
    REYES("Reyes"),
}

enum class PostMediaType {
    PHOTO,
    CAROUSEL,
    VIDEO,
}

enum class NotificationType {
    LIKE,
    COMMENT,
    FOLLOW,
    MENTION,
}

@Entity(tableName = "users")
data class UserAccount(
    @PrimaryKey val id: String,
    val username: String,
    val fullName: String,
    val bio: String,
    val website: String = "",
    val avatarKey: String,
    val email: String = "",
    val phone: String = "",
    val password: String = "freegram123",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isFollowing: Boolean = false,
    val isPrivate: Boolean = false,
    val isVerified: Boolean = false,
    val isActiveNow: Boolean = false,
)

@Entity(tableName = "stories")
data class StoryItem(
    @PrimaryKey val id: String,
    val userId: String,
    val username: String,
    val avatarKey: String,
    val mediaKey: String,
    val filterName: String = MediaFilter.NORMAL.name,
    val textOverlay: String = "",
    val musicTrack: String = "",
    val createdAtMillis: Long,
    val expiresAtMillis: Long,
    val isViewed: Boolean = false,
)

@Entity(tableName = "posts")
data class FeedPost(
    @PrimaryKey val id: String,
    val userId: String,
    val username: String,
    val userAvatarKey: String,
    val isUserVerified: Boolean = false,
    val location: String = "",
    val caption: String,
    val mediaKeysJoined: String, // pipe-delimited keys/URIs
    val mediaType: String = PostMediaType.PHOTO.name,
    val filterName: String = MediaFilter.NORMAL.name,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val commentsCount: Int = 0,
    val hashtagsJoined: String = "", // comma-delimited lowercase tags
    val createdAtMillis: Long,
) {
    val mediaKeys: List<String>
        get() = mediaKeysJoined.split("|").filter { it.isNotBlank() }

    val hashtags: List<String>
        get() = hashtagsJoined.split(",").map { it.trim() }.filter { it.isNotBlank() }
}

@Entity(tableName = "comments")
data class PostComment(
    @PrimaryKey val id: String,
    val postId: String,
    val parentCommentId: String? = null,
    val userId: String,
    val username: String,
    val userAvatarKey: String,
    val text: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val createdAtMillis: Long,
)

@Entity(tableName = "reels")
data class ReelItem(
    @PrimaryKey val id: String,
    val userId: String,
    val username: String,
    val userAvatarKey: String,
    val isUserVerified: Boolean = false,
    val mediaKey: String,
    val isVideoUri: Boolean = false,
    val filterName: String = MediaFilter.NORMAL.name,
    val caption: String,
    val audioTrackName: String,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val isFollowing: Boolean = false,
    val createdAtMillis: Long,
)

@Entity(tableName = "chat_threads")
data class ChatThread(
    @PrimaryKey val id: String,
    val peerUserId: String,
    val peerUsername: String,
    val peerFullName: String,
    val peerAvatarKey: String,
    val isPeerActive: Boolean = false,
    val lastMessage: String = "",
    val lastTimestampMillis: Long = 0L,
    val unreadCount: Int = 0,
    val isTyping: Boolean = false,
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey val id: String,
    val threadId: String,
    val senderId: String,
    val text: String = "",
    val mediaKey: String = "",
    val sharedPostId: String = "",
    val voiceDurationSec: Int = 0,
    val voiceFilePath: String = "",
    val isLiked: Boolean = false,
    val isSeen: Boolean = true,
    val createdAtMillis: Long,
)

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey val id: String,
    val type: String = NotificationType.LIKE.name,
    val actorUserId: String,
    val actorUsername: String,
    val actorAvatarKey: String,
    val message: String,
    val postId: String = "",
    val isRead: Boolean = false,
    val createdAtMillis: Long,
)
