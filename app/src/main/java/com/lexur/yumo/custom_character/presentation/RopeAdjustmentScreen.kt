package com.lexur.yumo.custom_character.presentation

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import kotlin.math.roundToInt
import androidx.core.graphics.get
import com.lexur.yumo.ui.theme.*

private const val MAX_CANVAS_DIMENSION = 4096
private const val MAX_CANVAS_PIXELS = 12_000_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RopeAdjustmentScreen(
    imageUri: Uri,
    maskPath: Path,
    currentStrokePath: Path,
    ropeResId: Int,
    ropeScale: Float,
    ropeOffsetX: Float,
    ropeOffsetY: Float,
    featheringSize: Float,
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
                RopePreviewCanvas(
                    imageUri = imageUri,
                    maskPath = maskPath,
                    currentStrokePath = currentStrokePath,
                    ropeResId = ropeResId,
                    ropeScale = ropeScale,
                    ropeOffsetX = ropeOffsetX,
                    ropeOffsetY = ropeOffsetY,
                    characterScale = characterScale,
                    featheringSize = featheringSize,
                    isStrokeEnabled = isStrokeEnabled,
                    strokeColor = strokeColor
                )
            }

            // Controls Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
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
                    // Character Size
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Character Size",
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

                    // Image Outline Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Image Outline",
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
private fun RopePreviewCanvas(
    imageUri: Uri,
    maskPath: Path,
    currentStrokePath: Path,
    ropeResId: Int,
    ropeScale: Float,
    ropeOffsetX: Float,
    ropeOffsetY: Float,
    characterScale: Float,
    featheringSize: Float,
    isStrokeEnabled: Boolean,
    strokeColor: Color
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val imageBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = imageUri) {
        value = try {
            loadScaledBitmapFromUri(context, imageUri).asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val ropeBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = ropeResId) {
        value = try {
            context.getDrawable(ropeResId)?.toBitmap()?.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val processedBitmap = remember(imageBitmap, maskPath, currentStrokePath) {
        if (imageBitmap != null) {
            val transparent = createTransparentBitmap(
                imageBitmap!!,
                maskPath,
                currentStrokePath,
                featheringSize
            )
            val androidBitmap = transparent.asAndroidBitmap()
            val cropped = cropTransparentBordersForPreview(androidBitmap)
            cropped.asImageBitmap()
        } else null
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

            if (ropeBitmap != null && processedBitmap != null && canvasSize != IntSize.Zero) {
                var ropeScaledWidth = ropeBitmap!!.width * ropeScale
                var ropeScaledHeight = ropeBitmap!!.height * ropeScale
                var characterWidth = processedBitmap.width.toFloat() * characterScale
                var characterHeight = processedBitmap.height.toFloat() * characterScale

                val maxDimension = maxOf(
                    ropeScaledWidth,
                    ropeScaledHeight,
                    characterWidth,
                    characterHeight
                )

                if (maxDimension > MAX_CANVAS_DIMENSION) {
                    val constraintFactor = MAX_CANVAS_DIMENSION / maxDimension
                    ropeScaledWidth *= constraintFactor
                    ropeScaledHeight *= constraintFactor
                    characterWidth *= constraintFactor
                    characterHeight *= constraintFactor
                }

                val totalContentHeight = ropeScaledHeight + characterHeight
                val totalContentWidth = characterWidth.coerceAtLeast(ropeScaledWidth)

                val totalPixels = totalContentWidth.toLong() * totalContentHeight.toLong()
                if (totalPixels > MAX_CANVAS_PIXELS) {
                    val pixelConstraint = kotlin.math.sqrt(
                        MAX_CANVAS_PIXELS.toDouble() / totalPixels
                    ).toFloat()
                    ropeScaledWidth *= pixelConstraint
                    ropeScaledHeight *= pixelConstraint
                    characterWidth *= pixelConstraint
                    characterHeight *= pixelConstraint
                }

                val finalContentHeight = ropeScaledHeight + characterHeight
                val finalContentWidth = characterWidth.coerceAtLeast(ropeScaledWidth)

                val scaleToFitWidth = size.width / finalContentWidth
                val scaleToFitHeight = size.height / finalContentHeight
                val scaleFactor = minOf(scaleToFitWidth, scaleToFitHeight) * 0.9f

                val displayRopeWidth = ropeScaledWidth * scaleFactor
                val displayRopeHeight = ropeScaledHeight * scaleFactor
                val displayCharacterWidth = characterWidth * scaleFactor
                val displayCharacterHeight = characterHeight * scaleFactor

                if (displayRopeWidth > MAX_CANVAS_DIMENSION ||
                    displayRopeHeight > MAX_CANVAS_DIMENSION ||
                    displayCharacterWidth > MAX_CANVAS_DIMENSION ||
                    displayCharacterHeight > MAX_CANVAS_DIMENSION) {
                    return@Canvas
                }

                val totalDisplayHeight = displayRopeHeight + displayCharacterHeight
                val totalDisplayWidth = displayCharacterWidth.coerceAtLeast(displayRopeWidth)

                val startX = (size.width - totalDisplayWidth) / 2
                val startY = (size.height - totalDisplayHeight) / 2

                val ropeX = startX + (totalDisplayWidth - displayRopeWidth) / 2 +
                        (ropeOffsetX * scaleFactor)
                val ropeY = startY + (ropeOffsetY * scaleFactor)

                drawImage(
                    image = ropeBitmap!!,
                    dstOffset = IntOffset(ropeX.roundToInt(), ropeY.roundToInt()),
                    dstSize = IntSize(
                        displayRopeWidth.roundToInt().coerceAtMost(MAX_CANVAS_DIMENSION),
                        displayRopeHeight.roundToInt().coerceAtMost(MAX_CANVAS_DIMENSION)
                    )
                )

                val characterX = startX + (totalDisplayWidth - displayCharacterWidth) / 2
                val characterY = startY + displayRopeHeight

                if (isStrokeEnabled) {
                    val strokeWidthPx = with(density) { 2.dp.toPx() } * scaleFactor
                    for (dx in -1..1) {
                        for (dy in -1..1) {
                            if (dx == 0 && dy == 0) continue
                            drawImage(
                                image = processedBitmap,
                                dstOffset = IntOffset(
                                    (characterX + dx * strokeWidthPx).roundToInt(),
                                    (characterY + dy * strokeWidthPx).roundToInt()
                                ),
                                dstSize = IntSize(
                                    displayCharacterWidth.roundToInt().coerceAtMost(MAX_CANVAS_DIMENSION),
                                    displayCharacterHeight.roundToInt().coerceAtMost(MAX_CANVAS_DIMENSION)
                                ),
                                colorFilter = ColorFilter.tint(strokeColor, blendMode = BlendMode.SrcIn)
                            )
                        }
                    }
                }

                drawImage(
                    image = processedBitmap,
                    dstOffset = IntOffset(characterX.roundToInt(), characterY.roundToInt()),
                    dstSize = IntSize(
                        displayCharacterWidth.roundToInt().coerceAtMost(MAX_CANVAS_DIMENSION),
                        displayCharacterHeight.roundToInt().coerceAtMost(MAX_CANVAS_DIMENSION)
                    )
                )
            }
        }
    }
}

private fun cropTransparentBordersForPreview(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height

    var top = 0
    var bottom = height
    var left = 0
    var right = width

    topLoop@ for (y in 0 until height) {
        for (x in 0 until width) {
            if (android.graphics.Color.alpha(bitmap[x, y]) > 0) {
                top = y
                break@topLoop
            }
        }
    }

    bottomLoop@ for (y in height - 1 downTo top) {
        for (x in 0 until width) {
            if (android.graphics.Color.alpha(bitmap[x, y]) > 0) {
                bottom = y + 1
                break@bottomLoop
            }
        }
    }

    leftLoop@ for (x in 0 until width) {
        for (y in top until bottom) {
            if (android.graphics.Color.alpha(bitmap[x, y]) > 0) {
                left = x
                break@leftLoop
            }
        }
    }

    rightLoop@ for (x in width - 1 downTo left) {
        for (y in top until bottom) {
            if (android.graphics.Color.alpha(bitmap[x, y]) > 0) {
                right = x + 1
                break@rightLoop
            }
        }
    }

    if (top >= bottom || left >= right) {
        return bitmap
    }

    val croppedWidth = right - left
    val croppedHeight = bottom - top

    return Bitmap.createBitmap(bitmap, left, top, croppedWidth, croppedHeight)
}