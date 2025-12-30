package com.lexur.yumo.character_screen.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Vibrate
import com.lexur.yumo.ui.theme.buttonPrimary
import com.lexur.yumo.ui.theme.containerColor
import com.lexur.yumo.ui.theme.iconPrimary
import com.lexur.yumo.ui.theme.iconSecondary
import com.lexur.yumo.ui.theme.inputBackground
import com.lexur.yumo.ui.theme.mainColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CharacterSettingsScreen(
    characterId: String,
    onNavigateBack: () -> Unit,
    isCharacterRunning: Boolean = false,
    viewModel: CharacterSettingsViewModel = hiltViewModel()
) {
    val character by viewModel.character.collectAsState()
    val speed by viewModel.speed.collectAsState()
    val size by viewModel.size.collectAsState()
    val animationDelay by viewModel.animationDelay.collectAsState()
    val yPosition by viewModel.yPosition.collectAsState()
    val xPosition by viewModel.xPosition.collectAsState()
    val characterRunning by viewModel.isCharacterRunning.collectAsState()
    val motionSensingEnabled by viewModel.motionSensingEnabled.collectAsState()
    val useButtonControls by viewModel.useButtonControls.collectAsState()
    val atBottom by viewModel.atBottom.collectAsState()
    val enableFullScreenY by viewModel.enableFullScreenY.collectAsState()
    val enableInLandscape by viewModel.enableInLandscape.collectAsState()
    val maxXPosition by viewModel.maxXPosition.collectAsState()

    val animationSpeedPresets = listOf(50L, 80L, 100L, 140L, 200L, 300L, 500L)
    var showPresetInfo by remember { mutableStateOf(false) }
    var showMotionInfo by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val isHangingCharacter = character?.isHanging == true

    LaunchedEffect(characterId) {
        viewModel.loadCharacter(characterId)
    }

    LaunchedEffect(Unit) {
        if (isCharacterRunning) {
            viewModel.setCharacterRunning(true)
        }
    }

    LaunchedEffect(characterRunning) {
        if (characterRunning && !enableInLandscape) {
            // Show a message if character is disabled in landscape
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${character?.name} Settings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (characterRunning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isHangingCharacter)
                                "Hanging character is active - changes will be applied instantly!"
                            else
                                "Character is running - changes will be applied instantly!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            if (isHangingCharacter) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Static Hanging Character",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (motionSensingEnabled)
                                    "This character swings and tilts with device movement"
                                else
                                    "This character stays perfectly still",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (enableInLandscape)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Show in Landscape Mode",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (enableInLandscape)
                                "Character will be visible when device is rotated"
                            else
                                "Character will hide when device is rotated to landscape",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = enableInLandscape,
                        onCheckedChange = { viewModel.setEnableInLandscape(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.surfaceContainer,
                            uncheckedThumbColor = MaterialTheme.colorScheme.iconSecondary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            if (isHangingCharacter) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (motionSensingEnabled)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Lucide.Vibrate,
                                    contentDescription = null,
                                    tint = if (motionSensingEnabled)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Motion Sensing",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                                IconButton(
                                    onClick = { showMotionInfo = !showMotionInfo },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Motion Info",
                                        tint = MaterialTheme.colorScheme.iconSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Switch(
                                checked = motionSensingEnabled,
                                onCheckedChange = { viewModel.setMotionSensingEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.surfaceContainer,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.iconSecondary,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }

                        if (showMotionInfo) {
                            Text(
                                text = if (motionSensingEnabled) {
                                    "Character hangs from a fixed rope/chain\n" +
                                            "Responds to device tilt and movement\n" +
                                            "May use slightly more battery"
                                } else {
                                    "Character remains completely static\n" +
                                            "Better battery life\n" +
                                            "No movement or tilting"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        if (motionSensingEnabled && characterRunning) {
                            Text(
                                text = "Try tilting your device left/right to see the character swing!",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            if (!characterRunning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        // Approximation for SecondaryVariant in Dark Mode: using secondaryContainer
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Test Your Settings",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = if (isHangingCharacter) {
                                val motionText = if (motionSensingEnabled) " and swing like a pendulum" else ""
                                "Activate the character to see it positioned on screen$motionText"
                            } else {
                                "Start the character to see changes in real-time"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(
                            onClick = {
                                viewModel.saveSettings()
                                viewModel.startCharacterTest()
                            },
                            enabled = character != null,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.buttonPrimary,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                if (isHangingCharacter) "Activate Character" else "Start Test",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick = {
                        viewModel.stopCharacterTest()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        if (isHangingCharacter) "Deactivate Character" else "Stop Test",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Text(
                text = "Character Size",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
            )

            Text(
                text = "Size: ${size}px",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Slider(
                value = size.toFloat(),
                onValueChange = { newSize ->
                    viewModel.updateSize(newSize.toInt())
                },
                valueRange = 10f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Text(
                text = "Position Settings ${if (characterRunning) "(Live Updates)" else ""}",
                style = MaterialTheme.typography.titleSmall,
                color = if (characterRunning)
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (atBottom)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Position at Bottom",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (isHangingCharacter)
                                "Hangs from the bottom (rotated 180°)"
                            else
                                "Walks along the bottom of the screen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = atBottom,
                        onCheckedChange = { viewModel.setAtBottom(it, isHangingCharacter) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.containerColor,
                            uncheckedThumbColor = MaterialTheme.colorScheme.iconSecondary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (enableFullScreenY)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Full Screen Y-Position",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Allows placing the character anywhere vertically on screen.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = enableFullScreenY,
                        onCheckedChange = { viewModel.setEnableFullScreenY(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.containerColor,
                            uncheckedThumbColor = MaterialTheme.colorScheme.iconSecondary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Use Button Controls",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Switch(
                    checked = useButtonControls,
                    onCheckedChange = { viewModel.setUseButtonControls(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.containerColor,
                        uncheckedThumbColor = MaterialTheme.colorScheme.iconSecondary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            if (useButtonControls) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Position: X=${xPosition}px, Y=${yPosition}px",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    IconButton(
                        onClick = { viewModel.movePosition(0, -1) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Move Up",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.movePosition(-1, 0) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Move Left",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = "1px",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )

                        IconButton(
                            onClick = { viewModel.movePosition(1, 0) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Move Right",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.movePosition(0, 1) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move Down",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            } else {
                if (isHangingCharacter) {
                    Text(
                        text = "Horizontal Position: $xPosition px from left (Max: ${maxXPosition}px)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Slider(
                        value = xPosition.toFloat(),
                        onValueChange = { viewModel.updateXPosition(it.toInt()) },
                        valueRange = 0f..maxXPosition.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }

                Text(
                    text = "Vertical Position: $yPosition px from ${if (atBottom) "bottom" else "top"}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Slider(
                    value = yPosition.toFloat(),
                    onValueChange = { viewModel.updateYPosition(it.toInt()) },
                    valueRange = if (enableFullScreenY) 0f..3000f else 0f..300f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }

            Text(
                text = if (isHangingCharacter)
                    "Movement Speed: Static (no movement)"
                else
                    "Movement Speed: $speed px/frame",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isHangingCharacter)
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.onBackground
            )
            Slider(
                value = speed.toFloat(),
                onValueChange = { viewModel.updateSpeed(it.toInt()) },
                valueRange = 1f..10f,
                enabled = !isHangingCharacter,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isHangingCharacter)
                        "Animation: ${animationDelay}ms (static)"
                    else
                        "Animation Speed: ${animationDelay}ms",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isHangingCharacter)
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onBackground
                )
                IconButton(
                    onClick = { showPresetInfo = !showPresetInfo },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.iconSecondary,
                    )
                }
            }

            if (showPresetInfo) {
                Text(
                    text = if (isHangingCharacter)
                        "Hanging characters display static images, animation delay doesn't affect them"
                    else
                        "Lower values = faster animation (more battery usage)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                animationSpeedPresets.forEach { preset ->
                    FilterChip(
                        selected = animationDelay == preset,
                        onClick = { viewModel.updateAnimationDelay(preset) },
                        enabled = !isHangingCharacter,
                        label = { Text("${preset}ms") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.containerColor,
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                            containerColor = MaterialTheme.colorScheme.inputBackground,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            selectedBorderColor = MaterialTheme.colorScheme.primary,
                            borderColor = MaterialTheme.colorScheme.outline,
                            selected = animationDelay == preset,
                            enabled = !isHangingCharacter
                        )
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.saveSettings()
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.buttonPrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("Save Settings", style = MaterialTheme.typography.labelLarge)
            }

            OutlinedButton(
                onClick = {
                    viewModel.resetToDefaults()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.iconPrimary
                )
            ) {
                Text("Reset to Defaults")
            }
        }
    }
}