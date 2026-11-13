package com.example.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatMessage
import com.example.data.model.ChatThread
import com.example.data.model.FeedPost
import com.example.data.model.MediaFilter
import com.example.data.model.NotificationItem
import com.example.data.model.PostComment
import com.example.data.model.ReelItem
import com.example.data.model.StoryItem
import com.example.data.model.UserAccount
import com.example.data.repository.FreegramRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    HOME,
    EXPLORE,
    CREATE,
    REELS,
    PROFILE,
}

data class ActiveCallState(
    val threadId: String,
    val peerUser: UserAccount,
    val isVideo: Boolean,
)

class FreegramViewModel(
    private val repository: FreegramRepository,
) : ViewModel() {

    private val _splashFinished = MutableStateFlow(false)
    val splashFinished: StateFlow<Boolean> = _splashFinished.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authInfoMessage = MutableStateFlow<String?>(null)
    val authInfoMessage: StateFlow<String?> = _authInfoMessage.asStateFlow()

    val isDarkMode: StateFlow<Boolean?> = repository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val loggedInUserId: StateFlow<String?> = repository.loggedInUserId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val users: StateFlow<List<UserAccount>> = repository.observeUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<UserAccount?> = combine(users, loggedInUserId) { allUsers, uid ->
        allUsers.firstOrNull { it.id == uid }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val stories: StateFlow<List<StoryItem>> = repository.observeActiveStories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val posts: StateFlow<List<FeedPost>> = repository.observePosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reels: StateFlow<List<ReelItem>> = repository.observeReels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatThreads: StateFlow<List<ChatThread>> = repository.observeChatThreads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationItem>> = repository.observeNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun finishSplash() {
        _splashFinished.value = true
    }

    fun clearAuthMessages() {
        _authError.value = null
        _authInfoMessage.value = null
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { repository.setDarkMode(enabled) }
    }

    fun login(identifier: String, password: String) {
        viewModelScope.launch {
            _authError.value = null
            _authInfoMessage.value = null
            repository.login(identifier, password).onFailure {
                _authError.value = it.message ?: "Login failed"
            }
        }
    }

    fun loginAsDemo() {
        viewModelScope.launch {
            _authError.value = null
            repository.loginAsDemo()
        }
    }

    fun signUp(
        username: String,
        fullName: String,
        emailOrPhone: String,
        password: String,
        bio: String,
        avatarKey: String,
    ) {
        viewModelScope.launch {
            _authError.value = null
            _authInfoMessage.value = null
            repository.signUp(username, fullName, emailOrPhone, password, bio, avatarKey).onFailure {
                _authError.value = it.message ?: "Sign up failed"
            }
        }
    }

    fun resetPassword(identifier: String, newPassword: String) {
        viewModelScope.launch {
            _authError.value = null
            repository.resetPassword(identifier, newPassword).onSuccess {
                _authInfoMessage.value = "Password updated. You can sign in now."
            }.onFailure {
                _authError.value = it.message ?: "Password reset failed"
            }
        }
    }

    fun logout() {
        viewModelScope.launch { repository.logout() }
    }

    fun togglePrivateAccount() {
        val uid = loggedInUserId.value ?: return
        viewModelScope.launch { repository.togglePrivateAccount(uid) }
    }

    fun updateProfile(
        fullName: String,
        username: String,
        bio: String,
        website: String,
        avatarKey: String,
    ) {
        val uid = loggedInUserId.value ?: return
        viewModelScope.launch {
            repository.updateProfile(uid, fullName, username, bio, website, avatarKey)
        }
    }

    fun toggleFollow(targetUserId: String) {
        val uid = loggedInUserId.value ?: return
        viewModelScope.launch { repository.toggleFollowUser(targetUserId, uid) }
    }

    fun toggleLikePost(postId: String, likedOnly: Boolean = false) {
        viewModelScope.launch { repository.toggleLikePost(postId, likedOnly) }
    }

    fun toggleSavePost(postId: String) {
        viewModelScope.launch { repository.toggleSavePost(postId) }
    }

    fun observeComments(postId: String): Flow<List<PostComment>> =
        repository.observeComments(postId)

    fun addComment(postId: String, text: String, parentCommentId: String? = null) {
        val author = currentUser.value ?: return
        viewModelScope.launch {
            repository.addComment(postId, author, text, parentCommentId)
        }
    }

    fun toggleCommentLike(comment: PostComment) {
        viewModelScope.launch { repository.toggleCommentLike(comment) }
    }

    fun createPost(
        mediaKeys: List<String>,
        isVideo: Boolean,
        filter: MediaFilter,
        caption: String,
        location: String,
    ) {
        val author = currentUser.value ?: return
        viewModelScope.launch {
            repository.createPost(author, mediaKeys, isVideo, filter, caption, location)
        }
    }

    fun createStory(
        mediaKey: String,
        filter: MediaFilter,
        textOverlay: String,
        musicTrack: String,
    ) {
        val author = currentUser.value ?: return
        viewModelScope.launch {
            repository.createStory(author, mediaKey, filter, textOverlay, musicTrack)
        }
    }

    fun markStoryViewed(storyId: String) {
        viewModelScope.launch { repository.markStoryViewed(storyId) }
    }

    fun createReel(
        mediaKey: String,
        isVideoUri: Boolean,
        filter: MediaFilter,
        caption: String,
        audioTrack: String,
    ) {
        val author = currentUser.value ?: return
        viewModelScope.launch {
            repository.createReel(author, mediaKey, isVideoUri, filter, caption, audioTrack)
        }
    }

    fun toggleReelLike(reelId: String, likedOnly: Boolean = false) {
        viewModelScope.launch { repository.toggleReelLike(reelId, likedOnly) }
    }

    fun toggleReelSave(reelId: String) {
        viewModelScope.launch { repository.toggleReelSave(reelId) }
    }

    fun toggleReelFollow(reelId: String) {
        viewModelScope.launch { repository.toggleReelFollow(reelId) }
    }

    fun observeMessages(threadId: String): Flow<List<ChatMessage>> =
        repository.observeMessages(threadId)

    fun openChatWithUser(peer: UserAccount, onReady: (String) -> Unit) {
        viewModelScope.launch {
            val thread = repository.getOrCreateThreadWithUser(peer)
            repository.markThreadRead(thread.id)
            onReady(thread.id)
        }
    }

    fun markThreadRead(threadId: String) {
        viewModelScope.launch { repository.markThreadRead(threadId) }
    }

    fun sendMessage(
        threadId: String,
        text: String = "",
        mediaKey: String = "",
        sharedPostId: String = "",
        voiceDurationSec: Int = 0,
        voiceFilePath: String = "",
    ) {
        val senderId = loggedInUserId.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                threadId = threadId,
                senderId = senderId,
                text = text,
                mediaKey = mediaKey,
                sharedPostId = sharedPostId,
                voiceDurationSec = voiceDurationSec,
                voiceFilePath = voiceFilePath,
            )
        }
    }

    fun sharePostToThread(threadId: String, postId: String) {
        val senderId = loggedInUserId.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                threadId = threadId,
                senderId = senderId,
                text = "Check out this post on Freegram",
                sharedPostId = postId,
            )
        }
    }

    fun toggleMessageHeart(messageId: String) {
        viewModelScope.launch { repository.toggleMessageHeart(messageId) }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch { repository.markAllNotificationsRead() }
    }

    class Factory(private val repository: FreegramRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FreegramViewModel(repository) as T
        }
    }
}
