package com.aftab.cat.home_screen.presentation.components



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
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
        if (isExpanded) {
            while (true) {
                delay(character?.animationDelay ?: 120L)
                currentFrame = (currentFrame + 1) % frameCount
            }
        } else {
            currentFrame = 0 // Reset to first frame when collapsed
        }
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
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box {
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
                                            MaterialTheme.colorScheme.primary.copy(alpha = glow * 0.15f),
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f),
                                            MaterialTheme.colorScheme.surfaceContainer
                                        )
                                    } else {
                                        listOf(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                            MaterialTheme.colorScheme.surfaceContainer
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
                                color = MaterialTheme.colorScheme.primary
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

                    // Top-right quick action button


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
                                    .alpha(glow)
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
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(android.R.drawable.ic_media_pause),
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
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        Icon(
                                            painter = painterResource(android.R.drawable.ic_media_play),
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
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
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