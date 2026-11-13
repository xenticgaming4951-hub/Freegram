package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeedPost
import com.example.data.model.PostMediaType
import com.example.ui.theme.FreegramBlue
import com.example.ui.theme.FreegramHeartRed
import com.example.ui.theme.LocalFreegramPalette
import kotlinx.coroutines.delay

@Composable
fun FeedPostCard(
    post: FeedPost,
    hasUnviewedStory: Boolean,
    hasStory: Boolean,
    onAuthorClick: (String) -> Unit,
    onLikeToggle: (Boolean) -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveToggle: () -> Unit,
    onCopyLink: () -> Unit,
    onHashtagClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalFreegramPalette.current
    var showDoubleTapHeart by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }
    var captionExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(showDoubleTapHeart) {
        if (showDoubleTapHeart) {
            delay(750)
            showDoubleTapHeart = false
        }
    }

    val likeScale by animateFloatAsState(
        targetValue = if (post.isLiked) 1.18f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "likeBounce",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.background)
            .testTag("post_card_${post.id}"),
    ) {
        // Post header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FreegramAvatar(
                avatarKey = post.userAvatarKey,
                username = post.username,
                size = 34.dp,
                hasStory = hasStory,
                isUnviewedStory = hasUnviewedStory,
                modifier = Modifier.clickable { onAuthorClick(post.userId) },
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onAuthorClick(post.userId) },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = post.username,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (post.isUserVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        VerifiedBadge()
                    }
                }
                if (post.location.isNotBlank()) {
                    Text(
                        text = post.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.testTag("post_more_${post.id}"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Post options",
                        tint = palette.textPrimary,
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(if (post.isSaved) "Remove from Saved" else "Save post") },
                        onClick = {
                            showMenu = false
                            onSaveToggle()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Share to DM") },
                        onClick = {
                            showMenu = false
                            onShareClick()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Copy link") },
                        onClick = {
                            showMenu = false
                            onCopyLink()
                        },
                    )
                }
            }
        }

        // Media stage (single photo, carousel, or video)
        val mediaList = post.mediaKeys.ifEmpty { listOf("res:tokyo") }
        val pagerState = rememberPagerState(pageCount = { mediaList.size })
        val isVideoPost = post.mediaType == PostMediaType.VIDEO.name

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(palette.surfaceVariant)
                .pointerInput(post.id) {
                    detectTapGestures(
                        onDoubleTap = {
                            showDoubleTapHeart = true
                            onLikeToggle(true)
                        }
                    )
                },
        ) {
            if (mediaList.size > 1) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    FreegramMediaView(
                        mediaKey = mediaList[page],
                        filterName = post.filterName,
                        contentDescription = "Post media ${page + 1} by ${post.username}",
                        modifier = Modifier.fillMaxSize(),
                        isVideo = false,
                    )
                }
                // Slide pill indicator (top right)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.68f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1}/${mediaList.size}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                FreegramMediaView(
                    mediaKey = mediaList.first(),
                    filterName = post.filterName,
                    contentDescription = "Post media by ${post.username}",
                    modifier = Modifier.fillMaxSize(),
                    isVideo = isVideoPost,
                    isMuted = isMuted,
                    onToggleMute = if (isVideoPost) ({ isMuted = !isMuted }) else null,
                )
            }

            DoubleTapHeartOverlay(
                visible = showDoubleTapHeart,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Action Bar + Carousel Dots
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { onLikeToggle(false) },
                    modifier = Modifier.testTag("like_button_${post.id}"),
                ) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like post",
                        tint = if (post.isLiked) FreegramHeartRed else palette.textPrimary,
                        modifier = Modifier
                            .size(26.dp)
                            .graphicsLayer {
                                scaleX = likeScale
                                scaleY = likeScale
                            },
                    )
                }
                IconButton(
                    onClick = onCommentClick,
                    modifier = Modifier.testTag("comment_button_${post.id}"),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(24.dp),
                    )
                }
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier.testTag("share_button_${post.id}"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Share post",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(23.dp),
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onSaveToggle,
                    modifier = Modifier.testTag("save_button_${post.id}"),
                ) {
                    Icon(
                        imageVector = if (post.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save post",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(25.dp),
                    )
                }
            }

            if (mediaList.size > 1) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(mediaList.size) { index ->
                        val active = pagerState.currentPage == index
                        val dotColor by animateColorAsState(
                            targetValue = if (active) FreegramBlue else palette.textSecondary.copy(alpha = 0.4f),
                            label = "dotColor",
                        )
                        Box(
                            modifier = Modifier
                                .size(if (active) 7.dp else 5.dp)
                                .clip(CircleShape)
                                .background(dotColor),
                        )
                    }
                }
            }
        }

        // Likes + Caption + Comments Preview + Timestamp
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
        ) {
            Text(
                text = "${formatCompactCount(post.likesCount)} likes",
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary,
            )

            Spacer(modifier = Modifier.height(4.dp))

            val annotatedCaption = remember(post.username, post.caption, palette.textPrimary) {
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = palette.textPrimary)) {
                        append(post.username)
                        append(" ")
                    }
                    val tokens = post.caption.split(" ")
                    tokens.forEachIndexed { idx, token ->
                        val isTag = token.startsWith("#") || token.startsWith("@")
                        withStyle(
                            SpanStyle(
                                color = if (isTag) FreegramBlue else palette.textPrimary,
                                fontWeight = if (isTag) FontWeight.Medium else FontWeight.Normal,
                            )
                        ) {
                            append(token)
                        }
                        if (idx < tokens.lastIndex) append(" ")
                    }
                }
            }

            Text(
                text = annotatedCaption,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (captionExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable {
                    if (post.hashtags.isNotEmpty() && captionExpanded) {
                        onHashtagClick(post.hashtags.first())
                    } else {
                        captionExpanded = !captionExpanded
                    }
                },
            )

            if (post.commentsCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "View all ${post.commentsCount} comments",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
                    modifier = Modifier.clickable { onCommentClick() },
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${formatRelativeTime(post.createdAtMillis)} ago",
                style = MaterialTheme.typography.labelSmall,
                color = palette.textSecondary,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        HorizontalDivider(color = palette.divider.copy(alpha = 0.45f), thickness = 0.5.dp)
    }
}
