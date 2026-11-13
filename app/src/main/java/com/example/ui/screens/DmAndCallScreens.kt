package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.ChatMessage
import com.example.data.model.ChatThread
import com.example.data.model.FeedPost
import com.example.data.model.MediaFilter
import com.example.data.model.UserAccount
import com.example.ui.components.FreegramAvatar
import com.example.ui.components.FreegramMediaView
import com.example.ui.components.formatRelativeTime
import com.example.ui.theme.FreegramBlue
import com.example.ui.theme.FreegramHeartRed
import com.example.ui.theme.LocalFreegramPalette
import kotlinx.coroutines.delay
import java.io.File
import java.util.UUID

@Composable
fun DmInboxScreen(
    currentUser: UserAccount?,
    threads: List<ChatThread>,
    allUsers: List<UserAccount>,
    onBack: () -> Unit,
    onThreadClick: (ChatThread) -> Unit,
    onStartChatWithUser: (UserAccount) -> Unit,
) {
    val palette = LocalFreegramPalette.current
    var query by remember { mutableStateOf("") }

    val filteredThreads = remember(threads, query) {
        if (query.isBlank()) threads
        else threads.filter {
            it.peerUsername.contains(query, ignoreCase = true) ||
                it.peerFullName.contains(query, ignoreCase = true)
        }
    }
    val activePeers = remember(allUsers, currentUser?.id) {
        allUsers.filter { it.id != currentUser?.id }
    }

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
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("dm_back_button"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = palette.textPrimary,
                )
            }
            Text(
                text = currentUser?.username ?: "Messages",
                style = MaterialTheme.typography.titleLarge,
                color = palette.textPrimary,
            )
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search messages") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = palette.surfaceVariant,
                unfocusedContainerColor = palette.surfaceVariant,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Active creators tray
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(activePeers, key = { it.id }) { peer ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(68.dp)
                        .clickable { onStartChatWithUser(peer) },
                ) {
                    FreegramAvatar(
                        avatarKey = peer.avatarKey,
                        username = peer.username,
                        size = 56.dp,
                        isActive = peer.isActiveNow,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = peer.fullName.substringBefore(" "),
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textSecondary,
                        maxLines = 1,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Messages",
            style = MaterialTheme.typography.titleMedium,
            color = palette.textPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("dm_threads_list"),
        ) {
            items(filteredThreads, key = { it.id }) { thread ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onThreadClick(thread) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("dm_thread_${thread.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FreegramAvatar(
                        avatarKey = thread.peerAvatarKey,
                        username = thread.peerUsername,
                        size = 52.dp,
                        isActive = thread.isPeerActive,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = thread.peerFullName,
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary,
                            fontWeight = if (thread.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (thread.isTyping) "Typing..." else "${thread.lastMessage} · ${formatRelativeTime(thread.lastTimestampMillis)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (thread.isTyping) FreegramBlue else if (thread.unreadCount > 0) palette.textPrimary else palette.textSecondary,
                            fontWeight = if (thread.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (thread.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(FreegramBlue),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatDetailScreen(
    thread: ChatThread,
    messages: List<ChatMessage>,
    postsById: Map<String, FeedPost>,
    currentUserId: String,
    onBack: () -> Unit,
    onStartAudioCall: () -> Unit,
    onStartVideoCall: () -> Unit,
    onSendMessage: (text: String, mediaKey: String, voiceDurationSec: Int, voicePath: String) -> Unit,
    onDoubleTapMessage: (String) -> Unit,
    onOpenSharedPost: (FeedPost) -> Unit,
) {
    val palette = LocalFreegramPalette.current
    val context = LocalContext.current
    var draft by remember { mutableStateOf("") }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var currentVoiceFile by remember { mutableStateOf("") }
    var playingMsgId by remember { mutableStateOf<String?>(null) }
    var activePlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    var hasAudioPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val audioPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasAudioPerm = it },
    )

    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onSendMessage("", uri.toString(), 0, "")
            }
        },
    )

    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            recordingSeconds = 0
            while (isRecordingVoice) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { recorder?.release() }
            runCatching { activePlayer?.release() }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        // Header with Audio & Video call actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to inbox",
                    tint = palette.textPrimary,
                )
            }
            FreegramAvatar(
                avatarKey = thread.peerAvatarKey,
                username = thread.peerUsername,
                size = 36.dp,
                isActive = thread.isPeerActive,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = thread.peerFullName,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary,
                )
                Text(
                    text = if (thread.isTyping) "Typing..." else if (thread.isPeerActive) "Active now" else "@${thread.peerUsername}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (thread.isTyping) FreegramBlue else palette.textSecondary,
                )
            }
            IconButton(
                onClick = onStartAudioCall,
                modifier = Modifier.testTag("chat_audio_call_button"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = "Audio call",
                    tint = palette.textPrimary,
                )
            }
            IconButton(
                onClick = onStartVideoCall,
                modifier = Modifier.testTag("chat_video_call_button"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Videocam,
                    contentDescription = "Video call",
                    tint = palette.textPrimary,
                )
            }
        }
        HorizontalDivider(color = palette.divider, thickness = 0.5.dp)

        // Messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(messages, key = { it.id }) { msg ->
                val isMine = msg.senderId == currentUserId
                val sharedPost = msg.sharedPostId.takeIf { it.isNotBlank() }?.let { postsById[it] }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 18.dp,
                                    topEnd = 18.dp,
                                    bottomStart = if (isMine) 18.dp else 4.dp,
                                    bottomEnd = if (isMine) 4.dp else 18.dp,
                                )
                            )
                            .background(if (isMine) FreegramBlue else palette.surfaceVariant)
                            .pointerInput(msg.id) {
                                detectTapGestures(
                                    onDoubleTap = { onDoubleTapMessage(msg.id) }
                                )
                            }
                            .padding(10.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (sharedPost != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(palette.surface)
                                        .clickable { onOpenSharedPost(sharedPost) },
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        FreegramAvatar(
                                            avatarKey = sharedPost.userAvatarKey,
                                            username = sharedPost.username,
                                            size = 24.dp,
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = sharedPost.username,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = palette.textPrimary,
                                        )
                                    }
                                    FreegramMediaView(
                                        mediaKey = sharedPost.mediaKeys.firstOrNull() ?: "res:tokyo",
                                        filterName = sharedPost.filterName,
                                        contentDescription = "Shared post",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp),
                                    )
                                    Text(
                                        text = sharedPost.caption,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.textPrimary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(8.dp),
                                    )
                                }
                            }

                            if (msg.mediaKey.isNotBlank()) {
                                FreegramMediaView(
                                    mediaKey = msg.mediaKey,
                                    filterName = MediaFilter.NORMAL.name,
                                    contentDescription = "Sent media",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(170.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                )
                            }

                            if (msg.voiceDurationSec > 0) {
                                val isPlaying = playingMsgId == msg.id
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable {
                                            if (isPlaying) {
                                                runCatching { activePlayer?.stop(); activePlayer?.release() }
                                                activePlayer = null
                                                playingMsgId = null
                                            } else if (msg.voiceFilePath.isNotBlank() && File(msg.voiceFilePath).exists()) {
                                                runCatching {
                                                    activePlayer?.release()
                                                    activePlayer = MediaPlayer().apply {
                                                        setDataSource(msg.voiceFilePath)
                                                        prepare()
                                                        setOnCompletionListener {
                                                            playingMsgId = null
                                                        }
                                                        start()
                                                    }
                                                    playingMsgId = msg.id
                                                }
                                            } else {
                                                playingMsgId = msg.id
                                            }
                                        }
                                        .padding(4.dp),
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                        contentDescription = "Play voice note",
                                        tint = if (isMine) Color.White else palette.textPrimary,
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Voice note · 0:${msg.voiceDurationSec.toString().padStart(2, '0')}",
                                        color = if (isMine) Color.White else palette.textPrimary,
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                }
                            }

                            if (msg.text.isNotBlank() && msg.voiceDurationSec == 0) {
                                Text(
                                    text = msg.text,
                                    color = if (isMine) Color.White else palette.textPrimary,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
                    ) {
                        if (msg.isLiked) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Hearted message",
                                tint = FreegramHeartRed,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                        if (isMine && msg.id == messages.lastOrNull()?.id) {
                            Text(
                                text = if (msg.isSeen) "Seen" else "Sent",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textSecondary,
                            )
                        }
                    }
                }
            }

            if (thread.isTyping) {
                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(palette.surfaceVariant)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = "${thread.peerUsername} is typing...",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary,
                        )
                    }
                }
            }
        }

        // Voice recording status pill
        AnimatedVisibility(visible = isRecordingVoice) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FreegramHeartRed.copy(alpha = 0.14f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(FreegramHeartRed),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recording voice note... 0:${recordingSeconds.toString().padStart(2, '0')}",
                    color = FreegramHeartRed,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "Stop & Send",
                    color = FreegramBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        isRecordingVoice = false
                        runCatching { recorder?.stop(); recorder?.release() }
                        recorder = null
                        onSendMessage("", "", recordingSeconds.coerceAtLeast(1), currentVoiceFile)
                    },
                )
            }
        }

        // Composer bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {
                    mediaPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
            ) {
                Icon(
                    imageVector = Icons.Filled.PhotoLibrary,
                    contentDescription = "Send photo",
                    tint = palette.textPrimary,
                )
            }
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                placeholder = { Text("Message...") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = palette.divider,
                    unfocusedBorderColor = palette.divider,
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_message_input"),
            )
            Spacer(modifier = Modifier.width(4.dp))
            if (draft.isNotBlank()) {
                IconButton(
                    onClick = {
                        onSendMessage(draft, "", 0, "")
                        draft = ""
                    },
                    modifier = Modifier.testTag("chat_send_button"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send message",
                        tint = FreegramBlue,
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        if (!hasAudioPerm) {
                            audioPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else if (!isRecordingVoice) {
                            val outFile = File(context.cacheDir, "voice_${UUID.randomUUID()}.m4a")
                            currentVoiceFile = outFile.absolutePath
                            runCatching {
                                val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    MediaRecorder(context)
                                } else {
                                    @Suppress("DEPRECATION")
                                    MediaRecorder()
                                }
                                mr.apply {
                                    setAudioSource(MediaRecorder.AudioSource.MIC)
                                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                    setOutputFile(outFile.absolutePath)
                                    prepare()
                                    start()
                                }
                                recorder = mr
                            }
                            isRecordingVoice = true
                        } else {
                            isRecordingVoice = false
                            runCatching { recorder?.stop(); recorder?.release() }
                            recorder = null
                            onSendMessage("", "", recordingSeconds.coerceAtLeast(1), currentVoiceFile)
                        }
                    },
                    modifier = Modifier.testTag("chat_voice_button"),
                ) {
                    Icon(
                        imageVector = if (isRecordingVoice) Icons.Filled.Stop else Icons.Filled.Mic,
                        contentDescription = "Record voice note",
                        tint = if (isRecordingVoice) FreegramHeartRed else palette.textPrimary,
                    )
                }
            }
        }
    }
}

@Composable
fun CallScreen(
    peerUser: UserAccount,
    isVideoCall: Boolean,
    onEndCall: (durationSec: Int) -> Unit,
) {
    val context = LocalContext.current
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(isVideoCall) }
    var isCameraEnabled by remember { mutableStateOf(isVideoCall) }
    var useFrontCamera by remember { mutableStateOf(true) }
    var seconds by remember { mutableIntStateOf(0) }

    var hasCameraPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { results ->
            hasCameraPerm = results[Manifest.permission.CAMERA] == true
        },
    )

    LaunchedEffect(Unit) {
        val needed = buildList {
            if (isVideoCall && !hasCameraPerm) add(Manifest.permission.CAMERA)
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                add(Manifest.permission.RECORD_AUDIO)
            }
        }
        if (needed.isNotEmpty()) {
            permLauncher.launch(needed.toTypedArray())
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            seconds++
        }
    }

    val timerText = remember(seconds) {
        if (seconds < 3) "Calling..."
        else "%02d:%02d".format(seconds / 60, seconds % 60)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF1A162B), Color(0xFF0A0A0F)))
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        if (isCameraEnabled && hasCameraPerm) {
            CameraPreviewStage(
                useFrontCamera = useFrontCamera,
                onImageCaptureReady = {},
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f)),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FreegramAvatar(
                avatarKey = peerUser.avatarKey,
                username = peerUser.username,
                size = 96.dp,
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = peerUser.fullName,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timerText,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 15.sp,
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 36.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CallControlButton(
                icon = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                label = if (isMuted) "Muted" else "Mute",
                active = isMuted,
                onClick = { isMuted = !isMuted },
            )
            CallControlButton(
                icon = if (isCameraEnabled) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                label = "Video",
                active = isCameraEnabled,
                onClick = { isCameraEnabled = !isCameraEnabled },
            )
            if (isCameraEnabled) {
                CallControlButton(
                    icon = Icons.Filled.FlipCameraAndroid,
                    label = "Flip",
                    active = false,
                    onClick = { useFrontCamera = !useFrontCamera },
                )
            }
            CallControlButton(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                label = "Speaker",
                active = isSpeakerOn,
                onClick = { isSpeakerOn = !isSpeakerOn },
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(FreegramHeartRed)
                        .clickable { onEndCall(seconds) }
                        .testTag("end_call_button"),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.CallEnd,
                        contentDescription = "End call",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("End", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun CallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (active) Color.White else Color.White.copy(alpha = 0.2f))
                .clickable { onClick() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) Color.Black else Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, color = Color.White, fontSize = 12.sp)
    }
}
