package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.MediaFilter
import com.example.data.model.UserAccount
import com.example.ui.components.FreegramAvatar
import com.example.ui.components.FreegramMediaView
import com.example.ui.theme.FreegramBlue
import com.example.ui.theme.FreegramHeartRed
import com.example.ui.theme.FreegramLiveGradient
import com.example.ui.theme.LocalFreegramPalette
import kotlinx.coroutines.delay
import java.io.File
import java.util.UUID

enum class CreateContentType(val label: String) {
    POST("Post"),
    STORY("Story"),
    REEL("Reel"),
    LIVE("Go Live"),
}

@Composable
fun CreateModalScreen(
    initialMode: CreateContentType = CreateContentType.POST,
    onClose: () -> Unit,
    onPublishPost: (mediaKeys: List<String>, isVideo: Boolean, filter: MediaFilter, caption: String, location: String) -> Unit,
    onPublishStory: (mediaKey: String, filter: MediaFilter, textOverlay: String, musicTrack: String) -> Unit,
    onPublishReel: (mediaKey: String, isVideoUri: Boolean, filter: MediaFilter, caption: String, audioTrack: String) -> Unit,
    onStartLive: (title: String) -> Unit,
) {
    val palette = LocalFreegramPalette.current
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf(initialMode) }
    var selectedFilter by remember { mutableStateOf(MediaFilter.NORMAL) }
    var selectedMediaKeys by remember { mutableStateOf(listOf("res:tokyo")) }
    var isVideoSelected by remember { mutableStateOf(false) }

    var caption by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var storyOverlay by remember { mutableStateOf("") }
    var storyMusic by remember { mutableStateOf("Midnight City Lights - Synthwave") }
    var reelAudio by remember { mutableStateOf("Original Audio • Freegram") }
    var liveTitle by remember { mutableStateOf("Behind the lens live Q&A") }

    var useCameraMode by remember { mutableStateOf(false) }
    var useFrontCamera by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (granted) useCameraMode = true
        },
    )

    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 4),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                selectedMediaKeys = uris.map { it.toString() }
                val firstMime = context.contentResolver.getType(uris.first()) ?: ""
                isVideoSelected = firstMime.startsWith("video/")
                useCameraMode = false
            }
        },
    )

    var imageCaptureRef by remember { mutableStateOf<ImageCapture?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("create_close_button"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close creator",
                    tint = palette.textPrimary,
                )
            }
            Text(
                text = "New ${selectedType.label}",
                style = MaterialTheme.typography.titleLarge,
                color = palette.textPrimary,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = {
                    when (selectedType) {
                        CreateContentType.POST -> onPublishPost(
                            selectedMediaKeys,
                            isVideoSelected,
                            selectedFilter,
                            caption.ifBlank { "Captured on Freegram #moments #photography" },
                            location,
                        )
                        CreateContentType.STORY -> onPublishStory(
                            selectedMediaKeys.first(),
                            selectedFilter,
                            storyOverlay,
                            storyMusic,
                        )
                        CreateContentType.REEL -> onPublishReel(
                            selectedMediaKeys.first(),
                            isVideoSelected && selectedMediaKeys.first().startsWith("content://"),
                            selectedFilter,
                            caption.ifBlank { "New Reel on Freegram #reels #trending" },
                            reelAudio,
                        )
                        CreateContentType.LIVE -> onStartLive(liveTitle.ifBlank { "Live on Freegram" })
                    }
                },
                modifier = Modifier.testTag("create_share_button"),
            ) {
                Text(
                    text = if (selectedType == CreateContentType.LIVE) "Start Live" else "Share",
                    color = FreegramBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            // Preview Stage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (selectedType == CreateContentType.REEL || selectedType == CreateContentType.STORY) 0.85f else 1f)
                    .background(Color.Black),
            ) {
                if (useCameraMode && hasCameraPermission) {
                    CameraPreviewStage(
                        useFrontCamera = useFrontCamera,
                        onImageCaptureReady = { imageCaptureRef = it },
                        modifier = Modifier.fillMaxSize(),
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = { useFrontCamera = !useFrontCamera },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f)),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FlipCameraAndroid,
                                contentDescription = "Flip camera",
                                tint = Color.White,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .border(4.dp, Color.White, CircleShape)
                                .padding(6.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable {
                                    capturePhotoToCache(context, imageCaptureRef) { uri ->
                                        if (uri != null) {
                                            selectedMediaKeys = listOf(uri.toString())
                                            isVideoSelected = false
                                            useCameraMode = false
                                        }
                                    }
                                },
                        )
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                } else {
                    FreegramMediaView(
                        mediaKey = selectedMediaKeys.first(),
                        filterName = selectedFilter.name,
                        contentDescription = "Create preview",
                        isVideo = isVideoSelected || selectedType == CreateContentType.REEL,
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (selectedType == CreateContentType.STORY && storyOverlay.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(20.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.55f))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Text(text = storyOverlay, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (selectedMediaKeys.size > 1) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text("${selectedMediaKeys.size} selected", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Source buttons: Gallery Photo Picker & Camera
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        mediaPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("create_gallery_button"),
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gallery")
                }
                OutlinedButton(
                    onClick = {
                        if (hasCameraPermission) {
                            useCameraMode = !useCameraMode
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("create_camera_button"),
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (useCameraMode) "Preview" else "Camera")
                }
            }

            // Quick studio sample presets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "res:tokyo" to "Tokyo",
                    "res:cafe" to "Cafe",
                    "res:ocean" to "Ocean",
                    "res:studio" to "Studio",
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = selectedMediaKeys.firstOrNull() == key,
                        onClick = {
                            selectedMediaKeys = listOf(key)
                            isVideoSelected = false
                            useCameraMode = false
                        },
                        label = { Text(label) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary,
                modifier = Modifier.padding(horizontal = 14.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Real-time Filter Strip
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(horizontal = 14.dp),
            ) {
                items(MediaFilter.entries, key = { it.name }) { filter ->
                    val isSelected = selectedFilter == filter
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { selectedFilter = filter }
                            .testTag("filter_chip_${filter.name}"),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) FreegramBlue else palette.divider,
                                    shape = RoundedCornerShape(10.dp),
                                ),
                        ) {
                            FreegramMediaView(
                                mediaKey = selectedMediaKeys.first(),
                                filterName = filter.name,
                                contentDescription = filter.label,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = filter.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) FreegramBlue else palette.textSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Contextual metadata fields
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                when (selectedType) {
                    CreateContentType.POST -> {
                        OutlinedTextField(
                            value = caption,
                            onValueChange = { caption = it },
                            label = { Text("Write a caption & #hashtags...") },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Add location (optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    CreateContentType.STORY -> {
                        OutlinedTextField(
                            value = storyOverlay,
                            onValueChange = { storyOverlay = it },
                            label = { Text("Text overlay sticker") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = storyMusic,
                            onValueChange = { storyMusic = it },
                            label = { Text("Music sticker track") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    CreateContentType.REEL -> {
                        OutlinedTextField(
                            value = caption,
                            onValueChange = { caption = it },
                            label = { Text("Reel caption & #hashtags") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = reelAudio,
                            onValueChange = { reelAudio = it },
                            label = { Text("Audio track name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    CreateContentType.LIVE -> {
                        OutlinedTextField(
                            value = liveTitle,
                            onValueChange = { liveTitle = it },
                            label = { Text("Live stream title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(
                            onClick = { onStartLive(liveTitle.ifBlank { "Live on Freegram" }) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(FreegramLiveGradient),
                        ) {
                            Text("Go Live Now", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom Post Type Switcher (Post / Story / Reel / Go Live)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.surface)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            CreateContentType.entries.forEach { type ->
                val active = selectedType == type
                Text(
                    text = type.label.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (active) palette.textPrimary else palette.textSecondary,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (active) palette.surfaceVariant else Color.Transparent)
                        .clickable { selectedType = type }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("create_type_${type.name}"),
                )
            }
        }
    }
}

@Composable
fun CameraPreviewStage(
    useFrontCamera: Boolean,
    onImageCaptureReady: (ImageCapture) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    DisposableEffect(useFrontCamera) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        val listener = Runnable {
            runCatching {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val capture = ImageCapture.Builder().build()
                onImageCaptureReady(capture)
                val selector = if (useFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
            }
        }
        cameraProviderFuture.addListener(listener, executor)
        onDispose {
            runCatching { cameraProviderFuture.get().unbindAll() }
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier,
    )
}

private fun capturePhotoToCache(
    context: Context,
    imageCapture: ImageCapture?,
    onCaptured: (Uri?) -> Unit,
) {
    if (imageCapture == null) {
        onCaptured(null)
        return
    }
    val file = File(context.cacheDir, "freegram_cap_${UUID.randomUUID()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onCaptured(Uri.fromFile(file))
            }

            override fun onError(exception: ImageCaptureException) {
                onCaptured(null)
            }
        },
    )
}

private data class LiveViewerComment(
    val id: String,
    val username: String,
    val text: String,
)

@Composable
fun LiveStreamScreen(
    hostUser: UserAccount?,
    title: String,
    onEndLive: () -> Unit,
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val cameraPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasCameraPermission = it },
    )
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var viewerCount by remember { mutableIntStateOf(42) }
    var commentDraft by remember { mutableStateOf("") }
    val liveComments = remember {
        mutableStateListOf(
            LiveViewerComment("l1", "maya.in.tokyo", "Welcome everyone! Lighting looks crisp!"),
            LiveViewerComment("l2", "leo.roast", "Morning from Seattle ☕"),
        )
    }
    val floatingHearts = remember { mutableStateListOf<String>() }

    // Simulate incoming viewer activity
    LaunchedEffect(Unit) {
        val sample = listOf(
            "aria.coast" to "Love the camera setup!",
            "zayn.frames" to "What lens are you shooting on today?",
            "loft.atelier" to "Sending hearts from NYC ❤️",
            "maya.in.tokyo" to "Drop a preset breakdown soon!",
        )
        var idx = 0
        while (true) {
            delay(3200)
            viewerCount += (1..4).random()
            val (u, msg) = sample[idx % sample.size]
            liveComments.add(LiveViewerComment("auto_${System.currentTimeMillis()}", u, msg))
            if (liveComments.size > 20) liveComments.removeAt(0)
            floatingHearts.add(UUID.randomUUID().toString())
            idx++
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
        if (hasCameraPermission) {
            CameraPreviewStage(
                useFrontCamera = true,
                onImageCaptureReady = {},
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            FreegramMediaView(
                mediaKey = "res:studio",
                filterName = MediaFilter.NORMAL.name,
                contentDescription = "Live broadcast backdrop",
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Top Live header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent))
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (hostUser != null) {
                FreegramAvatar(
                    avatarKey = hostUser.avatarKey,
                    username = hostUser.username,
                    size = 34.dp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = hostUser.username,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                    Text(
                        text = title,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        maxLines = 1,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(FreegramLiveGradient)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text("LIVE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.RemoveRedEye,
                    contentDescription = "Viewers",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(viewerCount.toString(), color = Color.White, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(6.dp))
            TextButton(onClick = onEndLive) {
                Text("End", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Floating animated hearts on right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 84.dp)
                .size(width = 60.dp, height = 220.dp),
        ) {
            floatingHearts.takeLast(8).forEach { heartId ->
                FloatingHeartBubble(
                    key = heartId,
                    onFinished = { floatingHearts.remove(heartId) },
                )
            }
        }

        // Bottom live comments + input
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f)))
                )
                .padding(12.dp),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(170.dp),
                reverseLayout = true,
            ) {
                items(liveComments.reversed(), key = { it.id }) { c ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = c.username,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = c.text,
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = 13.sp,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = commentDraft,
                    onValueChange = { commentDraft = it },
                    placeholder = { Text("Comment...", color = Color.White.copy(alpha = 0.7f)) },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White.copy(alpha = 0.8f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.45f),
                    ),
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (commentDraft.isNotBlank()) {
                            liveComments.add(
                                LiveViewerComment(
                                    UUID.randomUUID().toString(),
                                    hostUser?.username ?: "you",
                                    commentDraft.trim(),
                                )
                            )
                            commentDraft = ""
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send live comment",
                        tint = Color.White,
                    )
                }
                IconButton(
                    onClick = { floatingHearts.add(UUID.randomUUID().toString()) },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Send live heart",
                        tint = FreegramHeartRed,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingHeartBubble(
    key: String,
    onFinished: () -> Unit,
) {
    val progress = remember(key) { Animatable(0f) }
    val xDrift = remember(key) { (-16..16).random().toFloat() }

    LaunchedEffect(key) {
        progress.animateTo(1f, tween(1600))
        onFinished()
    }

    Icon(
        imageVector = Icons.Filled.Favorite,
        contentDescription = null,
        tint = FreegramHeartRed,
        modifier = Modifier
            .offset(x = (20 + xDrift * progress.value).dp, y = (190 - 180 * progress.value).dp)
            .alpha(1f - progress.value)
            .size(26.dp),
    )
}
