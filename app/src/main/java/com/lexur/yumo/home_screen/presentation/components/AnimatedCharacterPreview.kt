package com.lexur.yumo.home_screen.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.lexur.yumo.home_screen.data.model.CharacterCategory
import com.lexur.yumo.home_screen.data.model.Characters
import com.lexur.yumo.ui.theme.buttonPrimary
import com.lexur.yumo.ui.theme.cardBackground
import kotlinx.coroutines.delay

@Composable
fun AnimatedCharacterPreview(
    character: Characters?,
    isExpanded: Boolean,
    onCardClick: () -> Unit,
    onUseCharacter: () -> Unit,
    onCharacterSettings: () -> Unit,
    canUseCharacter: Boolean,
    isCharacterRunning: Boolean,
    onStopCharacter: () -> Unit,
    isPremiumUser: Boolean = false,
    onPremiumClick: () -> Unit = {}
) {
    var currentFrame by remember { mutableIntStateOf(0) }
    val frameCount = character?.frameIds?.size ?: 1
    var showHangingInfoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(character, isExpanded) {
        if (isExpanded && character != null && frameCount > 1 && character.animationDelay > 0L) {
            while (true) {
                delay(character.animationDelay)
                currentFrame = (currentFrame + 1) % frameCount
            }
        } else {
            currentFrame = 0
        }
    }

    HangingCharacterInfoDialog(
        showDialog = showHangingInfoDialog,
        onDismiss = { showHangingInfoDialog = false },
        characterName = character?.name ?: "This character"
    )

    Card(
        onClick = onCardClick,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.cardBackground
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Character preview container
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(100.dp)
                ) {
                    if (character!!.isCustom) {
                        Image(
                            painter = rememberAsyncImagePainter(model = character.imagePath),
                            contentDescription = character.name,
                            modifier = Modifier.size(
                                (character.previewWidth * 0.7).dp,
                                (character.previewHeight * 0.7).dp
                            ),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Image(
                            painter = painterResource(id = character.frameIds[currentFrame]),
                            contentDescription = character.name,
                            modifier = Modifier.size(
                                (character.previewWidth * 0.7).dp,
                                (character.previewHeight * 0.7).dp
                            ),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Character name and category
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = character?.name ?: "Loading...",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (!character?.category?.displayName.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = character.category.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Running status indicator
                if (isCharacterRunning && !isExpanded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = "Running",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Expanded content with action buttons
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(tween(400)) + expandVertically(tween(400)),
                    exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        if (isCharacterRunning) {
                            Button(
                                onClick = onStopCharacter,
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    painter = painterResource(android.R.drawable.ic_media_pause),
                                    contentDescription = "Stop Character",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    // Check premium status when trying to use
                                    if (character?.isPremium == true && !isPremiumUser) {
                                        onPremiumClick()
                                    } else {
                                        onUseCharacter()
                                    }
                                },
                                enabled = canUseCharacter && character != null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.buttonPrimary,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Icon(
                                    painter = painterResource(android.R.drawable.ic_media_play),
                                    contentDescription = "Use Character",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                // Check premium status when trying to access settings
                                if (character?.isPremium == true && !isPremiumUser) {
                                    onPremiumClick()
                                } else {
                                    onCharacterSettings()
                                }
                            },
                            enabled = character != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Settings",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Premium badge - small icon in top-left corner
            if (character?.isPremium == true && !isPremiumUser) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Premium",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Info button for hanging characters - top-right corner
            if (character?.category == CharacterCategory.HANGING || character?.isHanging == true) {
                IconButton(
                    onClick = { showHangingInfoDialog = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Hanging Character Info",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}