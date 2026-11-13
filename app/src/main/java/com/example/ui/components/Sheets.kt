package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatThread
import com.example.data.model.NotificationItem
import com.example.data.model.PostComment
import com.example.data.model.UserAccount
import com.example.ui.theme.FreegramBlue
import com.example.ui.theme.FreegramHeartRed
import com.example.ui.theme.LocalFreegramPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    comments: List<PostComment>,
    currentUser: UserAccount?,
    onDismiss: () -> Unit,
    onAddComment: (text: String, parentCommentId: String?) -> Unit,
    onToggleCommentLike: (PostComment) -> Unit,
) {
    val palette = LocalFreegramPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draft by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<PostComment?>(null) }

    val topLevel = remember(comments) { comments.filter { it.parentCommentId == null } }
    val repliesByParent = remember(comments) {
        comments.filter { it.parentCommentId != null }.groupBy { it.parentCommentId }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.surface,
        contentColor = palette.textPrimary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 340.dp, max = 560.dp)
                .padding(bottom = 12.dp),
        ) {
            Text(
                text = "Comments",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 12.dp),
            )
            HorizontalDivider(color = palette.divider)

            if (topLevel.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No comments yet. Start the conversation!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                ) {
                    items(topLevel, key = { it.id }) { comment ->
                        CommentRow(
                            comment = comment,
                            isNested = false,
                            onLike = { onToggleCommentLike(comment) },
                            onReply = {
                                replyingTo = comment
                                draft = "@${comment.username} "
                            },
                        )
                        val nested = repliesByParent[comment.id].orEmpty()
                        nested.forEach { reply ->
                            CommentRow(
                                comment = reply,
                                isNested = true,
                                onLike = { onToggleCommentLike(reply) },
                                onReply = {
                                    replyingTo = comment
                                    draft = "@${reply.username} "
                                },
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = palette.divider)

            // Quick emoji bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                listOf("❤️", "🙌", "🔥", "👏", "😍", "😮", "😂").forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { draft += emoji }
                            .padding(4.dp),
                    )
                }
            }

            if (replyingTo != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.surfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Replying to @${replyingTo?.username}",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.textSecondary,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cancel reply",
                        tint = palette.textSecondary,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                replyingTo = null
                                draft = ""
                            },
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (currentUser != null) {
                    FreegramAvatar(
                        avatarKey = currentUser.avatarKey,
                        username = currentUser.username,
                        size = 34.dp,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = { Text("Add a comment...") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.divider,
                        unfocusedBorderColor = palette.divider,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("comment_input"),
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (draft.isNotBlank()) {
                            onAddComment(draft, replyingTo?.id)
                            draft = ""
                            replyingTo = null
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (draft.isNotBlank()) FreegramBlue else palette.surfaceVariant)
                        .testTag("comment_send_button"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Post comment",
                        tint = if (draft.isNotBlank()) Color.White else palette.textSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentRow(
    comment: PostComment,
    isNested: Boolean,
    onLike: () -> Unit,
    onReply: () -> Unit,
) {
    val palette = LocalFreegramPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isNested) 40.dp else 0.dp,
                top = 10.dp,
                bottom = 6.dp,
            ),
        verticalAlignment = Alignment.Top,
    ) {
        FreegramAvatar(
            avatarKey = comment.userAvatarKey,
            username = comment.username,
            size = if (isNested) 26.dp else 32.dp,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.username,
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.textPrimary,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formatRelativeTime(comment.createdAtMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary,
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textPrimary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Reply",
                style = MaterialTheme.typography.labelMedium,
                color = palette.textSecondary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onReply() },
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = onLike,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = if (comment.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like comment",
                    tint = if (comment.isLiked) FreegramHeartRed else palette.textSecondary,
                    modifier = Modifier.size(16.dp),
                )
            }
            if (comment.likesCount > 0) {
                Text(
                    text = comment.likesCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharePostBottomSheet(
    threads: List<ChatThread>,
    onDismiss: () -> Unit,
    onSendToThread: (ChatThread) -> Unit,
    onCopyLink: () -> Unit,
) {
    val palette = LocalFreegramPalette.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        contentColor = palette.textPrimary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = "Share to",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.surfaceVariant)
                    .clickable { onCopyLink() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = "Copy link",
                    tint = palette.textPrimary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Copy link",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Send in Direct Message",
                style = MaterialTheme.typography.labelMedium,
                color = palette.textSecondary,
            )
            Spacer(modifier = Modifier.height(8.dp))

            threads.forEach { thread ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSendToThread(thread) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FreegramAvatar(
                        avatarKey = thread.peerAvatarKey,
                        username = thread.peerUsername,
                        size = 42.dp,
                        isActive = thread.isPeerActive,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = thread.peerFullName,
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary,
                        )
                        Text(
                            text = "@${thread.peerUsername}",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(FreegramBlue)
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                    ) {
                        Text(
                            text = "Send",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsBottomSheet(
    notifications: List<NotificationItem>,
    onDismiss: () -> Unit,
    onNotificationClick: (NotificationItem) -> Unit,
) {
    val palette = LocalFreegramPalette.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        contentColor = palette.textPrimary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyColumn {
                items(notifications, key = { it.id }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onNotificationClick(item) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FreegramAvatar(
                            avatarKey = item.actorAvatarKey,
                            username = item.actorUsername,
                            size = 42.dp,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${item.actorUsername} ${item.message}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textPrimary,
                            )
                            Text(
                                text = formatRelativeTime(item.createdAtMillis),
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textSecondary,
                            )
                        }
                        if (!item.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(FreegramBlue),
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
