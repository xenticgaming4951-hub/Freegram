package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReelItem
import com.example.ui.components.DoubleTapHeartOverlay
import com.example.ui.components.FreegramAvatar
import com.example.ui.components.FreegramMediaView
import com.example.ui.components.VerifiedBadge
import com.example.ui.components.formatCompactCount
import com.example.ui.theme.FreegramHeartRed
import kotlinx.coroutines.delay

@Composable
fun ReelsScreen(
    reels: List<ReelItem>,
    onLikeReel: (reelId: String, likedOnly: Boolean) -> Unit,
    onSaveReel: (String) -> Unit,
    onFollowReelCreator: (String) -> Unit,
    onOpenAuthorProfile: (String) -> Unit,
    onOpenComments: (ReelItem) -> Unit,
    onShareReel: (ReelItem) -> Unit,
    onCreateReelClick: () -> Unit,
) {
    if (reels.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            Text("No Reels yet", color = Color.White)
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { reels.size })
    var isMuted by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("reels_pager"),
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val reel = reels[page]
            ReelPage(
                reel = reel,
                isMuted = isMuted,
                onToggleMute = { isMuted = !isMuted },
                onLike = { likedOnly -> onLikeReel(reel.id, likedOnly) },
                onSave = { onSaveReel(reel.id) },
                onFollow = { onFollowReelCreator(reel.id) },
                onAuthorClick = { onOpenAuthorProfile(reel.userId) },
                onCommentsClick = { onOpenComments(reel) },
                onShareClick = { onShareReel(reel) },
            )
        }

        // Top Reels bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Reels",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = onCreateReelClick,
                modifier = Modifier.testTag("reels_camera_button"),
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "Create Reel",
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
private fun ReelPage(
    reel: ReelItem,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    onLike: (Boolean) -> Unit,
    onSave: () -> Unit,
    onFollow: () -> Unit,
    onAuthorClick: () -> Unit,
    onCommentsClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    var isPaused by remember { mutableStateOf(false) }
    var captionExpanded by remember { mutableStateOf(false) }
    var showMutePill by remember { mutableStateOf(false) }
    var showDoubleTapHeart by remember { mutableStateOf(false) }

    LaunchedEffect(showMutePill) {
        if (showMutePill) {
            delay(900)
            showMutePill = false
        }
    }
    LaunchedEffect(showDoubleTapHeart) {
        if (showDoubleTapHeart) {
            delay(750)
            showDoubleTapHeart = false
        }
    }

    val spinTransition = rememberInfiniteTransition(label = "discSpin")
    val discRotation by spinTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "discAngle",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(reel.id) {
                detectTapGestures(
                    onDoubleTap = {
                        showDoubleTapHeart = true
                        onLike(true)
                    },
                    onLongPress = { isPaused = true },
                    onPress = {
                        tryAwaitRelease()
                        isPaused = false
                    },
                    onTap = {
                        onToggleMute()
                        showMutePill = true
                    },
                )
            },
    ) {
        FreegramMediaView(
            mediaKey = reel.mediaKey,
            filterName = reel.filterName,
            contentDescription = "Reel by ${reel.username}",
            contentScale = ContentScale.Crop,
            isVideo = true,
            isPaused = isPaused,
            isMuted = isMuted,
            modifier = Modifier.fillMaxSize(),
        )

        // Bottom readability scrim
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f))
                    )
                ),
        )

        DoubleTapHeartOverlay(
            visible = showDoubleTapHeart,
            modifier = Modifier.fillMaxSize(),
        )

        if (showMutePill) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isMuted) "Muted" else "Unmuted",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        // Right vertical action bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onLike(false) },
                    modifier = Modifier.testTag("reel_like_${reel.id}"),
                ) {
                    Icon(
                        imageVector = if (reel.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like reel",
                        tint = if (reel.isLiked) FreegramHeartRed else Color.White,
                        modifier = Modifier.size(30.dp),
                    )
                }
                Text(
                    text = formatCompactCount(reel.likesCount),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onCommentsClick) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment on reel",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Text(
                    text = formatCompactCount(reel.commentsCount),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Share reel",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Text(
                    text = formatCompactCount(reel.sharesCount),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            IconButton(onClick = onSave) {
                Icon(
                    imageVector = if (reel.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save reel",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }

            // Rotating audio disc
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .graphicsLayer { rotationZ = if (isPaused) 0f else discRotation }
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                    .background(Color(0xFF222222)),
                contentAlignment = Alignment.Center,
            ) {
                FreegramAvatar(
                    avatarKey = reel.userAvatarKey,
                    username = reel.username,
                    size = 24.dp,
                )
            }
        }

        // Bottom creator info, caption & scrolling audio track
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .padding(start = 14.dp, bottom = 22.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FreegramAvatar(
                    avatarKey = reel.userAvatarKey,
                    username = reel.username,
                    size = 34.dp,
                    modifier = Modifier.clickable { onAuthorClick() },
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = reel.username,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.clickable { onAuthorClick() },
                )
                if (reel.isUserVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    VerifiedBadge()
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                        .clickable { onFollow() }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = if (reel.isFollowing) "Following" else "Follow",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = reel.caption,
                color = Color.White,
                fontSize = 14.sp,
                maxLines = if (captionExpanded) 6 else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { captionExpanded = !captionExpanded },
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = reel.audioTrackName,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
