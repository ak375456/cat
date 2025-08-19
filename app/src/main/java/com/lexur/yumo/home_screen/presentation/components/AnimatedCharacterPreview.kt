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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lexur.yumo.home_screen.data.model.CharacterCategory
import com.lexur.yumo.home_screen.data.model.Characters
import com.lexur.yumo.ui.theme.ButtonPrimary
import com.lexur.yumo.ui.theme.CardBackground
import com.lexur.yumo.ui.theme.Error
import com.lexur.yumo.ui.theme.OnButtonPrimary
import com.lexur.yumo.ui.theme.OnCard
import com.lexur.yumo.ui.theme.OnError
import com.lexur.yumo.ui.theme.OnSecondary
import com.lexur.yumo.ui.theme.Primary
import com.lexur.yumo.ui.theme.SecondaryVariant
import com.lexur.yumo.ui.theme.SurfaceVariant
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
) {
    // State for frame-by-frame animation
    var currentFrame by remember { mutableIntStateOf(0) }
    val frameCount = character?.frameIds?.size ?: 1

    // State for hanging character info dialog
    var showHangingInfoDialog by remember { mutableStateOf(false) }

    // This effect runs the character's frame animation when the card is expanded.
    LaunchedEffect(character, isExpanded) {
        if (isExpanded && character != null && frameCount > 1 && character.animationDelay > 0L) {
            while (true) {
                delay(character.animationDelay)
                currentFrame = (currentFrame + 1) % frameCount
            }
        } else {
            // Reset to the first frame when collapsed or static.
            currentFrame = 0
        }
    }

    // Show dialog for hanging characters
    HangingCharacterInfoDialog(
        showDialog = showHangingInfoDialog,
        onDismiss = { showHangingInfoDialog = false },
        characterName = character?.name ?: "This character"
    )

    Card(
        onClick = onCardClick,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize( // Animates the card size change smoothly
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
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
                    modifier = Modifier
                        .size(100.dp)
                ) {
                    if (character != null) {
                        Image(
                            painter = painterResource(id = character.frameIds[currentFrame]),
                            contentDescription = character.name,
                            modifier = Modifier.size(
                                (character.previewWidth * 0.7).dp,
                                (character.previewHeight * 0.7).dp
                            ),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        // Show a loading indicator if the character data is not yet available
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.5.dp,
                            color = Primary
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
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        color = OnCard
                    )

                    if (!character?.category?.displayName.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SecondaryVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = character.category.displayName, // Safe due to isNullOrBlank check
                                style = MaterialTheme.typography.labelMedium,
                                color = OnSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // "Running" status indicator shown only when the card is collapsed
                if (isCharacterRunning && !isExpanded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Primary)
                        )
                        Text(
                            text = "Running",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = Primary
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
                        // Show "Stop" button if running, otherwise show "Use" button
                        if (isCharacterRunning) {
                            Button(
                                onClick = onStopCharacter,
                                modifier = Modifier.fillMaxWidth(0.5f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Error,
                                    contentColor = OnError
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
                                onClick = onUseCharacter,
                                enabled = canUseCharacter && character != null,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ButtonPrimary,
                                    contentColor = OnButtonPrimary
                                )
                            ) {
                                Icon(
                                    painter = painterResource(android.R.drawable.ic_media_play),
                                    contentDescription = "Use Character",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Settings Button (replaces the OutlinedButton)
                        Button(
                            onClick = onCharacterSettings,
                            enabled = character != null,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                // Using a secondary color to distinguish from the primary action
                                containerColor = SurfaceVariant,
                                contentColor = OnCard
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

            // Info button for hanging characters - positioned at top-right
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
                        tint = Primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}