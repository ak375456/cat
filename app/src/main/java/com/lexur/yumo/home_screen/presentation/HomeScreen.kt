package com.lexur.yumo.home_screen.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lexur.yumo.billing.BillingViewModel
import com.lexur.yumo.componenets.PermissionExplanationDialog
import com.lexur.yumo.home_screen.presentation.components.AnimatedCharacterPreview
import com.lexur.yumo.home_screen.presentation.components.CategoryFilterSection
import com.lexur.yumo.home_screen.presentation.components.EnhancedPermissionWarningCard
import com.lexur.yumo.ui.theme.Background
import com.lexur.yumo.ui.theme.ButtonPrimary
import com.lexur.yumo.ui.theme.IconOnPrimary
import com.lexur.yumo.ui.theme.OnTopBar
import com.lexur.yumo.ui.theme.TopBarBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToCharacterSettings: (String) -> Unit = {},
    onNavigateToCustomCharacterCreation: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    navController: NavController,
    billingViewModel: BillingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val characters by viewModel.filteredCharacters.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    var expandedCharacterId by remember { mutableStateOf<String?>(null) }
    val runningCharacters by viewModel.runningCharacters.collectAsState()
    val showPermissionDialog by viewModel.showPermissionDialog.collectAsState()
    val showCreationDialog by viewModel.showCreationDialog.collectAsState()

    // Billing states
    val billingState by billingViewModel.billingState.collectAsState()

    // Check permissions
    var hasOverlayPermission by remember { mutableStateOf(checkOverlayPermission(context)) }
    var hasNotificationPermission by remember {
        mutableStateOf(checkNotificationPermission(context))
    }

    LaunchedEffect(Unit) {
        viewModel.bindToService(context)
    }

    LaunchedEffect(hasOverlayPermission, hasNotificationPermission) {
        if (hasOverlayPermission && hasNotificationPermission) {
            viewModel.syncWithService()
        }
    }

    LaunchedEffect(billingState.purchaseSuccess) {
        if (billingState.purchaseSuccess) {
            // Navigate to character creation
            onNavigateToCustomCharacterCreation()
            billingViewModel.resetPurchaseSuccess()
            billingViewModel.dismissPremiumDialog()
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
    if (showCreationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissCreationDialog() },
            title = { Text("Create Your Own Character") },
            text = { Text("Here, you can bring your own characters to life on your screen. They can be your partner, a friend, or anyone you can imagine!") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onDismissCreationDialog()
                        onNavigateToCustomCharacterCreation()
                    }
                ) {
                    Text("Next")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onDismissCreationDialog() }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Overlay Characters",
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
                            tint = IconOnPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TopBarBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("custom_character_creation")
                },
                containerColor = ButtonPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add custom character",
                )
            }
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
                    .fillMaxSize(),
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

                    AnimatedCharacterPreview (
                        character = character,
                        isExpanded = isExpanded,
                        onCardClick = {
                            expandedCharacterId =
                                if (expandedCharacterId == character.id) null else character.id
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
