package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.FeedPost
import com.example.data.model.ReelItem
import com.example.data.model.StoryItem
import com.example.ui.components.BrandWordmark
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.FeedPostCard
import com.example.ui.components.FreegramAvatar
import com.example.ui.components.NotificationsBottomSheet
import com.example.ui.components.SharePostBottomSheet
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CallScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.CreateContentType
import com.example.ui.screens.CreateModalScreen
import com.example.ui.screens.DmInboxScreen
import com.example.ui.screens.EditProfileBottomSheet
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveStreamScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReelsScreen
import com.example.ui.screens.SettingsBottomSheet
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StoryViewerScreen
import com.example.ui.state.ActiveCallState
import com.example.ui.state.FreegramViewModel
import com.example.ui.state.MainTab
import com.example.ui.theme.FreegramTheme
import com.example.ui.theme.LocalFreegramPalette
import kotlinx.coroutines.launch

private sealed interface OverlayRoute {
    data object None : OverlayRoute
    data class StoryViewer(val stories: List<StoryItem>, val startIndex: Int) : OverlayRoute
    data class CreateModal(val mode: CreateContentType) : OverlayRoute
    data class LiveBroadcast(val title: String) : OverlayRoute
    data object DmInbox : OverlayRoute
    data class ChatDetail(val threadId: String) : OverlayRoute
    data class ActiveCall(val call: ActiveCallState) : OverlayRoute
    data class OtherProfile(val userId: String) : OverlayRoute
    data class PostDetail(val postId: String) : OverlayRoute
}

@Composable
fun FreegramApp(viewModel: FreegramViewModel) {
    val systemDark = isSystemInDarkTheme()
    val darkModePref by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val useDarkTheme = darkModePref ?: systemDark

    val splashDone by viewModel.splashFinished.collectAsStateWithLifecycle()
    val loggedInUserId by viewModel.loggedInUserId.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val authInfo by viewModel.authInfoMessage.collectAsStateWithLifecycle()

    FreegramTheme(darkTheme = useDarkTheme) {
        when {
            !splashDone -> {
                SplashScreen(onFinished = { viewModel.finishSplash() })
            }
            loggedInUserId == null || currentUser == null -> {
                AuthScreen(
                    errorMessage = authError,
                    infoMessage = authInfo,
                    onClearMessages = { viewModel.clearAuthMessages() },
                    onLogin = { id, pw -> viewModel.login(id, pw) },
                    onLoginAsDemo = { viewModel.loginAsDemo() },
                    onSignUp = { u, full, contact, pw, bio, avatar ->
                        viewModel.signUp(u, full, contact, pw, bio, avatar)
                    },
                    onResetPassword = { id, newPw -> viewModel.resetPassword(id, newPw) },
                )
            }
            else -> {
                FreegramMainShell(
                    viewModel = viewModel,
                    useDarkTheme = useDarkTheme,
                )
            }
        }
    }
}

@Composable
private fun FreegramMainShell(
    viewModel: FreegramViewModel,
    useDarkTheme: Boolean,
) {
    val palette = LocalFreegramPalette.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.users.collectAsStateWithLifecycle()
    val stories by viewModel.stories.collectAsStateWithLifecycle()
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val reels by viewModel.reels.collectAsStateWithLifecycle()
    val threads by viewModel.chatThreads.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(MainTab.HOME) }
    var overlay by remember { mutableStateOf<OverlayRoute>(OverlayRoute.None) }
    var exploreQuery by remember { mutableStateOf("") }

    var activeCommentsPostId by remember { mutableStateOf<String?>(null) }
    var activeSharePost by remember { mutableStateOf<FeedPost?>(null) }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showEditProfileSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    val postsById = remember(posts) { posts.associateBy { it.id } }
    val usersById = remember(allUsers) { allUsers.associateBy { it.id } }
    val unreadNotifs = remember(notifications) { notifications.count { !it.isRead } }
    val unreadMessages = remember(threads) { threads.sumOf { it.unreadCount } }

    fun copyTextToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        scope.launch {
            snackbarHostState.showSnackbar("Link copied to clipboard")
        }
    }

    // Handle system Back cleanly for overlays & non-Home tabs
    BackHandler(enabled = overlay != OverlayRoute.None || selectedTab != MainTab.HOME) {
        when (val current = overlay) {
            is OverlayRoute.ActiveCall -> overlay = OverlayRoute.ChatDetail(current.call.threadId)
            is OverlayRoute.ChatDetail -> overlay = OverlayRoute.DmInbox
            is OverlayRoute.None -> selectedTab = MainTab.HOME
            else -> overlay = OverlayRoute.None
        }
    }

    Scaffold(
        containerColor = palette.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            // Only show compact bottom nav on narrow screens when no full-screen overlay is active
        },
    ) { _ ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth >= 600.dp

            Row(modifier = Modifier.fillMaxSize()) {
                // Tablet / Desktop Left Sidebar Navigation Rail
                if (isWideScreen && overlay == OverlayRoute.None) {
                    NavigationRail(
                        containerColor = palette.background,
                        header = {
                            Spacer(modifier = Modifier.height(12.dp))
                            BrandWordmark(fontSizeSp = 24f)
                            Spacer(modifier = Modifier.height(16.dp))
                        },
                        modifier = Modifier.statusBarsPadding(),
                    ) {
                        NavigationRailItem(
                            selected = selectedTab == MainTab.HOME,
                            onClick = { selectedTab = MainTab.HOME },
                            icon = {
                                Icon(
                                    if (selectedTab == MainTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = stringResource(id = R.string.nav_home),
                                )
                            },
                            label = { Text("Home") },
                        )
                        NavigationRailItem(
                            selected = selectedTab == MainTab.EXPLORE,
                            onClick = { selectedTab = MainTab.EXPLORE },
                            icon = {
                                Icon(
                                    if (selectedTab == MainTab.EXPLORE) Icons.Filled.Search else Icons.Outlined.Search,
                                    contentDescription = stringResource(id = R.string.nav_search),
                                )
                            },
                            label = { Text("Search") },
                        )
                        NavigationRailItem(
                            selected = false,
                            onClick = { overlay = OverlayRoute.CreateModal(CreateContentType.POST) },
                            icon = {
                                Icon(
                                    Icons.Outlined.AddBox,
                                    contentDescription = stringResource(id = R.string.nav_create),
                                )
                            },
                            label = { Text("Create") },
                        )
                        NavigationRailItem(
                            selected = selectedTab == MainTab.REELS,
                            onClick = { selectedTab = MainTab.REELS },
                            icon = {
                                Icon(
                                    if (selectedTab == MainTab.REELS) Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary,
                                    contentDescription = stringResource(id = R.string.nav_reels),
                                )
                            },
                            label = { Text("Reels") },
                        )
                        NavigationRailItem(
                            selected = selectedTab == MainTab.PROFILE,
                            onClick = { selectedTab = MainTab.PROFILE },
                            icon = {
                                FreegramAvatar(
                                    avatarKey = currentUser?.avatarKey ?: "avatar:1",
                                    username = currentUser?.username ?: "me",
                                    size = 26.dp,
                                )
                            },
                            label = { Text("Profile") },
                        )
                    }
                    VerticalDivider(color = palette.divider, thickness = 0.5.dp)
                }

                // Main Content Column
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        when (selectedTab) {
                            MainTab.HOME -> HomeScreen(
                                currentUser = currentUser,
                                stories = stories,
                                posts = posts,
                                unreadNotificationsCount = unreadNotifs,
                                unreadMessagesCount = unreadMessages,
                                onNotificationsClick = {
                                    viewModel.markAllNotificationsRead()
                                    showNotificationsSheet = true
                                },
                                onDmInboxClick = { overlay = OverlayRoute.DmInbox },
                                onAddStoryClick = {
                                    overlay = OverlayRoute.CreateModal(CreateContentType.STORY)
                                },
                                onOpenUserStory = { uid ->
                                    val userStories = stories.filter { it.userId == uid }
                                    if (userStories.isNotEmpty()) {
                                        overlay = OverlayRoute.StoryViewer(userStories, 0)
                                    }
                                },
                                onAuthorClick = { uid ->
                                    if (uid == currentUser?.id) selectedTab = MainTab.PROFILE
                                    else overlay = OverlayRoute.OtherProfile(uid)
                                },
                                onLikePost = { id, likedOnly -> viewModel.toggleLikePost(id, likedOnly) },
                                onCommentClick = { post -> activeCommentsPostId = post.id },
                                onShareClick = { post -> activeSharePost = post },
                                onSavePost = { id -> viewModel.toggleSavePost(id) },
                                onCopyLink = { post ->
                                    copyTextToClipboard("Freegram post", "https://freegram.app/p/${post.id}")
                                },
                                onHashtagClick = { tag ->
                                    exploreQuery = "#$tag"
                                    selectedTab = MainTab.EXPLORE
                                },
                            )

                            MainTab.EXPLORE -> ExploreScreen(
                                initialQuery = exploreQuery,
                                users = allUsers,
                                posts = posts,
                                reels = reels,
                                onUserClick = { u ->
                                    if (u.id == currentUser?.id) selectedTab = MainTab.PROFILE
                                    else overlay = OverlayRoute.OtherProfile(u.id)
                                },
                                onPostClick = { post -> overlay = OverlayRoute.PostDetail(post.id) },
                                onReelClick = { _ -> selectedTab = MainTab.REELS },
                            )

                            MainTab.REELS -> ReelsScreen(
                                reels = reels,
                                onLikeReel = { id, likedOnly -> viewModel.toggleReelLike(id, likedOnly) },
                                onSaveReel = { id -> viewModel.toggleReelSave(id) },
                                onFollowReelCreator = { id -> viewModel.toggleReelFollow(id) },
                                onOpenAuthorProfile = { uid ->
                                    if (uid == currentUser?.id) selectedTab = MainTab.PROFILE
                                    else overlay = OverlayRoute.OtherProfile(uid)
                                },
                                onOpenComments = { reel ->
                                    activeCommentsPostId = posts.firstOrNull()?.id
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Comments for @${reel.username}'s Reel")
                                    }
                                },
                                onShareReel = { reel ->
                                    activeSharePost = posts.firstOrNull()
                                        ?: FeedPost(
                                            id = reel.id,
                                            userId = reel.userId,
                                            username = reel.username,
                                            userAvatarKey = reel.userAvatarKey,
                                            caption = reel.caption,
                                            mediaKeysJoined = reel.mediaKey,
                                            createdAtMillis = reel.createdAtMillis,
                                        )
                                },
                                onCreateReelClick = {
                                    overlay = OverlayRoute.CreateModal(CreateContentType.REEL)
                                },
                            )

                            MainTab.PROFILE -> {
                                val me = currentUser
                                if (me != null) {
                                    ProfileScreen(
                                        profileUser = me,
                                        isOwnProfile = true,
                                        userPosts = posts.filter { it.userId == me.id },
                                        userReels = reels.filter { it.userId == me.id },
                                        savedPosts = posts.filter { it.isSaved },
                                        userStories = stories.filter { it.userId == me.id },
                                        onBack = null,
                                        onOpenSettings = { showSettingsSheet = true },
                                        onEditProfile = { showEditProfileSheet = true },
                                        onShareProfile = {
                                            copyTextToClipboard("Profile", "https://freegram.app/@${me.username}")
                                        },
                                        onToggleFollow = {},
                                        onMessageUser = {},
                                        onOpenStory = {
                                            val myStories = stories.filter { it.userId == me.id }
                                            if (myStories.isNotEmpty()) {
                                                overlay = OverlayRoute.StoryViewer(myStories, 0)
                                            }
                                        },
                                        onPostClick = { post -> overlay = OverlayRoute.PostDetail(post.id) },
                                        onReelClick = { _ -> selectedTab = MainTab.REELS },
                                    )
                                }
                            }

                            MainTab.CREATE -> Unit
                        }
                    }

                    // Mobile Bottom Navigation Bar (5 Instagram-style icons)
                    if (!isWideScreen && overlay == OverlayRoute.None) {
                        val isReelsDarkBar = selectedTab == MainTab.REELS
                        val barBg = if (isReelsDarkBar) Color.Black else palette.background
                        val iconTint = if (isReelsDarkBar) Color.White else palette.textPrimary

                        HorizontalDivider(
                            color = if (isReelsDarkBar) Color.White.copy(alpha = 0.14f) else palette.divider.copy(alpha = 0.5f),
                            thickness = 0.5.dp,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(barBg)
                                .navigationBarsPadding()
                                .height(54.dp)
                                .testTag("bottom_nav_bar"),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = { selectedTab = MainTab.HOME },
                                modifier = Modifier.testTag("nav_home_button"),
                            ) {
                                Icon(
                                    imageVector = if (selectedTab == MainTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = stringResource(id = R.string.nav_home),
                                    tint = iconTint,
                                    modifier = Modifier.size(27.dp),
                                )
                            }
                            IconButton(
                                onClick = { selectedTab = MainTab.EXPLORE },
                                modifier = Modifier.testTag("nav_explore_button"),
                            ) {
                                Icon(
                                    imageVector = if (selectedTab == MainTab.EXPLORE) Icons.Filled.Search else Icons.Outlined.Search,
                                    contentDescription = stringResource(id = R.string.nav_search),
                                    tint = iconTint,
                                    modifier = Modifier.size(27.dp),
                                )
                            }
                            IconButton(
                                onClick = { overlay = OverlayRoute.CreateModal(CreateContentType.POST) },
                                modifier = Modifier.testTag("nav_create_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddBox,
                                    contentDescription = stringResource(id = R.string.nav_create),
                                    tint = iconTint,
                                    modifier = Modifier.size(27.dp),
                                )
                            }
                            IconButton(
                                onClick = { selectedTab = MainTab.REELS },
                                modifier = Modifier.testTag("nav_reels_button"),
                            ) {
                                Icon(
                                    imageVector = if (selectedTab == MainTab.REELS) Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary,
                                    contentDescription = stringResource(id = R.string.nav_reels),
                                    tint = iconTint,
                                    modifier = Modifier.size(26.dp),
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable { selectedTab = MainTab.PROFILE }
                                    .testTag("nav_profile_button"),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .then(
                                            if (selectedTab == MainTab.PROFILE) {
                                                Modifier.border(2.dp, iconTint, CircleShape)
                                            } else Modifier
                                        )
                                        .padding(2.dp),
                                ) {
                                    FreegramAvatar(
                                        avatarKey = currentUser?.avatarKey ?: "avatar:1",
                                        username = currentUser?.username ?: "me",
                                        size = 26.dp,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Full-Screen Overlays
            when (val activeOverlay = overlay) {
                is OverlayRoute.StoryViewer -> {
                    StoryViewerScreen(
                        stories = activeOverlay.stories,
                        initialIndex = activeOverlay.startIndex,
                        onMarkViewed = { id -> viewModel.markStoryViewed(id) },
                        onSendReply = { targetUid, text ->
                            val target = usersById[targetUid]
                            if (target != null) {
                                viewModel.openChatWithUser(target) { tid ->
                                    viewModel.sendMessage(tid, text = text)
                                }
                                scope.launch {
                                    snackbarHostState.showSnackbar("Sent reply to @${target.username}")
                                }
                            }
                        },
                        onClose = { overlay = OverlayRoute.None },
                    )
                }

                is OverlayRoute.CreateModal -> {
                    CreateModalScreen(
                        initialMode = activeOverlay.mode,
                        onClose = { overlay = OverlayRoute.None },
                        onPublishPost = { mediaKeys, isVideo, filter, caption, loc ->
                            viewModel.createPost(mediaKeys, isVideo, filter, caption, loc)
                            overlay = OverlayRoute.None
                            selectedTab = MainTab.HOME
                        },
                        onPublishStory = { mediaKey, filter, textOverlay, music ->
                            viewModel.createStory(mediaKey, filter, textOverlay, music)
                            overlay = OverlayRoute.None
                            selectedTab = MainTab.HOME
                        },
                        onPublishReel = { mediaKey, isVideoUri, filter, caption, audio ->
                            viewModel.createReel(mediaKey, isVideoUri, filter, caption, audio)
                            overlay = OverlayRoute.None
                            selectedTab = MainTab.REELS
                        },
                        onStartLive = { title ->
                            overlay = OverlayRoute.LiveBroadcast(title)
                        },
                    )
                }

                is OverlayRoute.LiveBroadcast -> {
                    LiveStreamScreen(
                        hostUser = currentUser,
                        title = activeOverlay.title,
                        onEndLive = {
                            overlay = OverlayRoute.None
                            scope.launch { snackbarHostState.showSnackbar("Live broadcast ended") }
                        },
                    )
                }

                is OverlayRoute.DmInbox -> {
                    DmInboxScreen(
                        currentUser = currentUser,
                        threads = threads,
                        allUsers = allUsers,
                        onBack = { overlay = OverlayRoute.None },
                        onThreadClick = { thread ->
                            viewModel.markThreadRead(thread.id)
                            overlay = OverlayRoute.ChatDetail(thread.id)
                        },
                        onStartChatWithUser = { peer ->
                            viewModel.openChatWithUser(peer) { tid ->
                                overlay = OverlayRoute.ChatDetail(tid)
                            }
                        },
                    )
                }

                is OverlayRoute.ChatDetail -> {
                    val thread = threads.firstOrNull { it.id == activeOverlay.threadId }
                    if (thread != null && currentUser != null) {
                        val messages by viewModel.observeMessages(thread.id)
                            .collectAsStateWithLifecycle(initialValue = emptyList())
                        val peerUser = usersById[thread.peerUserId]
                        ChatDetailScreen(
                            thread = thread,
                            messages = messages,
                            postsById = postsById,
                            currentUserId = currentUser!!.id,
                            onBack = { overlay = OverlayRoute.DmInbox },
                            onStartAudioCall = {
                                if (peerUser != null) {
                                    overlay = OverlayRoute.ActiveCall(
                                        ActiveCallState(thread.id, peerUser, isVideo = false)
                                    )
                                }
                            },
                            onStartVideoCall = {
                                if (peerUser != null) {
                                    overlay = OverlayRoute.ActiveCall(
                                        ActiveCallState(thread.id, peerUser, isVideo = true)
                                    )
                                }
                            },
                            onSendMessage = { text, mediaKey, voiceSec, voicePath ->
                                viewModel.sendMessage(
                                    threadId = thread.id,
                                    text = text,
                                    mediaKey = mediaKey,
                                    voiceDurationSec = voiceSec,
                                    voiceFilePath = voicePath,
                                )
                            },
                            onDoubleTapMessage = { msgId -> viewModel.toggleMessageHeart(msgId) },
                            onOpenSharedPost = { post ->
                                overlay = OverlayRoute.PostDetail(post.id)
                            },
                        )
                    }
                }

                is OverlayRoute.ActiveCall -> {
                    CallScreen(
                        peerUser = activeOverlay.call.peerUser,
                        isVideoCall = activeOverlay.call.isVideo,
                        onEndCall = { duration ->
                            val label = if (activeOverlay.call.isVideo) "Video call" else "Audio call"
                            viewModel.sendMessage(
                                threadId = activeOverlay.call.threadId,
                                text = "Ended $label (${duration}s)",
                            )
                            overlay = OverlayRoute.ChatDetail(activeOverlay.call.threadId)
                        },
                    )
                }

                is OverlayRoute.OtherProfile -> {
                    val target = usersById[activeOverlay.userId]
                    if (target != null) {
                        ProfileScreen(
                            profileUser = target,
                            isOwnProfile = false,
                            userPosts = posts.filter { it.userId == target.id },
                            userReels = reels.filter { it.userId == target.id },
                            savedPosts = emptyList(),
                            userStories = stories.filter { it.userId == target.id },
                            onBack = { overlay = OverlayRoute.None },
                            onOpenSettings = {},
                            onEditProfile = {},
                            onShareProfile = {
                                copyTextToClipboard("Profile", "https://freegram.app/@${target.username}")
                            },
                            onToggleFollow = { viewModel.toggleFollow(target.id) },
                            onMessageUser = {
                                viewModel.openChatWithUser(target) { tid ->
                                    overlay = OverlayRoute.ChatDetail(tid)
                                }
                            },
                            onOpenStory = {
                                val userStories = stories.filter { it.userId == target.id }
                                if (userStories.isNotEmpty()) {
                                    overlay = OverlayRoute.StoryViewer(userStories, 0)
                                }
                            },
                            onPostClick = { post -> overlay = OverlayRoute.PostDetail(post.id) },
                            onReelClick = { _ ->
                                overlay = OverlayRoute.None
                                selectedTab = MainTab.REELS
                            },
                        )
                    }
                }

                is OverlayRoute.PostDetail -> {
                    val post = postsById[activeOverlay.postId]
                    if (post != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(palette.background)
                                .statusBarsPadding()
                                .navigationBarsPadding(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(onClick = { overlay = OverlayRoute.None }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = palette.textPrimary,
                                    )
                                }
                                Text(
                                    text = "Post",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = palette.textPrimary,
                                )
                            }
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                FeedPostCard(
                                    post = post,
                                    hasStory = stories.any { it.userId == post.userId },
                                    hasUnviewedStory = stories.any { it.userId == post.userId && !it.isViewed },
                                    onAuthorClick = { uid -> overlay = OverlayRoute.OtherProfile(uid) },
                                    onLikeToggle = { likedOnly -> viewModel.toggleLikePost(post.id, likedOnly) },
                                    onCommentClick = { activeCommentsPostId = post.id },
                                    onShareClick = { activeSharePost = post },
                                    onSaveToggle = { viewModel.toggleSavePost(post.id) },
                                    onCopyLink = {
                                        copyTextToClipboard("Post", "https://freegram.app/p/${post.id}")
                                    },
                                    onHashtagClick = { tag ->
                                        overlay = OverlayRoute.None
                                        exploreQuery = "#$tag"
                                        selectedTab = MainTab.EXPLORE
                                    },
                                )
                            }
                        }
                    }
                }

                OverlayRoute.None -> Unit
            }
        }
    }

    // Bottom Sheets (Comments, Share, Notifications, Edit Profile, Settings)
    if (activeCommentsPostId != null) {
        val pid = activeCommentsPostId!!
        val comments by viewModel.observeComments(pid).collectAsStateWithLifecycle(initialValue = emptyList())
        CommentsBottomSheet(
            comments = comments,
            currentUser = currentUser,
            onDismiss = { activeCommentsPostId = null },
            onAddComment = { text, parentId -> viewModel.addComment(pid, text, parentId) },
            onToggleCommentLike = { comment -> viewModel.toggleCommentLike(comment) },
        )
    }

    if (activeSharePost != null) {
        val post = activeSharePost!!
        SharePostBottomSheet(
            threads = threads,
            onDismiss = { activeSharePost = null },
            onSendToThread = { thread ->
                viewModel.sharePostToThread(thread.id, post.id)
                activeSharePost = null
                scope.launch {
                    snackbarHostState.showSnackbar("Sent to @${thread.peerUsername}")
                }
            },
            onCopyLink = {
                activeSharePost = null
                copyTextToClipboard("Post", "https://freegram.app/p/${post.id}")
            },
        )
    }

    if (showNotificationsSheet) {
        NotificationsBottomSheet(
            notifications = notifications,
            onDismiss = { showNotificationsSheet = false },
            onNotificationClick = { notif ->
                showNotificationsSheet = false
                if (notif.postId.isNotBlank() && postsById.containsKey(notif.postId)) {
                    overlay = OverlayRoute.PostDetail(notif.postId)
                } else {
                    overlay = OverlayRoute.OtherProfile(notif.actorUserId)
                }
            },
        )
    }

    if (showEditProfileSheet && currentUser != null) {
        EditProfileBottomSheet(
            user = currentUser!!,
            onDismiss = { showEditProfileSheet = false },
            onSave = { full, uname, bio, web, avatar ->
                viewModel.updateProfile(full, uname, bio, web, avatar)
                showEditProfileSheet = false
            },
        )
    }

    if (showSettingsSheet && currentUser != null) {
        SettingsBottomSheet(
            isDarkMode = useDarkTheme,
            isPrivateAccount = currentUser!!.isPrivate,
            onToggleDarkMode = { viewModel.setDarkMode(it) },
            onTogglePrivateAccount = { viewModel.togglePrivateAccount() },
            onLogout = {
                showSettingsSheet = false
                viewModel.logout()
            },
            onDismiss = { showSettingsSheet = false },
        )
    }
}
