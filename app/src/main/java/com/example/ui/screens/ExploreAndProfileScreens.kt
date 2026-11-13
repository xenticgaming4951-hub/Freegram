package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.FeedPost
import com.example.data.model.PostMediaType
import com.example.data.model.ReelItem
import com.example.data.model.StoryItem
import com.example.data.model.UserAccount
import com.example.ui.components.FreegramAvatar
import com.example.ui.components.FreegramMediaView
import com.example.ui.components.VerifiedBadge
import com.example.ui.components.formatCompactCount
import com.example.ui.theme.FreegramBlue
import com.example.ui.theme.FreegramHeartRed
import com.example.ui.theme.LocalFreegramPalette

@Composable
fun ExploreScreen(
    initialQuery: String,
    users: List<UserAccount>,
    posts: List<FeedPost>,
    reels: List<ReelItem>,
    onUserClick: (UserAccount) -> Unit,
    onPostClick: (FeedPost) -> Unit,
    onReelClick: (ReelItem) -> Unit,
) {
    val palette = LocalFreegramPalette.current
    var query by remember(initialQuery) { mutableStateOf(initialQuery) }
    var searchTab by remember { mutableIntStateOf(0) } // 0 = Top/Posts, 1 = Accounts, 2 = Tags

    val cleanQuery = query.trim().removePrefix("#").removePrefix("@")

    val matchedUsers = remember(users, cleanQuery) {
        if (cleanQuery.isBlank()) emptyList()
        else users.filter {
            it.username.contains(cleanQuery, ignoreCase = true) ||
                it.fullName.contains(cleanQuery, ignoreCase = true)
        }
    }

    val matchedPosts = remember(posts, cleanQuery) {
        if (cleanQuery.isBlank()) posts
        else posts.filter {
            it.caption.contains(cleanQuery, ignoreCase = true) ||
                it.hashtags.any { tag -> tag.contains(cleanQuery, ignoreCase = true) } ||
                it.username.contains(cleanQuery, ignoreCase = true)
        }
    }

    val allTags = remember(posts, cleanQuery) {
        posts.flatMap { it.hashtags }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .filter { cleanQuery.isBlank() || it.key.contains(cleanQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding(),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search users or #hashtags") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
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
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("explore_search_input"),
        )

        if (cleanQuery.isNotBlank()) {
            TabRow(
                selectedTabIndex = searchTab,
                containerColor = palette.background,
                contentColor = palette.textPrimary,
                indicator = { tabPositions ->
                    if (searchTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[searchTab]),
                            color = palette.textPrimary,
                        )
                    }
                },
            ) {
                listOf("Posts", "Accounts", "Tags").forEachIndexed { idx, label ->
                    Tab(
                        selected = searchTab == idx,
                        onClick = { searchTab = idx },
                        text = { Text(label) },
                    )
                }
            }
        }

        if (cleanQuery.isNotBlank() && searchTab == 1) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(matchedUsers, key = { it.id }) { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUserClick(user) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FreegramAvatar(
                            avatarKey = user.avatarKey,
                            username = user.username,
                            size = 46.dp,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(user.username, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                                if (user.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    VerifiedBadge()
                                }
                            }
                            Text(user.fullName, style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                        }
                    }
                }
            }
        } else if (cleanQuery.isNotBlank() && searchTab == 2) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(allTags, key = { it.key }) { (tag, count) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                query = "#$tag"
                                searchTab = 0
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(21.dp))
                                .background(palette.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.Tag, contentDescription = null, tint = palette.textPrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("#$tag", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                            Text("$count posts", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                        }
                    }
                }
            }
        } else {
            // Masonry/Staggered Explore Grid combining Posts and Reels
            val exploreTiles = remember(matchedPosts, reels, cleanQuery) {
                val postTiles = matchedPosts.map { ExploreTile.PostTile(it) }
                val reelTiles = if (cleanQuery.isBlank()) reels.map { ExploreTile.ReelTile(it) }
                else reels.filter { it.caption.contains(cleanQuery, ignoreCase = true) }.map { ExploreTile.ReelTile(it) }
                (reelTiles + postTiles).distinctBy { it.key }
            }

            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(3),
                verticalItemSpacing = 2.dp,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("explore_masonry_grid"),
            ) {
                itemsIndexed(exploreTiles, key = { _, item -> item.key }) { index, tile ->
                    val isTall = tile is ExploreTile.ReelTile || index % 5 == 0
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(if (isTall) 0.65f else 1f)
                            .clickable {
                                when (tile) {
                                    is ExploreTile.PostTile -> onPostClick(tile.post)
                                    is ExploreTile.ReelTile -> onReelClick(tile.reel)
                                }
                            }
                            .testTag("explore_tile_${tile.key}"),
                    ) {
                        when (tile) {
                            is ExploreTile.PostTile -> {
                                FreegramMediaView(
                                    mediaKey = tile.post.mediaKeys.firstOrNull() ?: "res:tokyo",
                                    filterName = tile.post.filterName,
                                    contentDescription = tile.post.caption,
                                    modifier = Modifier.fillMaxSize(),
                                )
                                if (tile.post.mediaType == PostMediaType.CAROUSEL.name) {
                                    Icon(
                                        imageVector = Icons.Filled.Collections,
                                        contentDescription = "Carousel",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(18.dp),
                                    )
                                } else if (tile.post.mediaType == PostMediaType.VIDEO.name) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = "Video",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(20.dp),
                                    )
                                }
                            }
                            is ExploreTile.ReelTile -> {
                                FreegramMediaView(
                                    mediaKey = tile.reel.mediaKey,
                                    filterName = tile.reel.filterName,
                                    contentDescription = tile.reel.caption,
                                    modifier = Modifier.fillMaxSize(),
                                )
                                Icon(
                                    imageVector = Icons.Filled.VideoLibrary,
                                    contentDescription = "Reel",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(18.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private sealed class ExploreTile(val key: String) {
    data class PostTile(val post: FeedPost) : ExploreTile("p_${post.id}")
    data class ReelTile(val reel: ReelItem) : ExploreTile("r_${reel.id}")
}

@Composable
fun ProfileScreen(
    profileUser: UserAccount,
    isOwnProfile: Boolean,
    userPosts: List<FeedPost>,
    userReels: List<ReelItem>,
    savedPosts: List<FeedPost>,
    userStories: List<StoryItem>,
    onBack: (() -> Unit)?,
    onOpenSettings: () -> Unit,
    onEditProfile: () -> Unit,
    onShareProfile: () -> Unit,
    onToggleFollow: () -> Unit,
    onMessageUser: () -> Unit,
    onOpenStory: () -> Unit,
    onPostClick: (FeedPost) -> Unit,
    onReelClick: (ReelItem) -> Unit,
) {
    val palette = LocalFreegramPalette.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Posts, 1 = Reels, 2 = Saved

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = palette.textPrimary,
                        )
                    }
                }
                if (profileUser.isPrivate) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Private account",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = profileUser.username,
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.textPrimary,
                )
                if (profileUser.isVerified) {
                    Spacer(modifier = Modifier.width(5.dp))
                    VerifiedBadge()
                }
                Spacer(modifier = Modifier.weight(1f))
                if (isOwnProfile) {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("profile_settings_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = stringResource(id = R.string.settings),
                            tint = palette.textPrimary,
                        )
                    }
                }
            }

            // Header stats & avatar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FreegramAvatar(
                    avatarKey = profileUser.avatarKey,
                    username = profileUser.username,
                    size = 82.dp,
                    hasStory = userStories.isNotEmpty(),
                    isUnviewedStory = userStories.any { !it.isViewed },
                    modifier = Modifier.clickable {
                        if (userStories.isNotEmpty()) onOpenStory()
                    },
                )
                Spacer(modifier = Modifier.width(20.dp))
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    ProfileStatItem(count = userPosts.size, label = "Posts")
                    ProfileStatItem(count = profileUser.followersCount, label = "Followers")
                    ProfileStatItem(count = profileUser.followingCount, label = "Following")
                }
            }

            // Bio & Website
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                Text(
                    text = profileUser.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary,
                )
                if (profileUser.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = profileUser.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textPrimary,
                    )
                }
                if (profileUser.website.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = profileUser.website,
                        style = MaterialTheme.typography.bodyMedium,
                        color = FreegramBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isOwnProfile) {
                    OutlinedButton(
                        onClick = onEditProfile,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_profile_button"),
                    ) {
                        Text(
                            text = stringResource(id = R.string.edit_profile),
                            color = palette.textPrimary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    OutlinedButton(
                        onClick = onShareProfile,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_profile_button"),
                    ) {
                        Text(
                            text = stringResource(id = R.string.share_profile),
                            color = palette.textPrimary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                } else {
                    Button(
                        onClick = onToggleFollow,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (profileUser.isFollowing) palette.surfaceVariant else FreegramBlue,
                        ),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_follow_button"),
                    ) {
                        Text(
                            text = if (profileUser.isFollowing) "Following" else "Follow",
                            color = if (profileUser.isFollowing) palette.textPrimary else Color.White,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    OutlinedButton(
                        onClick = onMessageUser,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = stringResource(id = R.string.message),
                            color = palette.textPrimary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }

            // Tabs: Grid Posts, Reels, Saved
            val tabIcons = if (isOwnProfile) {
                listOf(Icons.Filled.GridOn, Icons.Filled.VideoLibrary, Icons.Filled.Bookmark)
            } else {
                listOf(Icons.Filled.GridOn, Icons.Filled.VideoLibrary)
            }

            TabRow(
                selectedTabIndex = selectedTab.coerceAtMost(tabIcons.lastIndex),
                containerColor = palette.background,
                contentColor = palette.textPrimary,
                indicator = { tabPositions ->
                    val safeIdx = selectedTab.coerceAtMost(tabPositions.lastIndex)
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[safeIdx]),
                        color = palette.textPrimary,
                    )
                },
            ) {
                tabIcons.forEachIndexed { idx, icon ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = when (idx) {
                                    0 -> stringResource(id = R.string.tab_posts)
                                    1 -> stringResource(id = R.string.tab_reels)
                                    else -> stringResource(id = R.string.tab_saved)
                                },
                            )
                        },
                    )
                }
            }

            // Grid content
            when (selectedTab) {
                0 -> PostThumbnailGrid(
                    posts = userPosts,
                    emptyText = "No posts yet",
                    onPostClick = onPostClick,
                )
                1 -> {
                    if (userReels.isEmpty()) {
                        EmptyProfileGridMessage("No Reels yet")
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(userReels, key = { it.id }) { reel ->
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(0.66f)
                                        .clickable { onReelClick(reel) },
                                ) {
                                    FreegramMediaView(
                                        mediaKey = reel.mediaKey,
                                        filterName = reel.filterName,
                                        contentDescription = reel.caption,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(6.dp)
                                            .size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }
                else -> PostThumbnailGrid(
                    posts = savedPosts,
                    emptyText = "Save posts you want to revisit later",
                    onPostClick = onPostClick,
                )
            }
        }
    }
}

@Composable
private fun ProfileStatItem(count: Int, label: String) {
    val palette = LocalFreegramPalette.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = formatCompactCount(count),
            style = MaterialTheme.typography.titleLarge,
            color = palette.textPrimary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary,
        )
    }
}

@Composable
private fun PostThumbnailGrid(
    posts: List<FeedPost>,
    emptyText: String,
    onPostClick: (FeedPost) -> Unit,
) {
    if (posts.isEmpty()) {
        EmptyProfileGridMessage(emptyText)
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(posts, key = { it.id }) { post ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable { onPostClick(post) },
            ) {
                FreegramMediaView(
                    mediaKey = post.mediaKeys.firstOrNull() ?: "res:tokyo",
                    filterName = post.filterName,
                    contentDescription = post.caption,
                    modifier = Modifier.fillMaxSize(),
                )
                if (post.mediaType == PostMediaType.CAROUSEL.name) {
                    Icon(
                        imageVector = Icons.Filled.Collections,
                        contentDescription = "Carousel",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyProfileGridMessage(text: String) {
    val palette = LocalFreegramPalette.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textSecondary,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileBottomSheet(
    user: UserAccount,
    onDismiss: () -> Unit,
    onSave: (fullName: String, username: String, bio: String, website: String, avatarKey: String) -> Unit,
) {
    val palette = LocalFreegramPalette.current
    var fullName by remember { mutableStateOf(user.fullName) }
    var username by remember { mutableStateOf(user.username) }
    var bio by remember { mutableStateOf(user.bio) }
    var website by remember { mutableStateOf(user.website) }
    var avatarKey by remember { mutableStateOf(user.avatarKey) }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) avatarKey = uri.toString()
        },
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        contentColor = palette.textPrimary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(modifier = Modifier.weight(1f))
                Text("Edit profile", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = { onSave(fullName, username, bio, website, avatarKey) },
                    modifier = Modifier.testTag("save_profile_button"),
                ) {
                    Text("Done", color = FreegramBlue, fontWeight = FontWeight.Bold)
                }
            }

            FreegramAvatar(
                avatarKey = avatarKey,
                username = username,
                size = 76.dp,
                modifier = Modifier.clickable {
                    avatarPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            )
            Text(
                text = "Change profile photo",
                color = FreegramBlue,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clickable {
                    avatarPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = website,
                onValueChange = { website = it },
                label = { Text("Website") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    isDarkMode: Boolean,
    isPrivateAccount: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onTogglePrivateAccount: () -> Unit,
    onLogout: () -> Unit,
    onDismiss: () -> Unit,
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
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(id = R.string.settings),
                style = MaterialTheme.typography.titleLarge,
            )
            HorizontalDivider(color = palette.divider)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.DarkMode, contentDescription = null, tint = palette.textPrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(id = R.string.dark_mode),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onToggleDarkMode,
                    modifier = Modifier.testTag("settings_dark_mode_switch"),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = palette.textPrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.private_account),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Only approved followers can view your photos & stories",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary,
                    )
                }
                Switch(
                    checked = isPrivateAccount,
                    onCheckedChange = { onTogglePrivateAccount() },
                    modifier = Modifier.testTag("settings_private_switch"),
                )
            }

            HorizontalDivider(color = palette.divider)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onLogout() }
                    .padding(vertical = 14.dp, horizontal = 4.dp)
                    .testTag("settings_logout_button"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = FreegramHeartRed)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(id = R.string.log_out),
                    color = FreegramHeartRed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
