package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.FeedPost
import com.example.data.model.StoryItem
import com.example.data.model.UserAccount
import com.example.ui.components.BrandWordmark
import com.example.ui.components.FeedPostCard
import com.example.ui.components.FreegramAvatar
import com.example.ui.components.FreegramMediaView
import com.example.ui.components.formatRelativeTime
import com.example.ui.theme.FreegramBlue
import com.example.ui.theme.FreegramHeartRed
import com.example.ui.theme.LocalFreegramPalette
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    currentUser: UserAccount?,
    stories: List<StoryItem>,
    posts: List<FeedPost>,
    unreadNotificationsCount: Int,
    unreadMessagesCount: Int,
    onNotificationsClick: () -> Unit,
    onDmInboxClick: () -> Unit,
    onAddStoryClick: () -> Unit,
    onOpenUserStory: (userId: String) -> Unit,
    onAuthorClick: (userId: String) -> Unit,
    onLikePost: (postId: String, likedOnly: Boolean) -> Unit,
    onCommentClick: (FeedPost) -> Unit,
    onShareClick: (FeedPost) -> Unit,
    onSavePost: (String) -> Unit,
    onCopyLink: (FeedPost) -> Unit,
    onHashtagClick: (String) -> Unit,
) {
    val palette = LocalFreegramPalette.current
    val storiesByUser = remember(stories) { stories.groupBy { it.userId } }
    val orderedStoryAuthors = remember(stories, currentUser?.id) {
        storiesByUser.keys.filter { it != currentUser?.id }
    }
    val myStories = currentUser?.let { storiesByUser[it.id] }.orEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 620.dp)
                .fillMaxSize()
                .testTag("home_feed_list"),
        ) {
            // Top bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BrandWordmark(fontSizeSp = 32f)
                    Spacer(modifier = Modifier.weight(1f))

                    Box {
                        IconButton(
                            onClick = onNotificationsClick,
                            modifier = Modifier.testTag("home_notifications_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = stringResource(id = R.string.action_notifications),
                                tint = palette.textPrimary,
                                modifier = Modifier.size(26.dp),
                            )
                        }
                        if (unreadNotificationsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 8.dp)
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(FreegramHeartRed),
                            )
                        }
                    }

                    Box {
                        IconButton(
                            onClick = onDmInboxClick,
                            modifier = Modifier.testTag("home_dm_button"),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Send,
                                contentDescription = stringResource(id = R.string.action_messages),
                                tint = palette.textPrimary,
                                modifier = Modifier.size(25.dp),
                            )
                        }
                        if (unreadMessagesCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 4.dp, end = 4.dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(FreegramHeartRed),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = unreadMessagesCount.coerceAtMost(9).toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }

            // Stories Tray
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.testTag("stories_tray"),
                ) {
                    // Your Story
                    if (currentUser != null) {
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(74.dp)
                                    .clickable {
                                        if (myStories.isNotEmpty()) onOpenUserStory(currentUser.id)
                                        else onAddStoryClick()
                                    }
                                    .testTag("your_story_item"),
                            ) {
                                Box {
                                    FreegramAvatar(
                                        avatarKey = currentUser.avatarKey,
                                        username = currentUser.username,
                                        size = 64.dp,
                                        hasStory = myStories.isNotEmpty(),
                                        isUnviewedStory = myStories.any { !it.isViewed },
                                    )
                                    if (myStories.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .offset(x = (-2).dp, y = (-2).dp)
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(palette.background)
                                                .padding(2.dp)
                                                .clip(CircleShape)
                                                .background(FreegramBlue)
                                                .clickable { onAddStoryClick() },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Add,
                                                contentDescription = "Add to story",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp),
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(
                                    text = stringResource(id = R.string.your_story),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }

                    items(orderedStoryAuthors, key = { it }) { authorId ->
                        val userStories = storiesByUser[authorId].orEmpty()
                        val lead = userStories.first()
                        val hasUnviewed = userStories.any { !it.isViewed }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(74.dp)
                                .clickable { onOpenUserStory(authorId) }
                                .testTag("story_item_$authorId"),
                        ) {
                            FreegramAvatar(
                                avatarKey = lead.avatarKey,
                                username = lead.username,
                                size = 64.dp,
                                hasStory = true,
                                isUnviewedStory = hasUnviewed,
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = lead.username,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (hasUnviewed) palette.textPrimary else palette.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                HorizontalDivider(color = palette.divider.copy(alpha = 0.45f), thickness = 0.5.dp)
            }

            // Feed posts
            items(posts, key = { it.id }) { post ->
                val userStories = storiesByUser[post.userId].orEmpty()
                FeedPostCard(
                    post = post,
                    hasStory = userStories.isNotEmpty(),
                    hasUnviewedStory = userStories.any { !it.isViewed },
                    onAuthorClick = { uid ->
                        if (userStories.isNotEmpty()) onOpenUserStory(uid) else onAuthorClick(uid)
                    },
                    onLikeToggle = { likedOnly -> onLikePost(post.id, likedOnly) },
                    onCommentClick = { onCommentClick(post) },
                    onShareClick = { onShareClick(post) },
                    onSaveToggle = { onSavePost(post.id) },
                    onCopyLink = { onCopyLink(post) },
                    onHashtagClick = onHashtagClick,
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun StoryViewerScreen(
    stories: List<StoryItem>,
    initialIndex: Int = 0,
    onMarkViewed: (String) -> Unit,
    onSendReply: (userId: String, replyText: String) -> Unit,
    onClose: () -> Unit,
) {
    var currentIndex by remember { mutableIntStateOf(initialIndex.coerceIn(0, (stories.size - 1).coerceAtLeast(0))) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isPaused by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    var likedThisStory by remember { mutableStateOf(false) }

    if (stories.isEmpty()) {
        onClose()
        return
    }

    val currentStory = stories[currentIndex]

    LaunchedEffect(currentStory.id) {
        onMarkViewed(currentStory.id)
        likedThisStory = false
        progress = 0f
    }

    LaunchedEffect(currentIndex, isPaused) {
        while (progress < 1f) {
            delay(50)
            if (!isPaused) {
                progress += 0.01f // ~5 seconds per story
            }
        }
        if (currentIndex < stories.lastIndex) {
            currentIndex++
            progress = 0f
        } else {
            onClose()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        FreegramMediaView(
            mediaKey = currentStory.mediaKey,
            filterName = currentStory.filterName,
            contentDescription = "Story by ${currentStory.username}",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Tap navigation zones (left = prev, right = next, press = pause)
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .pointerInput(currentIndex) {
                        detectTapGestures(
                            onLongPress = { isPaused = true },
                            onPress = {
                                tryAwaitRelease()
                                isPaused = false
                            },
                            onTap = {
                                if (currentIndex > 0) {
                                    currentIndex--
                                    progress = 0f
                                } else {
                                    progress = 0f
                                }
                            },
                        )
                    },
            )
            Box(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight()
                    .pointerInput(currentIndex) {
                        detectTapGestures(
                            onLongPress = { isPaused = true },
                            onPress = {
                                tryAwaitRelease()
                                isPaused = false
                            },
                            onTap = {
                                if (currentIndex < stories.lastIndex) {
                                    currentIndex++
                                    progress = 0f
                                } else {
                                    onClose()
                                }
                            },
                        )
                    },
            )
        }

        // Top gradient + segmented progress bars + author header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent)
                    )
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                stories.forEachIndexed { idx, _ ->
                    val segProgress = when {
                        idx < currentIndex -> 1f
                        idx == currentIndex -> progress.coerceIn(0f, 1f)
                        else -> 0f
                    }
                    LinearProgressIndicator(
                        progress = { segProgress },
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.35f),
                        modifier = Modifier
                            .weight(1f)
                            .height(2.5.dp)
                            .clip(RoundedCornerShape(2.dp)),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FreegramAvatar(
                    avatarKey = currentStory.avatarKey,
                    username = currentStory.username,
                    size = 34.dp,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentStory.username,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatRelativeTime(currentStory.createdAtMillis),
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 12.sp,
                        )
                    }
                    if (currentStory.musicTrack.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentStory.musicTrack,
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close story",
                        tint = Color.White,
                    )
                }
            }
        }

        // Story text overlay sticker
        if (currentStory.textOverlay.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.56f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text(
                    text = currentStory.textOverlay,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Bottom reply bar
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))
                    )
                )
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = replyText,
                onValueChange = {
                    isPaused = true
                    replyText = it
                },
                placeholder = { Text("Send message", color = Color.White.copy(alpha = 0.7f)) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White.copy(alpha = 0.8f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                ),
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    likedThisStory = !likedThisStory
                    if (likedThisStory) {
                        onSendReply(currentStory.userId, "Reacted ❤️ to your story")
                    }
                },
            ) {
                Icon(
                    imageVector = if (likedThisStory) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like story",
                    tint = if (likedThisStory) FreegramHeartRed else Color.White,
                )
            }
            if (replyText.isNotBlank()) {
                IconButton(
                    onClick = {
                        onSendReply(currentStory.userId, "Replied to story: $replyText")
                        replyText = ""
                        isPaused = false
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Send story reply",
                        tint = Color.White,
                    )
                }
            }
        }
    }
}
