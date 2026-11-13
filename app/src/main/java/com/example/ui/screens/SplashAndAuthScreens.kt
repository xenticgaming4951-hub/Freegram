package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.BrandWordmark
import com.example.ui.components.FreegramAvatar
import com.example.ui.theme.FreegramBlue
import com.example.ui.theme.FreegramBrandGradient
import com.example.ui.theme.FreegramHeartRed
import com.example.ui.theme.LocalFreegramPalette
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val palette = LocalFreegramPalette.current
    var started by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.72f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "splashLogoScale",
    )

    LaunchedEffect(Unit) {
        started = true
        delay(2000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_freegram_logo),
            contentDescription = "Freegram logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.Center)
                .size(96.dp)
                .graphicsLayer {
                    scaleX = logoScale
                    scaleY = logoScale
                }
                .clip(RoundedCornerShape(24.dp)),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(id = R.string.from_brand_label),
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
                letterSpacing = 0.8.sp,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(id = R.string.author_brand_name),
                style = TextStyle(
                    brush = FreegramBrandGradient,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                ),
            )
        }
    }
}

@Composable
fun AuthScreen(
    errorMessage: String?,
    infoMessage: String?,
    onClearMessages: () -> Unit,
    onLogin: (identifier: String, password: String) -> Unit,
    onLoginAsDemo: () -> Unit,
    onSignUp: (
        username: String,
        fullName: String,
        emailOrPhone: String,
        password: String,
        bio: String,
        avatarKey: String,
    ) -> Unit,
    onResetPassword: (identifier: String, newPassword: String) -> Unit,
) {
    val palette = LocalFreegramPalette.current
    var isSignUpMode by remember { mutableStateOf(false) }
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Signup fields
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var pickedAvatarUri by remember { mutableStateOf("") }

    var showResetDialog by remember { mutableStateOf(false) }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                pickedAvatarUri = uri.toString()
            }
        },
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_freegram_logo),
                contentDescription = "Freegram logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp)),
            )
            Spacer(modifier = Modifier.height(10.dp))
            BrandWordmark(fontSizeSp = 44f)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isSignUpMode) {
                    "Sign up to share photos, Reels, and stories with friends."
                } else {
                    "Sign in to your Freegram account"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(22.dp))

            AnimatedVisibility(visible = isSignUpMode) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .border(2.dp, palette.divider, CircleShape)
                            .clickable {
                                avatarPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("signup_avatar_picker"),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (pickedAvatarUri.isNotBlank()) {
                            FreegramAvatar(
                                avatarKey = pickedAvatarUri,
                                username = username.ifBlank { "New" },
                                size = 86.dp,
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.AddAPhoto,
                                    contentDescription = "Pick profile photo",
                                    tint = FreegramBlue,
                                    modifier = Modifier.size(28.dp),
                                )
                                Text(
                                    text = "Photo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FreegramBlue,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            OutlinedTextField(
                value = identifier,
                onValueChange = {
                    identifier = it
                    onClearMessages()
                },
                label = {
                    Text(
                        if (isSignUpMode) "Phone number or email" else "Phone number, username, or email"
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = palette.textSecondary,
                    unfocusedBorderColor = palette.divider,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_identifier_input"),
            )

            if (isSignUpMode) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.textSecondary,
                        unfocusedBorderColor = palette.divider,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_fullname_input"),
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.textSecondary,
                        unfocusedBorderColor = palette.divider,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_username_input"),
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio (optional)") },
                    maxLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.textSecondary,
                        unfocusedBorderColor = palette.divider,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    onClearMessages()
                },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Toggle password visibility",
                        )
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = palette.textSecondary,
                    unfocusedBorderColor = palette.divider,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_password_input"),
            )

            if (!isSignUpMode) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.align(Alignment.CenterEnd),
                    ) {
                        Text(
                            text = "Forgot password?",
                            color = FreegramBlue,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = FreegramHeartRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            if (infoMessage != null) {
                Text(
                    text = infoMessage,
                    color = FreegramBlue,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            Button(
                onClick = {
                    if (isSignUpMode) {
                        onSignUp(username, fullName, identifier, password, bio, pickedAvatarUri)
                    } else {
                        onLogin(identifier, password)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FreegramBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("auth_submit_button"),
            ) {
                Text(
                    text = if (isSignUpMode) "Sign up" else "Log in",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = palette.divider)
                Text(
                    text = "  OR  ",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textSecondary,
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = palette.divider)
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onLoginAsDemo,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("demo_login_button"),
            ) {
                Text(
                    text = "Continue as @alex.visuals (Demo)",
                    color = FreegramBlue,
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            Spacer(modifier = Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isSignUpMode) "Already have an account? " else "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
                )
                Text(
                    text = if (isSignUpMode) "Log in." else "Sign up.",
                    style = MaterialTheme.typography.labelLarge,
                    color = FreegramBlue,
                    modifier = Modifier
                        .clickable {
                            isSignUpMode = !isSignUpMode
                            onClearMessages()
                        }
                        .padding(4.dp)
                        .testTag("toggle_auth_mode_button"),
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "from asif 4537",
                style = MaterialTheme.typography.labelMedium,
                color = palette.textSecondary,
            )
        }
    }

    if (showResetDialog) {
        var resetId by remember { mutableStateOf(identifier) }
        var newPass by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter your username, email, or phone and choose a new password.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    OutlinedTextField(
                        value = resetId,
                        onValueChange = { resetId = it },
                        label = { Text("Username / Email / Phone") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetPassword(resetId, newPass)
                        showResetDialog = false
                    },
                ) {
                    Text("Update", color = FreegramBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}
