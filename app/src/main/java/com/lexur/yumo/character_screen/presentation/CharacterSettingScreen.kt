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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.material.icons.filled.Close
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Vibrate
import com.lexur.yumo.ui.theme.Background
import com.lexur.yumo.ui.theme.ButtonPrimary
import com.lexur.yumo.ui.theme.Container
import com.lexur.yumo.ui.theme.Error
import com.lexur.yumo.ui.theme.IconOnPrimary
import com.lexur.yumo.ui.theme.IconPrimary
import com.lexur.yumo.ui.theme.IconSecondary
import com.lexur.yumo.ui.theme.OnBackground
import com.lexur.yumo.ui.theme.OnButtonPrimary
import com.lexur.yumo.ui.theme.OnContainer
import com.lexur.yumo.ui.theme.OnSecondary
import com.lexur.yumo.ui.theme.OnSurfaceVariant
import com.lexur.yumo.ui.theme.OnTopBar
import com.lexur.yumo.ui.theme.OutlineSecondary
import com.lexur.yumo.ui.theme.OutlineVariant
import com.lexur.yumo.ui.theme.Primary
import com.lexur.yumo.ui.theme.SecondaryVariant
import com.lexur.yumo.ui.theme.SurfaceVariant
import com.lexur.yumo.ui.theme.TopBarBackground

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

    // UI state
    val animationSpeedPresets = listOf(50L, 80L, 100L, 140L, 200L, 300L, 500L)
    var showPresetInfo by remember { mutableStateOf(false) }
    var showMotionInfo by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Check if character is hanging (static)
    val isHangingCharacter = character?.isHanging == true

    // Load character on startup and set running state
    LaunchedEffect(characterId, isCharacterRunning) {
        viewModel.loadCharacter(characterId)
        viewModel.setCharacterRunning(isCharacterRunning)
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${character?.name} Settings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = OnTopBar
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = IconOnPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TopBarBackground
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
            // Live Updates Info Card
            if (characterRunning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Container.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isHangingCharacter)
                                "Hanging character is active - changes will be applied instantly!"
                            else
                                "Character is running - changes will be applied instantly!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Primary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            // Character Type Info Card
            if (isHangingCharacter) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Container.copy(alpha = 0.3f)
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
                                color = OnBackground
                            )
                            Text(
                                text = if (motionSensingEnabled)
                                    "This character swings and tilts with device movement"
                                else
                                    "This character stays perfectly still",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Motion Sensing Control (only for hanging characters)
            if (isHangingCharacter) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (motionSensingEnabled)
                            Primary.copy(alpha = 0.1f)
                        else
                            SurfaceVariant.copy(alpha = 0.3f)
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
                                    tint = if (motionSensingEnabled) Primary else OnSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Motion Sensing",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    color = OnBackground,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                                IconButton(
                                    onClick = { showMotionInfo = !showMotionInfo },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Motion Info",
                                        tint = IconSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Switch(
                                checked = motionSensingEnabled,
                                onCheckedChange = { viewModel.setMotionSensingEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Primary,
                                    checkedTrackColor = Container,
                                    uncheckedThumbColor = IconSecondary,
                                    uncheckedTrackColor = SurfaceVariant
                                )
                            )
                        }

                        if (showMotionInfo) {
                            Text(
                                text = if (motionSensingEnabled) {
                                    "Character hangs from a fixed rope/chain at the top\n" +
                                            "Responds to device tilt and movement\n" +
                                            "May use slightly more battery"
                                } else {
                                    "Character remains completely static\n" +
                                            "Better battery life\n" +
                                            "No movement or tilting"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = OnBackground.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        // Motion sensitivity info when enabled
                        if (motionSensingEnabled && characterRunning) {
                            Text(
                                text = "Try tilting your device left/right to see the character swing!",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = Primary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            // Test Controls (only show when character is NOT running)
            if (!characterRunning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SecondaryVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Test Your Settings",
                            style = MaterialTheme.typography.titleSmall,
                            color = OnSecondary
                        )
                        Text(
                            text = if (isHangingCharacter) {
                                val motionText = if (motionSensingEnabled) " and swing like a pendulum" else ""
                                "Activate the character to see it positioned on screen$motionText"
                            } else {
                                "Start the character to see changes in real-time"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSecondary.copy(alpha = 0.8f),
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
                                containerColor = ButtonPrimary,
                                contentColor = OnButtonPrimary
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
                // Stop Test Button
                OutlinedButton(
                    onClick = {
                        viewModel.stopCharacterTest()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Error
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

            // Size Control (Single slider for both width and height)
            Text(
                text = "Character Size",
                style = MaterialTheme.typography.titleSmall,
                color = Primary.copy(alpha = 0.9f)
            )

            Text(
                text = "Size: ${size}px",
                style = MaterialTheme.typography.bodyLarge,
                color = OnBackground
            )
            Slider(
                value = size.toFloat(),
                onValueChange = { newSize ->
                    viewModel.updateSize(newSize.toInt())
                },
                valueRange = 10f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = Primary,
                    activeTrackColor = Primary.copy(alpha = 0.7f),
                    inactiveTrackColor = OutlineVariant
                )
            )

            // Position Control Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Use Button Controls",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnBackground
                )
                Switch(
                    checked = useButtonControls,
                    onCheckedChange = { viewModel.setUseButtonControls(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Primary,
                        checkedTrackColor = Container,
                        uncheckedThumbColor = IconSecondary,
                        uncheckedTrackColor = SurfaceVariant
                    )
                )
            }

            // Position Controls
            Text(
                text = "Position Settings ${if (characterRunning) "(Live Updates)" else ""}",
                style = MaterialTheme.typography.titleSmall,
                color = if (characterRunning) Primary else Primary.copy(alpha = 0.9f)
            )

            if (useButtonControls) {
                // Button Controls
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Display current position
                    Text(
                        text = "Position: X=${xPosition}px, Y=${yPosition}px",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnBackground
                    )

                    // Up button
                    IconButton(
                        onClick = { viewModel.movePosition(0, -1) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Move Up",
                            tint = Primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Left, Right buttons row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.movePosition(-1, 0) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Move Left",
                                tint = Primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Center space
                        Text(
                            text = "1px",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnBackground.copy(alpha = 0.7f)
                        )

                        IconButton(
                            onClick = { viewModel.movePosition(1, 0) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Move Right",
                                tint = Primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Down button
                    IconButton(
                        onClick = { viewModel.movePosition(0, 1) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move Down",
                            tint = Primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            } else {
                // Slider Controls
                // Horizontal Position Slider - ONLY for hanging characters
                if (isHangingCharacter) {
                    Text(
                        text = "Horizontal Position: $xPosition px from left",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnBackground
                    )
                    Slider(
                        value = xPosition.toFloat(),
                        onValueChange = { viewModel.updateXPosition(it.toInt()) },
                        valueRange = 0f..1000f,
                        colors = SliderDefaults.colors(
                            thumbColor = Primary,
                            activeTrackColor = Primary.copy(alpha = 0.7f),
                            inactiveTrackColor = OutlineVariant
                        )
                    )
                }

                // Vertical Position Slider - for both types
                Text(
                    text = "Vertical Position: $yPosition px from top",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnBackground
                )
                Slider(
                    value = yPosition.toFloat(),
                    onValueChange = { viewModel.updateYPosition(it.toInt()) },
                    valueRange = 0f..300f,
                    colors = SliderDefaults.colors(
                        thumbColor = Primary,
                        activeTrackColor = Primary.copy(alpha = 0.7f),
                        inactiveTrackColor = OutlineVariant
                    )
                )
            }

            // Speed control (disabled for hanging characters)
            Text(
                text = if (isHangingCharacter)
                    "Movement Speed: Static (no movement)"
                else
                    "Movement Speed: $speed px/frame",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isHangingCharacter)
                    OnBackground.copy(alpha = 0.5f)
                else
                    OnBackground
            )
            Slider(
                value = speed.toFloat(),
                onValueChange = { viewModel.updateSpeed(it.toInt()) },
                valueRange = 1f..10f,
                enabled = !isHangingCharacter,
                colors = SliderDefaults.colors(
                    thumbColor = Primary,
                    activeTrackColor = Primary.copy(alpha = 0.7f),
                    inactiveTrackColor = OutlineVariant
                )
            )

            // Animation Speed Control
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
                        OnBackground.copy(alpha = 0.5f)
                    else
                        OnBackground
                )
                IconButton(
                    onClick = { showPresetInfo = !showPresetInfo },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = IconSecondary,
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
                    color = OnBackground.copy(alpha = 0.7f)
                )
            }

            // Preset chips (disabled for hanging characters)
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
                            selectedContainerColor = Container,
                            selectedLabelColor = OnContainer,
                            containerColor = SurfaceVariant,
                            labelColor = OnSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            selectedBorderColor = Primary,
                            borderColor = OutlineSecondary,
                            selected = animationDelay == preset,
                            enabled = !isHangingCharacter
                        )
                    )
                }
            }

            // Save button
            Button(
                onClick = {
                    viewModel.saveSettings()
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonPrimary,
                    contentColor = OnButtonPrimary
                )
            ) {
                Text("Save Settings", style = MaterialTheme.typography.labelLarge)
            }

            // Reset to defaults button
            OutlinedButton(
                onClick = {
                    viewModel.resetToDefaults()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = IconPrimary
                )
            ) {
                Text("Reset to Defaults")
            }
        }
    }
}