package com.aftab.cat.home_screen.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.aftab.cat.componenets.PermissionExplanationDialog
import com.aftab.cat.home_screen.presentation.components.AnimatedCharacterPreviewCard
import com.aftab.cat.home_screen.presentation.components.CategoryFilterSection
import com.aftab.cat.home_screen.presentation.components.EnhancedPermissionWarningCard
import com.aftab.cat.ui.theme.Background
import com.aftab.cat.ui.theme.IconOnPrimary
import com.aftab.cat.ui.theme.OnTopBar
import com.aftab.cat.ui.theme.SurfaceVariant
import com.aftab.cat.ui.theme.TopBarBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToCharacterSettings: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val characters by viewModel.filteredCharacters.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    var expandedCharacterId by remember { mutableStateOf<String?>(null) }
    val runningCharacters by viewModel.runningCharacters.collectAsState()
    val showPermissionDialog by viewModel.showPermissionDialog.collectAsState()

    // Check permissions
    var hasOverlayPermission by remember { mutableStateOf(checkOverlayPermission(context)) }
    var hasNotificationPermission by remember {
        mutableStateOf(checkNotificationPermission(context))
    }

    // Initialize dialog state
    LaunchedEffect(Unit) {
        viewModel.initializeDialogState(context)
    }

    // Bind to service and sync state
    LaunchedEffect(Unit) {
        viewModel.bindToService(context)
    }

    LaunchedEffect(hasOverlayPermission, hasNotificationPermission) {
        if (hasOverlayPermission && hasNotificationPermission) {
            viewModel.syncWithService()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.unbindFromService(context)
        }
    }

    // Permission explanation dialog
    PermissionExplanationDialog(
        showDialog = showPermissionDialog,
        onDismiss = { viewModel.dismissPermissionDialog() },
        onDontShowAgain = { viewModel.setDontShowPermissionDialogAgain() },
        onContinue = { viewModel.dismissPermissionDialog() }
    )

    Scaffold(
        containerColor = Background, // Using custom background color
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Overlay Pets",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = OnTopBar // Using custom topbar text color
                    )
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = IconOnPrimary // Using custom icon color
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TopBarBackground // Using main color for topbar
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CategoryFilterSection(
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    viewModel.selectCategory(category)
                    expandedCharacterId = null
                },
                onClearFilter = {
                    viewModel.clearCategoryFilter()
                    expandedCharacterId = null
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Background, // Using custom background colors
                                SurfaceVariant.copy(alpha = 0.3f)
                            )
                        )
                    ),
                contentPadding = PaddingValues(
                    bottom = innerPadding.calculateBottomPadding() + 16.dp,
                    start = 12.dp,
                    end = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!hasOverlayPermission || !hasNotificationPermission) {
                    item(span = { GridItemSpan(2) }) {
                        EnhancedPermissionWarningCard(
                            onNavigateToSettings = onNavigateToSettings
                        )
                    }
                }

                items(items = characters, key = { it.id }) { character ->
                    val isRunning = runningCharacters.contains(character.id)
                    val isExpanded = expandedCharacterId == character.id

                    AnimatedCharacterPreviewCard(
                        character = character,
                        isExpanded = isExpanded,
                        onCardClick = {
                            expandedCharacterId = if (expandedCharacterId == character.id) null else character.id
                        },
                        onUseCharacter = {
                            if (hasOverlayPermission && hasNotificationPermission) {
                                viewModel.startCharacter(context, character)
                            } else {
                                onNavigateToSettings()
                            }
                            expandedCharacterId = null
                        },
                        onCharacterSettings = {
                            onNavigateToCharacterSettings(character.id)
                            expandedCharacterId = null
                        },
                        isCharacterRunning = isRunning,
                        onStopCharacter = {
                            viewModel.stopCharacter(context, character.id)
                        },
                        canUseCharacter = hasOverlayPermission && hasNotificationPermission
                    )
                }
            }
        }
    }
}

private fun checkOverlayPermission(context: Context): Boolean {
    return Settings.canDrawOverlays(context)
}

private fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true // Not required for older versions
    }
}