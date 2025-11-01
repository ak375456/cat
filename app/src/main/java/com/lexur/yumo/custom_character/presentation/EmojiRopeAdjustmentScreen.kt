package com.lexur.yumo.custom_character.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.lexur.yumo.ui.theme.*
import com.lexur.yumo.util.EmojiUtils
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiRopeAdjustmentScreen(
    emoji: String,
    ropeResId: Int,
    ropeScale: Float,
    ropeOffsetX: Float,
    ropeOffsetY: Float,
    onRopeScaleChanged: (Float) -> Unit,
    onRopeOffsetXChanged: (Float) -> Unit,
    onRopeOffsetYChanged: (Float) -> Unit,
    onConfirm: () -> Unit,
    onNavigateBack: () -> Unit,
    characterScale: Float,
    onCharacterScaleChanged: (Float) -> Unit,
    isStrokeEnabled: Boolean,
    strokeColor: Color,
    onToggleStroke: (Boolean) -> Unit,
    onStrokeColorChanged: (Color) -> Unit,
) {
    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Adjust Position",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = OnTopBar
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = IconPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onConfirm) {
                        Icon(
                            Icons.Default.Done,
                            contentDescription = "Confirm",
                            tint = IconPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TopBarBackground,
                    titleContentColor = OnTopBar,
                    navigationIconContentColor = IconPrimary,
                    actionIconContentColor = IconPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Preview Canvas
            Box(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                EmojiRopePreviewCanvas(
                    emoji = emoji,
                    ropeResId = ropeResId,
                    ropeScale = ropeScale,
                    ropeOffsetX = ropeOffsetX,
                    ropeOffsetY = ropeOffsetY,
                    characterScale = characterScale,
                    isStrokeEnabled = isStrokeEnabled,
                    strokeColor = strokeColor
                )
            }

            // Controls Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
                    .padding(horizontal = 20.dp,
                        vertical = 16.dp),
                shape = RoundedCornerShape(24.dp, 24.dp, 0.dp, 0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                elevation = CardDefaults.cardElevation(0.dp),
                border = BorderStroke(1.dp, OutlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Emoji Size
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Emoji Size",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = OnCard
                            )
                            Text(
                                text = "${(characterScale * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Primary
                            )
                        }
                        Slider(
                            value = characterScale,
                            onValueChange = onCharacterScaleChanged,
                            valueRange = 0.3f..2.5f,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = Primary,
                                activeTrackColor = Primary,
                                inactiveTrackColor = OutlineSecondary
                            )
                        )
                    }

                    HorizontalDivider(color = Divider)

                    // Emoji Outline Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Emoji Outline",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = OnCard
                        )
                        Switch(
                            checked = isStrokeEnabled,
                            onCheckedChange = onToggleStroke,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Primary,
                                checkedTrackColor = Primary.copy(alpha = 0.5f),
                                uncheckedThumbColor = IconSecondary,
                                uncheckedTrackColor = OutlineSecondary
                            )
                        )
                    }

                    // Outline Color Selector
                    if (isStrokeEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Color:",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnContainerVariant
                            )
                            Spacer(Modifier.width(12.dp))
                            Box(
                                Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(
                                        width = 2.5.dp,
                                        color = if (strokeColor == Color.White) Primary else OutlineSecondary,
                                        shape = CircleShape
                                    )
                                    .clickable { onStrokeColorChanged(Color.White) }
                            )
                            Spacer(Modifier.width(12.dp))
                            Box(
                                Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black)
                                    .border(
                                        width = 2.5.dp,
                                        color = if (strokeColor == Color.Black) Primary else OutlineSecondary,
                                        shape = CircleShape
                                    )
                                    .clickable { onStrokeColorChanged(Color.Black) }
                            )
                        }
                    }

                    HorizontalDivider(color = Divider)

                    // Rope Adjustments Header
                    Text(
                        text = "Rope Adjustments",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Primary
                    )

                    // Rope Size
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Size",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = OnCard
                            )
                            Text(
                                text = "${(ropeScale * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Primary
                            )
                        }
                        Slider(
                            value = ropeScale,
                            onValueChange = onRopeScaleChanged,
                            valueRange = 0.3f..2.5f,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = Primary,
                                activeTrackColor = Primary,
                                inactiveTrackColor = OutlineSecondary
                            )
                        )
                    }

                    // Horizontal Position
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Horizontal Position",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = OnCard
                            )
                            Text(
                                text = "${ropeOffsetX.toInt()}px",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Primary
                            )
                        }
                        Slider(
                            value = ropeOffsetX,
                            onValueChange = onRopeOffsetXChanged,
                            valueRange = -1000f..1000f,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = Primary,
                                activeTrackColor = Primary,
                                inactiveTrackColor = OutlineSecondary
                            )
                        )
                    }

                    // Vertical Position
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Vertical Position",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = OnCard
                            )
                            Text(
                                text = "${ropeOffsetY.toInt()}px",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Primary
                            )
                        }
                        Slider(
                            value = ropeOffsetY,
                            onValueChange = onRopeOffsetYChanged,
                            valueRange = -1000f..1000f,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = Primary,
                                activeTrackColor = Primary,
                                inactiveTrackColor = OutlineSecondary
                            )
                        )
                    }

                    // Reset Button
                    OutlinedButton(
                        onClick = {
                            onRopeScaleChanged(1f)
                            onRopeOffsetXChanged(0f)
                            onRopeOffsetYChanged(0f)
                            onCharacterScaleChanged(1f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = ButtonSecondary,
                            contentColor = OnButtonSecondary
                        ),
                        border = BorderStroke(1.5.dp, OutlinePrimary)
                    ) {
                        Text(
                            "Reset to Default",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmojiRopePreviewCanvas(
    emoji: String,
    ropeResId: Int,
    ropeScale: Float,
    ropeOffsetX: Float,
    ropeOffsetY: Float,
    characterScale: Float,
    isStrokeEnabled: Boolean,
    strokeColor: Color
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val emojiBitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(
        initialValue = null,
        key1 = emoji,
        key2 = characterScale
    ) {
        value = try {
            EmojiUtils.emojiToBitmap(context, emoji).asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val ropeBitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(
        initialValue = null,
        key1 = ropeResId
    ) {
        value = try {
            context.getDrawable(ropeResId)?.toBitmap()?.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Transparent)
            .border(1.dp, OutlineVariant, RoundedCornerShape(20.dp))
            .onGloballyPositioned { coordinates ->
                if (canvasSize != coordinates.size) {
                    canvasSize = coordinates.size
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCheckerboard()

            if (ropeBitmap != null && emojiBitmap != null && canvasSize != IntSize.Zero) {
                var ropeScaledWidth = ropeBitmap!!.width * ropeScale
                var ropeScaledHeight = ropeBitmap!!.height * ropeScale
                var emojiWidth = emojiBitmap!!.width.toFloat() * characterScale
                var emojiHeight = emojiBitmap!!.height.toFloat() * characterScale

                val maxDimension = maxOf(
                    ropeScaledWidth,
                    ropeScaledHeight,
                    emojiWidth,
                    emojiHeight
                )

                if (maxDimension > 4096) {
                    val constraintFactor = 4096 / maxDimension
                    ropeScaledWidth *= constraintFactor
                    ropeScaledHeight *= constraintFactor
                    emojiWidth *= constraintFactor
                    emojiHeight *= constraintFactor
                }

                val totalContentHeight = ropeScaledHeight + emojiHeight
                val totalContentWidth = emojiWidth.coerceAtLeast(ropeScaledWidth)

                val totalPixels = totalContentWidth.toLong() * totalContentHeight.toLong()
                if (totalPixels > 12_000_000L) {
                    val pixelConstraint = kotlin.math.sqrt(
                        12_000_000.0 / totalPixels
                    ).toFloat()
                    ropeScaledWidth *= pixelConstraint
                    ropeScaledHeight *= pixelConstraint
                    emojiWidth *= pixelConstraint
                    emojiHeight *= pixelConstraint
                }

                val finalContentHeight = ropeScaledHeight + emojiHeight
                val finalContentWidth = emojiWidth.coerceAtLeast(ropeScaledWidth)

                val scaleToFitWidth = size.width / finalContentWidth
                val scaleToFitHeight = size.height / finalContentHeight
                val scaleFactor = minOf(scaleToFitWidth, scaleToFitHeight) * 0.9f

                val displayRopeWidth = ropeScaledWidth * scaleFactor
                val displayRopeHeight = ropeScaledHeight * scaleFactor
                val displayEmojiWidth = emojiWidth * scaleFactor
                val displayEmojiHeight = emojiHeight * scaleFactor

                if (displayRopeWidth > 4096 ||
                    displayRopeHeight > 4096 ||
                    displayEmojiWidth > 4096 ||
                    displayEmojiHeight > 4096
                ) {
                    return@Canvas
                }

                val totalDisplayHeight = displayRopeHeight + displayEmojiHeight
                val totalDisplayWidth = displayEmojiWidth.coerceAtLeast(displayRopeWidth)

                val startX = (size.width - totalDisplayWidth) / 2
                val startY = (size.height - totalDisplayHeight) / 2

                val ropeX = startX + (totalDisplayWidth - displayRopeWidth) / 2 +
                        (ropeOffsetX * scaleFactor)
                val ropeY = startY + (ropeOffsetY * scaleFactor)

                drawImage(
                    image = ropeBitmap!!,
                    dstOffset = IntOffset(ropeX.roundToInt(), ropeY.roundToInt()),
                    dstSize = IntSize(
                        displayRopeWidth.roundToInt().coerceAtMost(4096),
                        displayRopeHeight.roundToInt().coerceAtMost(4096)
                    )
                )

                val emojiX = startX + (totalDisplayWidth - displayEmojiWidth) / 2
                val emojiY = startY + displayRopeHeight

                // Draw emoji with optional stroke
                if (isStrokeEnabled) {
                    val strokeWidthPx = with(density) { 3.dp.toPx() } * scaleFactor

                    // Draw stroke using native canvas for better text rendering
                    drawIntoCanvas { canvas ->
                        val paint = android.graphics.Paint().apply {
                            textSize = displayEmojiHeight * 0.8f
                            typeface = android.graphics.Typeface.DEFAULT
                            isAntiAlias = true
                            style = android.graphics.Paint.Style.STROKE
                            strokeWidth = strokeWidthPx
                            color = strokeColor.hashCode()
                        }

                        // Calculate text position
                        val textBounds = android.graphics.Rect()
                        paint.getTextBounds(emoji, 0, emoji.length, textBounds)

                        val textX = emojiX + displayEmojiWidth / 2f
                        val textY = emojiY + displayEmojiHeight / 2f - textBounds.exactCenterY()

                        canvas.nativeCanvas.drawText(
                            emoji,
                            textX,
                            textY,
                            paint
                        )
                    }
                }

                // Draw the emoji itself
                drawImage(
                    image = emojiBitmap!!,
                    dstOffset = IntOffset(emojiX.roundToInt(), emojiY.roundToInt()),
                    dstSize = IntSize(
                        displayEmojiWidth.roundToInt().coerceAtMost(4096),
                        displayEmojiHeight.roundToInt().coerceAtMost(4096)
                    )
                )
            }
        }
    }
}