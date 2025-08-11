package com.lexur.yumo.home_screen.presentation.components

import android.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.lexur.yumo.home_screen.data.model.Characters
import com.lexur.yumo.ui.theme.ButtonPrimary
import com.lexur.yumo.ui.theme.CardBackground
import com.lexur.yumo.ui.theme.Container
import com.lexur.yumo.ui.theme.Error
import com.lexur.yumo.ui.theme.IconPrimary
import com.lexur.yumo.ui.theme.OnButtonPrimary
import com.lexur.yumo.ui.theme.OnCard
import com.lexur.yumo.ui.theme.OnError
import com.lexur.yumo.ui.theme.OnSecondary
import com.lexur.yumo.ui.theme.OutlinePrimary
import com.lexur.yumo.ui.theme.Primary
import com.lexur.yumo.ui.theme.SecondaryVariant
import com.lexur.yumo.ui.theme.SurfaceVariant
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedCharacterPreviewCard(
    character: Characters?,
    isExpanded: Boolean,
    onCardClick: () -> Unit,
    onUseCharacter: () -> Unit,
    onCharacterSettings: () -> Unit,
    canUseCharacter: Boolean,
    isCharacterRunning: Boolean,
    onStopCharacter: () -> Unit,
) {
    var showHangingInfoDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition()
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val glow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Animation state for frame-by-frame animation - only animate when expanded
    var currentFrame by remember { mutableIntStateOf(0) }
    val frameCount = character?.frameIds?.size ?: 1

    LaunchedEffect(character, isExpanded) {
        if (isExpanded && character != null) {
            val animationDelay = character.animationDelay

            // Only animate if there are multiple frames and delay is greater than 0
            if (frameCount > 1 && animationDelay > 0L) {
                while (true) {
                    delay(animationDelay)
                    currentFrame = (currentFrame + 1) % frameCount
                }
            } else {
                // For static characters (single frame or 0 delay), keep frame at 0
                currentFrame = 0
            }
        } else {
            currentFrame = 0 // Reset to first frame when collapsed
        }
    }

    // Hanging Character Info Dialog
    if (showHangingInfoDialog) {
        AlertDialog(
            onDismissRequest = { showHangingInfoDialog = false },
            icon = {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Hanging Characters",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = OnCard
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Hanging characters are special static pets that attach to your status bar elements!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnCard
                    )

                    Text(
                        text = "They can hang from:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = OnCard
                    )

                    Column(
                        modifier = Modifier.padding(start = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("• Camera notch", style = MaterialTheme.typography.bodySmall, color = OnCard)
                        Text("• WiFi signal icon", style = MaterialTheme.typography.bodySmall, color = OnCard)
                        Text("• Mobile signal bars", style = MaterialTheme.typography.bodySmall, color = OnCard)
                        Text("• Battery indicator", style = MaterialTheme.typography.bodySmall, color = OnCard)
                        Text("• Clock", style = MaterialTheme.typography.bodySmall, color = OnCard)
                        Text("• Any other status bar element", style = MaterialTheme.typography.bodySmall, color = OnCard)
                    }

                    Text(
                        text = "Unlike walking characters, hanging pets stay in one position and add a cute touch to your status bar!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnCard.copy(alpha = 0.8f)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showHangingInfoDialog = false }
                ) {
                    Text(
                        "Got it!",
                        color = Primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            containerColor = CardBackground
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
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
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isExpanded) 12.dp else 8.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = CardBackground, // Using custom card color
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box {
                // Hanging Character Info Icon - Top Right Corner
                if (character?.isHanging == true) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(1f), // Ensure it's above other content
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Surface(
                            modifier = Modifier
                                .padding(12.dp)
                                .size(32.dp)
                                .clickable { showHangingInfoDialog = true },
                            shape = CircleShape,
                            color = Primary.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "Hanging character info",
                                    tint = Primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

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
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = if (isCharacterRunning) {
                                        listOf(
                                            Primary.copy(alpha = glow * 0.15f), // Using custom primary color
                                            Container.copy(alpha = 0.08f), // Using custom container color
                                            CardBackground
                                        )
                                    } else {
                                        listOf(
                                            SurfaceVariant.copy(alpha = 0.3f), // Using custom surface variant
                                            CardBackground
                                        )
                                    }
                                )
                            )
                    ) {
                        if (character != null) {
                            Image(
                                painter = painterResource(id = character.frameIds[currentFrame]),
                                contentDescription = character.name,
                                modifier = Modifier
                                    .size(
                                        (character.previewWidth * 0.7).dp,
                                        (character.previewHeight * 0.7).dp
                                    )
                                    .offset(y = if (isExpanded) bounce.dp else 0.dp)
                                    .graphicsLayer {
                                        if (isCharacterRunning) {
                                            scaleX = 1f + (glow * 0.1f)
                                            scaleY = 1f + (glow * 0.1f)
                                        }
                                    },
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.5.dp,
                                color = Primary // Using custom primary color
                            )
                        }
                    }

                    // Character info
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
                            color = OnCard // Using custom text color on cards
                        )

                        if (!character?.category?.displayName.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SecondaryVariant.copy(alpha = 0.6f) // Using custom secondary variant
                            ) {
                                Text(
                                    text = character.category.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OnSecondary, // Using custom text color
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
                                    .background(Primary) // Using custom primary color
                                    .alpha(glow)
                            )
                            Text(
                                text = "Running",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Primary // Using custom primary color
                            )
                        }
                    }

                    // Expanded content
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn(tween(400)) + expandVertically(tween(400)) + slideInVertically { it / 4 },
                        exit = fadeOut(tween(300)) + shrinkVertically(tween(300)) + slideOutVertically { -it / 4 }
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Action buttons
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isCharacterRunning) {
                                    Button(
                                        onClick = onStopCharacter,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Error, // Using custom error color
                                            contentColor = OnError // Using custom text on error
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_media_pause),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = onUseCharacter,
                                        enabled = canUseCharacter && character != null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ButtonPrimary, // Using custom button color
                                            contentColor = OnButtonPrimary // Using custom button text color
                                        )
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_media_play),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = onCharacterSettings,
                                    enabled = character != null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(
                                        1.5.dp,
                                        OutlinePrimary.copy(alpha = 0.7f) // Using custom outline color
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = IconPrimary // Using custom icon color
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}