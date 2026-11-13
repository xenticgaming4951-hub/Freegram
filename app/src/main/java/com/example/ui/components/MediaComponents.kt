package com.example.ui.components

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.MediaFilter
import com.example.ui.theme.FreegramActiveGreen
import com.example.ui.theme.FreegramStoryRingGradient
import com.example.ui.theme.FreegramVerifiedBlue
import com.example.ui.theme.LocalFreegramPalette

fun MediaFilter.toColorFilter(): ColorFilter? {
    val matrix = when (this) {
        MediaFilter.NORMAL -> return null
        MediaFilter.CLARENDON -> ColorMatrix(
            floatArrayOf(
                1.18f, 0f, 0f, 0f, -10f,
                0f, 1.16f, 0f, 0f, -5f,
                0f, 0f, 1.24f, 0f, 6f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        MediaFilter.GINGHAM -> ColorMatrix(
            floatArrayOf(
                0.95f, 0.08f, 0.05f, 0f, 14f,
                0.04f, 0.96f, 0.04f, 0f, 12f,
                0.04f, 0.05f, 0.90f, 0f, 18f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        MediaFilter.MOON -> ColorMatrix().apply {
            setToSaturation(0f)
            val contrast = ColorMatrix(
                floatArrayOf(
                    1.15f, 0f, 0f, 0f, -8f,
                    0f, 1.15f, 0f, 0f, -8f,
                    0f, 0f, 1.18f, 0f, -4f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            timesAssign(contrast)
        }
        MediaFilter.LARK -> ColorMatrix(
            floatArrayOf(
                1.08f, 0.04f, 0f, 0f, 12f,
                0f, 1.12f, 0.03f, 0f, 10f,
                0f, 0.04f, 1.06f, 0f, 6f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        MediaFilter.REYES -> ColorMatrix(
            floatArrayOf(
                1.08f, 0.12f, 0.04f, 0f, 16f,
                0.06f, 1.00f, 0.04f, 0f, 10f,
                0.02f, 0.04f, 0.85f, 0f, 4f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }
    return ColorFilter.colorMatrix(matrix)
}

fun resolveMediaFilter(name: String): MediaFilter =
    MediaFilter.entries.firstOrNull { it.name == name } ?: MediaFilter.NORMAL

fun drawableForMediaKey(key: String): Int? = when (key) {
    "res:tokyo" -> R.drawable.img_feed_tokyo
    "res:cafe" -> R.drawable.img_feed_cafe
    "res:ocean" -> R.drawable.img_reel_ocean
    "res:studio" -> R.drawable.img_feed_studio
    "res:logo" -> R.drawable.img_freegram_logo
    else -> null
}

@Composable
fun FreegramMediaView(
    mediaKey: String,
    filterName: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    isVideo: Boolean = false,
    isPaused: Boolean = false,
    isMuted: Boolean = true,
    onToggleMute: (() -> Unit)? = null,
) {
    val filter = remember(filterName) { resolveMediaFilter(filterName).toColorFilter() }
    val drawableRes = remember(mediaKey) { drawableForMediaKey(mediaKey) }
    val isFileOrContentVideo = isVideo && (mediaKey.startsWith("content://") || mediaKey.startsWith("file://"))

    Box(modifier = modifier) {
        if (isFileOrContentVideo) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoURI(Uri.parse(mediaKey))
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            val vol = if (isMuted) 0f else 1f
                            mp.setVolume(vol, vol)
                            if (!isPaused) start()
                        }
                    }
                },
                update = { view ->
                    if (isPaused && view.isPlaying) {
                        view.pause()
                    } else if (!isPaused && !view.isPlaying) {
                        view.start()
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            val motionTransition = rememberInfiniteTransition(label = "videoMotion")
            val scaleAnim by motionTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = if (isVideo && !isPaused) 1.08f else 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 8000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "kenBurns",
            )
            val effectiveScale = if (isVideo) scaleAnim else 1f

            if (drawableRes != null) {
                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = contentDescription,
                    colorFilter = filter,
                    contentScale = contentScale,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = effectiveScale
                            scaleY = effectiveScale
                        },
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(mediaKey)
                        .crossfade(true)
                        .build(),
                    contentDescription = contentDescription,
                    colorFilter = filter,
                    contentScale = contentScale,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = effectiveScale
                            scaleY = effectiveScale
                        },
                )
            }
        }

        if (isVideo && onToggleMute != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable { onToggleMute() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isMuted) "Unmute video" else "Mute video",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        if (isVideo && isPaused) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Paused",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp),
                )
            }
        }
    }
}

@Composable
fun FreegramAvatar(
    avatarKey: String,
    username: String,
    size: Dp = 40.dp,
    hasStory: Boolean = false,
    isUnviewedStory: Boolean = false,
    isActive: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val palette = LocalFreegramPalette.current
    val ringWidth = if (size >= 60.dp) 2.8.dp else 2.dp
    val ringPadding = if (hasStory) ringWidth + 2.5.dp else 0.dp
    val avatarGradient = remember(avatarKey, username) {
        when (avatarKey) {
            "avatar:1" -> listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045))
            "avatar:2" -> listOf(Color(0xFF00C6FF), Color(0xFF0072FF))
            "avatar:3" -> listOf(Color(0xFFD38312), Color(0xFFA83279))
            "avatar:4" -> listOf(Color(0xFF11998E), Color(0xFF38EF7D))
            "avatar:5" -> listOf(Color(0xFF654EA3), Color(0xFFEAAFC8))
            "avatar:6" -> listOf(Color(0xFFFF512F), Color(0xFFDD2476))
            else -> listOf(Color(0xFF515BD4), Color(0xFFDD2A7B))
        }
    }

    Box(
        modifier = modifier.size(size + ringPadding * 2),
        contentAlignment = Alignment.Center,
    ) {
        if (hasStory) {
            val ringModifier = if (isUnviewedStory) {
                Modifier
                    .fillMaxSize()
                    .border(width = ringWidth, brush = FreegramStoryRingGradient, shape = CircleShape)
            } else {
                Modifier
                    .fillMaxSize()
                    .border(width = 1.5.dp, color = palette.divider, shape = CircleShape)
            }
            Box(modifier = ringModifier)
        }

        val innerModifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(width = 1.dp, color = palette.background, shape = CircleShape)

        val drawable = drawableForMediaKey(avatarKey)
        if (drawable != null) {
            Image(
                painter = painterResource(id = drawable),
                contentDescription = "$username avatar",
                contentScale = ContentScale.Crop,
                modifier = innerModifier,
            )
        } else if (avatarKey.startsWith("content://") || avatarKey.startsWith("file://")) {
            AsyncImage(
                model = avatarKey,
                contentDescription = "$username avatar",
                contentScale = ContentScale.Crop,
                modifier = innerModifier,
            )
        } else {
            val initial = username.trim().removePrefix("@").firstOrNull()?.uppercaseChar()?.toString() ?: "F"
            Box(
                modifier = innerModifier.background(Brush.linearGradient(avatarGradient)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initial,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.42f).sp,
                )
            }
        }

        if (isActive) {
            val dotSize = (size * 0.28f).coerceIn(10.dp, 16.dp)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(palette.background)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(FreegramActiveGreen)
            )
        }
    }
}

@Composable
fun VerifiedBadge(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Filled.Verified,
        contentDescription = "Verified account",
        tint = FreegramVerifiedBlue,
        modifier = modifier.size(15.dp),
    )
}

@Composable
fun DoubleTapHeartOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                initialScale = 0.2f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            ) + fadeIn(tween(120)),
            exit = scaleOut(targetScale = 1.35f, animationSpec = tween(220)) + fadeOut(tween(220)),
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.94f),
                modifier = Modifier.size(96.dp),
            )
        }
    }
}

@Composable
fun BrandWordmark(
    modifier: Modifier = Modifier,
    fontSizeSp: Float = 30f,
    color: Color? = null,
) {
    val palette = LocalFreegramPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = "Freegram",
            fontFamily = com.example.ui.theme.GrandHotelFontFamily,
            fontSize = fontSizeSp.sp,
            color = color ?: palette.textPrimary,
        )
    }
}

fun formatCompactCount(count: Int): String = when {
    count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000f).replace(".0M", "M")
    count >= 10_000 -> String.format("%.1fK", count / 1_000f).replace(".0K", "K")
    count >= 1_000 -> String.format("%,d", count)
    else -> count.toString()
}

fun formatRelativeTime(timestampMillis: Long): String {
    val diff = (System.currentTimeMillis() - timestampMillis).coerceAtLeast(0L)
    val mins = diff / 60_000L
    val hours = mins / 60L
    val days = hours / 24L
    return when {
        mins < 1 -> "Just now"
        mins < 60 -> "${mins}m"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}d"
        else -> "${days / 7}w"
    }
}
