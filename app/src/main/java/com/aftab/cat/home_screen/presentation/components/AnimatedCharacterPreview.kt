package com.aftab.cat.home_screen.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.aftab.cat.home_screen.data.model.Characters
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
    val infiniteTransition = rememberInfiniteTransition()
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Animation state for frame-by-frame animation
    var currentFrame by remember { mutableIntStateOf(0) }
    val frameCount = character?.frameIds?.size ?: 1

    LaunchedEffect(character) {
        while (true) {
            delay(character?.animationDelay ?: 100L)
            currentFrame = (currentFrame + 1) % frameCount
        }
    }

    Card(
        onClick = onCardClick,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Character preview with bounce and frame animation
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
            ) {
                if (character != null) {
                    // Display the current animation frame
                    Image(
                        painter = painterResource(id = character.frameIds[currentFrame]),
                        contentDescription = character.name,
                        modifier = Modifier
                            .size(character.previewWidth.dp, character.previewHeight.dp)
                            .offset(y = bounce.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    CircularProgressIndicator()
                }
            }

            // Character name and category
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = character?.name ?: "No character selected",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = character?.category?.displayName ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expanded content with buttons
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Character stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("Frames", "${character?.frameIds?.size ?: 0}")
                        StatItem("Speed", "${character?.speed ?: 0}px/frame")
                        StatItem("Size", "${character?.width ?: 0}x${character?.height ?: 0}")
                    }

                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isCharacterRunning) {
                            // Show Stop button if character is running
                            Button(
                                onClick = onStopCharacter,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Stop This")
                            }
                        } else {
                            // Show Use button if character is not running
                            Button(
                                onClick = onUseCharacter,
                                enabled = canUseCharacter && character != null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Use This")
                            }
                        }

                                    OutlinedButton(
                                    onClick = onCharacterSettings,
                            enabled = character != null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Settings")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}