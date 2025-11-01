package com.lexur.yumo.custom_character.presentation


import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.pointerInteropFilter
import android.view.MotionEvent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Upload
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavController
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.lexur.yumo.R
import com.lexur.yumo.billing.BillingViewModel
import com.lexur.yumo.billing.PremiumFeatureDialog
import com.lexur.yumo.ui.theme.Background
import com.lexur.yumo.ui.theme.ButtonPrimary
import com.lexur.yumo.ui.theme.ButtonSecondary
import com.lexur.yumo.ui.theme.CardBackground
import com.lexur.yumo.ui.theme.Container
import com.lexur.yumo.ui.theme.DialogBackground
import com.lexur.yumo.ui.theme.Disabled
import com.lexur.yumo.ui.theme.IconDisabled
import com.lexur.yumo.ui.theme.IconPrimary
import com.lexur.yumo.ui.theme.IconSecondary
import com.lexur.yumo.ui.theme.InputBackground
import com.lexur.yumo.ui.theme.InputBorder
import com.lexur.yumo.ui.theme.InputBorderFocused
import com.lexur.yumo.ui.theme.InputPlaceholder
import com.lexur.yumo.ui.theme.InputText
import com.lexur.yumo.ui.theme.OnBackground
import com.lexur.yumo.ui.theme.OnButtonPrimary
import com.lexur.yumo.ui.theme.OnButtonSecondary
import com.lexur.yumo.ui.theme.OnCard
import com.lexur.yumo.ui.theme.OnContainerVariant
import com.lexur.yumo.ui.theme.OnDialog
import com.lexur.yumo.ui.theme.OnDisabled
import com.lexur.yumo.ui.theme.OnTopBar
import com.lexur.yumo.ui.theme.OutlinePrimary
import com.lexur.yumo.ui.theme.OutlineSecondary
import com.lexur.yumo.ui.theme.OutlineVariant
import com.lexur.yumo.ui.theme.Primary
import com.lexur.yumo.ui.theme.TopBarBackground

// Data class to hold tutorial step information
private data class TutorialStep(
    @get:DrawableRes val imageRes: Int,
    val title: String,
    val description: String
)

// List of tutorial steps
private val tutorialSteps = listOf(
    TutorialStep(
        imageRes = R.drawable.a, // Assuming a.webp is in your drawable folder
        title = "Step 1: Select Image",
        description = "Select an image and remove the background."
    ),
    TutorialStep(
        imageRes = R.drawable.b, // Assuming b.webp is in your drawable folder
        title = "Step 2: Choose a Rope",
        description = "Pick a rope style that you like the best."
    ),
    TutorialStep(
        imageRes = R.drawable.c, // Assuming c.webp is in your drawable folder
        title = "Step 3: Adjust & Save",
        description = "Adjust the rope size, character size, and position."
    )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class) // Added ExperimentalFoundationApi

@Composable
fun CustomCharacterCreationScreen(
    navController: NavController,
    viewModel: CustomCharacterCreationViewModel = hiltViewModel(),
    billingViewModel: BillingViewModel = hiltViewModel(), // Added BillingViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity // Get activity for billing

    // Billing states
    val billingState by billingViewModel.billingState.collectAsState()
    val showPremiumDialog by billingViewModel.showPremiumDialog.collectAsState()

    var showRopeSelection by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.onImageSelected(uri) }
    )

    val pngPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.onImageSelected(uri)
                viewModel.finishEditing()
                showRopeSelection = true
            }
        }
    )

    LaunchedEffect(uiState.saveComplete) {
        if (uiState.saveComplete) {
            navController.popBackStack()
            viewModel.onNavigationComplete()
        }
    }

    LaunchedEffect(uiState.selectedEmoji) {
        if (uiState.selectedEmoji != null && !showRopeSelection) {
            showRopeSelection = true
        }
    }

    // Handle successful purchase
    LaunchedEffect(billingState.purchaseSuccess) {
        if (billingState.purchaseSuccess) {
            // Purchase was successful, just dismiss the dialog.
            // The user can now click the button again to proceed.
            billingViewModel.resetPurchaseSuccess()
            billingViewModel.dismissPremiumDialog()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Show Premium Dialog over everything
        PremiumFeatureDialog(
            showDialog = showPremiumDialog,
            onDismiss = {
                billingViewModel.dismissPremiumDialog()
                billingViewModel.clearError()
            },
            onPurchase = {
                activity?.let { billingViewModel.purchasePremium(it) }
            },
            isLoading = billingState.isLoading,
            productPrice = billingState.availableProducts.firstOrNull()?.price ?: "",
            error = billingState.error
        )

        EmojiInputDialog(
            showDialog = uiState.showEmojiPicker,
            onDismiss = { viewModel.dismissEmojiPicker() },
            onEmojiSelected = { emoji ->
                viewModel.onEmojiSelected(emoji)
            },
            errorMessage = uiState.emojiError
        )

        when {
            uiState.showRopeAdjustment && uiState.selectedRopeResId != null -> {
                // Check if it's emoji or image
                if (uiState.selectedEmoji != null) {
                    // Emoji rope adjustment
                    EmojiRopeAdjustmentScreen(
                        emoji = uiState.selectedEmoji!!,
                        ropeResId = uiState.selectedRopeResId!!,
                        ropeScale = uiState.ropeScale,
                        ropeOffsetX = uiState.ropeOffsetX,
                        ropeOffsetY = uiState.ropeOffsetY,
                        onRopeScaleChanged = viewModel::updateRopeScale,
                        onRopeOffsetXChanged = viewModel::updateRopeOffsetX,
                        onRopeOffsetYChanged = viewModel::updateRopeOffsetY,
                        onConfirm = {
                            viewModel.finishRopeAdjustment()
                            showNameDialog = true
                        },
                        onNavigateBack = {
                            viewModel.finishRopeAdjustment()
                            showRopeSelection = true
                        },
                        characterScale = uiState.characterScale,
                        onCharacterScaleChanged = viewModel::updateCharacterScale,
                        isStrokeEnabled = uiState.isStrokeEnabled,
                        strokeColor = uiState.strokeColor,
                        onToggleStroke = viewModel::toggleImageStroke,
                        onStrokeColorChanged = viewModel::setStrokeColor
                    )
                } else {
                    // Regular image rope adjustment
                    RopeAdjustmentScreen(
                        imageUri = uiState.selectedImageUri!!,
                        maskPath = uiState.maskPath,
                        currentStrokePath = uiState.currentStrokePath,
                        ropeResId = uiState.selectedRopeResId!!,
                        ropeScale = uiState.ropeScale,
                        ropeOffsetX = uiState.ropeOffsetX,
                        ropeOffsetY = uiState.ropeOffsetY,
                        onRopeScaleChanged = viewModel::updateRopeScale,
                        onRopeOffsetXChanged = viewModel::updateRopeOffsetX,
                        onRopeOffsetYChanged = viewModel::updateRopeOffsetY,
                        onConfirm = {
                            viewModel.finishRopeAdjustment()
                            showNameDialog = true
                        },
                        onNavigateBack = {
                            viewModel.finishRopeAdjustment()
                            showRopeSelection = true
                        },
                        characterScale = uiState.characterScale,
                        onCharacterScaleChanged = viewModel::updateCharacterScale,
                        featheringSize = uiState.featheringSize,
                        isStrokeEnabled = uiState.isStrokeEnabled,
                        strokeColor = uiState.strokeColor,
                        onToggleStroke = viewModel::toggleImageStroke,
                        onStrokeColorChanged = viewModel::setStrokeColor
                    )
                }
            }

            showRopeSelection -> {
                RopeSelectionScreen(
                    onRopeSelected = { ropeResId ->
                        viewModel.onRopeSelected(ropeResId)
                        showRopeSelection = false
                    },
                    onNavigateBack = {
                        if (uiState.selectedEmoji != null) {
                            viewModel.resetEmojiSelection()
                        }
                        showRopeSelection = false
                    }
                )
            }

            showNameDialog -> {
                NameCharacterDialog(
                    onNameSelected = { name ->
                        viewModel.onCharacterNameChanged(name)
                        // Check if it's emoji or regular character
                        if (uiState.selectedEmoji != null) {
                            viewModel.saveEmojiCharacter(context)
                        } else {
                            viewModel.saveCustomCharacter(context)
                        }
                        showNameDialog = false
                    },
                    onDismiss = { showNameDialog = false }
                )
            }

            else -> {
                Scaffold(
                    containerColor = Background,
                    topBar = {
                        if (uiState.selectedImageUri != null) {
                            TopAppBar(
                                title = {
                                    Text(
                                        "Background Removal",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = OnTopBar
                                    )
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = TopBarBackground,
                                    titleContentColor = OnTopBar,
                                    actionIconContentColor = IconPrimary
                                ),
                                actions = {
                                    // Toggle Brush Mode
                                    IconButton(onClick = { viewModel.toggleBackgroundRemovalMode() }) {
                                        Icon(
                                            Icons.Default.Brush,
                                            contentDescription = "Toggle background removal mode",
                                            tint = if (uiState.isBackgroundRemovalMode && !uiState.isPanningMode)
                                                Primary
                                            else
                                                IconPrimary
                                        )
                                    }
                                    // Toggle Pan Mode
                                    if (uiState.isBackgroundRemovalMode) {
                                        IconButton(onClick = { viewModel.togglePanningMode() }) {
                                            Icon(
                                                Icons.Default.PanTool,
                                                contentDescription = "Toggle pan mode",
                                                tint = if (uiState.isPanningMode)
                                                    Primary
                                                else
                                                    IconPrimary
                                            )
                                        }
                                    }
                                    // Undo
                                    IconButton(
                                        onClick = { viewModel.undoLastStroke() },
                                        enabled = uiState.strokeHistory.isNotEmpty() && !uiState.isPanningMode
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Undo,
                                            contentDescription = "Undo",
                                            tint = if (uiState.strokeHistory.isNotEmpty() && !uiState.isPanningMode)
                                                IconPrimary
                                            else
                                                IconDisabled
                                        )
                                    }
                                    // Done
                                    IconButton(onClick = {
                                        viewModel.finishEditing()
                                        showRopeSelection = true
                                    }) {
                                        Icon(
                                            Icons.Default.Done,
                                            contentDescription = "Done",
                                            tint = IconPrimary
                                        )
                                    }
                                }
                            )
                        }
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center // <-- MODIFIED: This centers the scrollable column when content is short
                    ) {
                        when {
                            uiState.selectedImageUri == null -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth() // <-- MODIFIED: Changed from fillMaxSize
                                        .verticalScroll(rememberScrollState()) // <-- MODIFIED: Added scroll
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    // verticalArrangement = Arrangement.Center, // <-- MODIFIED: Removed this
                                ) {
                                    // Header Section
                                    Text(
                                        "Create Hanging Character",
                                        style = MaterialTheme.typography.displaySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = (-0.5).sp
                                        ),
                                        color = OnBackground,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // --- START: NEW TUTORIAL PAGER ---
                                    TutorialPager()
                                    // --- END: NEW TUTORIAL PAGER ---

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Privacy Notice Card
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .padding(vertical = 16.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Container
                                        ),
                                        elevation = CardDefaults.cardElevation(0.dp),
                                        border = BorderStroke(1.dp, OutlineVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = IconSecondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                "Custom characters are stored locally on your device. We never store or share your images.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = OnContainerVariant,
                                                lineHeight = 12.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))


                                    // Primary Button - Select Image
                                    Button(
                                        onClick = {
                                            if (billingState.isPremiumOwned) {
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            } else {
                                                billingViewModel.showPremiumDialog()
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .height(56.dp),
                                        shape = RoundedCornerShape(28.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ButtonPrimary,
                                            contentColor = OnButtonPrimary
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(
                                            defaultElevation = 0.dp,
                                            pressedElevation = 2.dp
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ImageSearch,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            "Select Image & Remove Background",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Secondary Button - Upload PNG
                                    OutlinedButton(
                                        onClick = {
                                            if (billingState.isPremiumOwned) {
                                                pngPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            } else {
                                                billingViewModel.showPremiumDialog()
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .height(56.dp),
                                        shape = RoundedCornerShape(28.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = ButtonSecondary,
                                            contentColor = OnButtonSecondary
                                        ),
                                        border = BorderStroke(1.5.dp, OutlinePrimary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Upload,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            "Upload Pre-cut PNG",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            if (billingState.isPremiumOwned) {
                                                viewModel.showEmojiPicker()
                                            } else {
                                                billingViewModel.showPremiumDialog()
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .height(56.dp),
                                        shape = RoundedCornerShape(28.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = ButtonSecondary,
                                            contentColor = OnButtonSecondary
                                        ),
                                        border = BorderStroke(1.5.dp, OutlinePrimary)
                                    ) {
                                        Text(
                                            "😊",
                                            style = MaterialTheme.typography.headlineSmall
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            "Hang an Emoji",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }

                                }
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    BackgroundRemovalCanvas(
                                        imageUri = uiState.selectedImageUri!!,
                                        brushSize = uiState.brushSize,
                                        isBackgroundRemovalMode = uiState.isBackgroundRemovalMode,
                                        isPanningMode = uiState.isPanningMode,
                                        maskPath = uiState.maskPath,
                                        currentStrokePath = uiState.currentStrokePath,
                                        previewPosition = uiState.previewPosition,
                                        canvasOffset = uiState.canvasOffset,
                                        canvasScale = uiState.canvasScale,
                                        featheringSize = uiState.featheringSize,
                                        onDrawStart = viewModel::startDrawing,
                                        onDrawContinue = viewModel::continueDrawing,
                                        onDrawEnd = viewModel::endDrawing,
                                        onPreviewMove = viewModel::updatePreviewPosition,
                                        onCanvasSize = viewModel::updateCanvasSize,
                                        onCanvasTransform = viewModel::onCanvasTransform
                                    )
                                }

                                if (uiState.isBackgroundRemovalMode && !uiState.isPanningMode) {
                                    // Brush Size Control
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 8.dp),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = CardBackground
                                        ),
                                        elevation = CardDefaults.cardElevation(0.dp),
                                        border = BorderStroke(1.dp, OutlineVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(20.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "Brush Size",
                                                    style = MaterialTheme.typography.labelLarge.copy(
                                                        fontWeight = FontWeight.Medium
                                                    ),
                                                    color = OnCard
                                                )
                                                Text(
                                                    "${uiState.brushSize.toInt()}px",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Primary
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Slider(
                                                value = uiState.brushSize,
                                                onValueChange = { viewModel.updateBrushSize(it) },
                                                valueRange = 10f..300f,
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = SliderDefaults.colors(
                                                    thumbColor = Primary,
                                                    activeTrackColor = Primary,
                                                    inactiveTrackColor = OutlineSecondary
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(60.dp)
                                                    .align(Alignment.CenterHorizontally),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Canvas(modifier = Modifier.size(60.dp)) {
                                                    drawCheckerboard()
                                                    drawCircle(
                                                        color = Primary.copy(alpha = 0.3f),
                                                        radius = uiState.brushSize / 2,
                                                        center = center
                                                    )
                                                    drawCircle(
                                                        color = Primary,
                                                        radius = uiState.brushSize / 2,
                                                        center = center,
                                                        style = Stroke(width = 2.dp.toPx())
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Edge Softness Control
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 8.dp),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = CardBackground
                                        ),
                                        elevation = CardDefaults.cardElevation(0.dp),
                                        border = BorderStroke(1.dp, OutlineVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(20.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "Edge Softness",
                                                    style = MaterialTheme.typography.labelLarge.copy(
                                                        fontWeight = FontWeight.Medium
                                                    ),
                                                    color = OnCard
                                                )
                                                Text(
                                                    "${uiState.featheringSize.toInt()}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Primary
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Slider(
                                                value = uiState.featheringSize,
                                                onValueChange = { viewModel.updateFeatheringSize(it) },
                                                valueRange = 0f..50f,
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = SliderDefaults.colors(
                                                    thumbColor = Primary,
                                                    activeTrackColor = Primary,
                                                    inactiveTrackColor = OutlineSecondary
                                                )
                                            )
                                        }
                                    }
                                }

                                // Bottom Action Buttons
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.resetImage() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp),
                                        shape = RoundedCornerShape(26.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = ButtonSecondary,
                                            contentColor = OnButtonSecondary
                                        ),
                                        border = BorderStroke(1.5.dp, OutlinePrimary)
                                    ) {
                                        Text(
                                            "Reset",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.finishEditing()
                                            showRopeSelection = true
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp),
                                        shape = RoundedCornerShape(26.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ButtonPrimary,
                                            contentColor = OnButtonPrimary
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(
                                            defaultElevation = 0.dp,
                                            pressedElevation = 2.dp
                                        )
                                    ) {
                                        Text(
                                            "Next",
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
            }
        }

        // Saving Overlay
        if (uiState.isSaving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OnBackground.copy(alpha = 0.7f))
                    .clickable(enabled = false, onClick = {}),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Primary,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Saving Character...",
                            color = OnCard,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

// --- START: NEW TUTORIAL COMPOSABLES ---

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TutorialPager() {
    val pagerState = rememberPagerState(pageCount = { tutorialSteps.size })

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp), // Adjust height as needed
            contentPadding = PaddingValues(horizontal = 32.dp) // Shows hints of next/prev items
        ) { page ->
            TutorialStepCard(step = tutorialSteps[page])
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pager Indicator
        PagerIndicator(pagerState = pagerState)
    }
}

@Composable
private fun TutorialStepCard(step: TutorialStep) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp), // Spacing between pages
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, OutlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = step.imageRes),
                contentDescription = step.title,
                contentScale = ContentScale.Fit, // Use Fit to see the whole 1080x2400 image
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Give image flexible space
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = step.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = OnCard,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodySmall,
                color = OnContainerVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PagerIndicator(pagerState: PagerState) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(pagerState.pageCount) { iteration ->
            val color = if (pagerState.currentPage == iteration) Primary else OutlineSecondary
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(color)
                    .size(8.dp)
            )
        }
    }
}

// --- END: NEW TUTORIAL COMPOSABLES ---

@Composable
fun NameCharacterDialog(
    onNameSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogBackground,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "Name Your Character",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = OnDialog
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Give your character a unique name",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnContainerVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text(
                            "Character Name",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = InputBackground,
                        unfocusedContainerColor = InputBackground,
                        focusedBorderColor = InputBorderFocused,
                        unfocusedBorderColor = InputBorder,
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = InputPlaceholder,
                        cursorColor = Primary,
                        focusedTextColor = InputText,
                        unfocusedTextColor = InputText
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onNameSelected(name) },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonPrimary,
                    contentColor = OnButtonPrimary,
                    disabledContainerColor = Disabled,
                    disabledContentColor = OnDisabled
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 2.dp
                ),
                modifier = Modifier.height(44.dp)
            ) {
                Text(
                    "Save",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = OnButtonSecondary
                ),
                modifier = Modifier.height(44.dp)
            ) {
                Text(
                    "Cancel",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    )
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun BackgroundRemovalCanvas(
    imageUri: Uri,
    brushSize: Float,
    isBackgroundRemovalMode: Boolean,
    isPanningMode: Boolean,
    maskPath: Path,
    currentStrokePath: Path,
    previewPosition: Offset?,
    canvasOffset: Offset,
    canvasScale: Float,
    featheringSize: Float,
    onDrawStart: (Offset) -> Unit,
    onDrawContinue: (Offset) -> Unit,
    onDrawEnd: () -> Unit,
    onPreviewMove: (Offset?) -> Unit,
    onCanvasSize: (IntSize) -> Unit,
    onCanvasTransform: (centroid: Offset, pan: Offset, zoom: Float) -> Unit,
) {
    val context = LocalContext.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val imageBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = imageUri) {
        value = try {
            context.contentResolver.openInputStream(imageUri)?.use {
                BitmapFactory.decodeStream(it)
            }?.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val transformation = remember(imageBitmap, canvasSize) {
        if (imageBitmap == null || canvasSize == IntSize.Zero) {
            null
        } else {
            calculateImageTransformation(imageBitmap!!, canvasSize)
        }
    }

    val processedBitmap = remember(imageBitmap, maskPath, currentStrokePath, featheringSize) {
        if (imageBitmap != null) {
            createTransparentBitmap(imageBitmap!!, maskPath, currentStrokePath, featheringSize)
        } else null
    }

    val interactionModifier = if (isBackgroundRemovalMode && transformation != null) {
        if (isPanningMode) {
            Modifier.pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    onCanvasTransform(centroid, pan, zoom)
                }
            }
        } else {
            Modifier.pointerInteropFilter { event ->
                val canvasTouchPos = Offset(event.x, event.y)

                // Convert canvas coordinates to image coordinates for accurate drawing
                val imageTouchPos = canvasToImageCoordinates(
                    screenPosition = canvasTouchPos,
                    transformation = transformation,
                    canvasOffset = canvasOffset,
                    canvasScale = canvasScale,
                    canvasSize = canvasSize
                )

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        onDrawStart(imageTouchPos)
                        onPreviewMove(imageTouchPos)
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        onDrawContinue(imageTouchPos)
                        onPreviewMove(imageTouchPos)
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        onDrawEnd()
                        onPreviewMove(null)
                        true
                    }
                    else -> false
                }
            }
        }
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Transparent)
            .onGloballyPositioned { coordinates ->
                if (canvasSize != coordinates.size) {
                    canvasSize = coordinates.size
                    onCanvasSize(coordinates.size)
                }
            }
            .then(interactionModifier)
    ) {
        // Layer 1 & 2: Checkerboard and Image (transformed together)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = canvasOffset.x
                    translationY = canvasOffset.y
                    scaleX = canvasScale
                    scaleY = canvasScale
                    // The default pivot is center, which is what our math now assumes
                }
        ) {
            drawCheckerboard()
            if (processedBitmap != null && transformation != null) {
                drawImage(
                    image = processedBitmap,
                    dstOffset = IntOffset(transformation.imageOffset.x.roundToInt(), transformation.imageOffset.y.roundToInt()),
                    dstSize = IntSize(transformation.displayedImageSize.width.roundToInt(), transformation.displayedImageSize.height.roundToInt())
                )
            } else if (imageBitmap != null && transformation != null) {
                drawImage(
                    image = imageBitmap!!,
                    dstOffset = IntOffset(transformation.imageOffset.x.roundToInt(), transformation.imageOffset.y.roundToInt()),
                    dstSize = IntSize(transformation.displayedImageSize.width.roundToInt(), transformation.displayedImageSize.height.roundToInt())
                )
            }
        }

        if (imageBitmap == null) {
            Image(
                painter = rememberAsyncImagePainter(model = imageUri),
                contentDescription = "Character Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // Layer 3: Drawing interaction and preview (on top, not transformed by graphicsLayer)
        if (isBackgroundRemovalMode && !isPanningMode && canvasSize != IntSize.Zero) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                previewPosition?.let { position ->
                    // Convert image coordinates back to screen coordinates for preview cursor
                    val canvasCenter = Offset(size.width / 2f, size.height / 2f)

                    // 1. Convert from image coordinates to the inner Canvas's layer coordinates
                    val layerPos = Offset(
                        x = position.x * transformation!!.scaleFactor + transformation.imageOffset.x,
                        y = position.y * transformation.scaleFactor + transformation.imageOffset.y
                    )

                    // 2. Apply the graphicsLayer transformations (scaling around center, then translation)
                    val screenPosition = ((layerPos - canvasCenter) * canvasScale) + canvasCenter + canvasOffset

                    val scaledBrushRadius = (brushSize / 2 * transformation.scaleFactor * canvasScale)
                    drawCircle(
                        color = Color.White,
                        radius = scaledBrushRadius + 2.dp.toPx(),
                        center = screenPosition,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = Color.Red,
                        radius = scaledBrushRadius,
                        center = screenPosition,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        // Magnifier
        val currentImageBitmap = imageBitmap
        if (isBackgroundRemovalMode && !isPanningMode &&
            previewPosition != null &&
            currentImageBitmap != null &&
            transformation != null &&
            canvasSize != IntSize.Zero
        ) {
            MagnifierPreview(
                position = previewPosition,
                imageBitmap = currentImageBitmap,
                transformation = transformation,
                canvasOffset = canvasOffset,
                canvasScale = canvasScale,
                canvasSize = canvasSize,
                maskPath = maskPath,
                currentStrokePath = currentStrokePath,
                featheringSize = featheringSize,
                brushSize = brushSize  // ADD THIS
            )
        }
    }
}

private fun canvasToImageCoordinates(
    screenPosition: Offset,
    transformation: ImageTransformation,
    canvasOffset: Offset,
    canvasScale: Float,
    canvasSize: IntSize
): Offset {
    // The center of the canvas is the pivot point for the graphicsLayer scale
    val canvasCenter = Offset(canvasSize.width / 2f, canvasSize.height / 2f)

    // The forward transformation is: screen_pos = ( (layer_pos - center) * scale ) + center + offset
    // We need to reverse this to find layer_pos from screen_pos.

    // 1. Reverse the canvasOffset translation
    val posAfterOffsetUndo = screenPosition - canvasOffset

    // 2. Reverse the scaling around the center pivot
    val layerPosition = ((posAfterOffsetUndo - canvasCenter) / canvasScale) + canvasCenter

    // `layerPosition` is now the coordinate within the inner Canvas's coordinate space.
    // This is the coordinate system where the image is drawn with the fit-transformation.

    // 3. Reverse the initial fit transformation to get the coordinate on the original image.
    val imageX = (layerPosition.x - transformation.imageOffset.x) / transformation.scaleFactor
    val imageY = (layerPosition.y - transformation.imageOffset.y) / transformation.scaleFactor

    return Offset(imageX, imageY)
}

@Composable
private fun MagnifierPreview(
    position: Offset,
    imageBitmap: ImageBitmap,
    transformation: ImageTransformation,
    canvasOffset: Offset,
    canvasScale: Float,
    canvasSize: IntSize,
    maskPath: Path,
    currentStrokePath: Path,
    brushSize: Float,
    featheringSize: Float
) {
    val loupeSize = 120.dp
    val loupeSizePx = with(LocalDensity.current) { loupeSize.toPx() }

    val canvasCenter = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
    val layerPos = Offset(
        x = position.x * transformation.scaleFactor + transformation.imageOffset.x,
        y = position.y * transformation.scaleFactor + transformation.imageOffset.y
    )
    val screenTouchPosition = ((layerPos - canvasCenter) * canvasScale) + canvasCenter + canvasOffset

    val imagePosition = position

    if (imagePosition.x < 0 || imagePosition.y < 0 ||
        imagePosition.x >= imageBitmap.width || imagePosition.y >= imageBitmap.height) {
        return
    }

    // This is the actual issue - we need to get the brush size from uiState
    // But since we don't have access to it here, we need to pass it as a parameter
    // For now, let's use a calculation that works regardless of brush size

    // The magnifier should always show the same relative area
    // Let's use a fixed reasonable size that shows context around the brush
    val viewportSize = (brushSize * 2.5f).coerceIn(100f, 400f) // Show 150 pixels of the actual image

    val srcSize = IntSize(
        viewportSize.toInt(),
        viewportSize.toInt()
    )

    val srcOffset = IntOffset(
        (imagePosition.x - srcSize.width / 2)
            .coerceIn(0f, (imageBitmap.width - srcSize.width).coerceAtLeast(0).toFloat())
            .roundToInt(),
        (imagePosition.y - srcSize.height / 2)
            .coerceIn(0f, (imageBitmap.height - srcSize.height).coerceAtLeast(0).toFloat())
            .roundToInt()
    )

    val clampedSrcSize = IntSize(
        srcSize.width.coerceAtMost(imageBitmap.width - srcOffset.x).coerceAtLeast(1),
        srcSize.height.coerceAtMost(imageBitmap.height - srcOffset.y).coerceAtLeast(1)
    )

    if (clampedSrcSize.width <= 0 || clampedSrcSize.height <= 0) {
        return
    }

    val loupeOffset = calculateLoupePosition(
        touchPosition = screenTouchPosition,
        loupeSizePx = loupeSizePx,
        containerSize = Size(canvasSize.width.toFloat(), canvasSize.height.toFloat())
    )

    val processedBitmap = remember(imageBitmap, maskPath, currentStrokePath, featheringSize) {
        createTransparentBitmap(imageBitmap, maskPath, currentStrokePath, featheringSize)
    }

    Canvas(
        modifier = Modifier
            .size(loupeSize)
            .offset { IntOffset(loupeOffset.x.roundToInt(), loupeOffset.y.roundToInt()) }
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .border(2.dp, Color.DarkGray, CircleShape)
    ) {
        drawCheckerboard()

        drawImage(
            image = processedBitmap,
            srcOffset = srcOffset,
            srcSize = clampedSrcSize,
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
        )

        val center = this.center
        val crosshairSize = 8.dp.toPx()
        drawLine(
            Color.Black.copy(alpha = 0.5f),
            start = Offset(center.x - crosshairSize, center.y),
            end = Offset(center.x + crosshairSize, center.y),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            Color.Black.copy(alpha = 0.5f),
            start = Offset(center.x, center.y - crosshairSize),
            end = Offset(center.x, center.y + crosshairSize),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Composable
private fun calculateLoupePosition(
    touchPosition: Offset,
    loupeSizePx: Float,
    containerSize: Size
): Offset {
    val density = LocalDensity.current
    val margin = with(density) { 16.dp.toPx() }

    // Position the loupe above the finger, with a small margin.
    val yPos = touchPosition.y - loupeSizePx - margin

    // Center the loupe horizontally on the finger.
    val xPos = touchPosition.x - (loupeSizePx / 2f)

    // Ensure the loupe stays within the screen bounds.
    return Offset(
        x = xPos.coerceIn(
            margin,
            containerSize.width - loupeSizePx - margin
        ),
        y = yPos.coerceIn(
            margin,
            containerSize.height - loupeSizePx - margin
        )
    )
}

fun DrawScope.drawCheckerboard() {
    val checkSize = 20.dp.toPx()
    val lightGray = Color.LightGray.copy(alpha = 0.3f)
    val darkGray = Color.Gray.copy(alpha = 0.3f)

    val cols = (size.width / checkSize).toInt() + 1
    val rows = (size.height / checkSize).toInt() + 1

    for (row in 0..rows) {
        for (col in 0..cols) {
            val color = if ((row + col) % 2 == 0) lightGray else darkGray
            drawRect(
                color = color,
                topLeft = Offset(col * checkSize, row * checkSize),
                size = Size(checkSize, checkSize)
            )
        }
    }
}

data class ImageTransformation(
    val scaleFactor: Float,
    val imageOffset: Offset,
    val displayedImageSize: Size
)

fun calculateImageTransformation(
    imageBitmap: ImageBitmap,
    canvasSize: IntSize
): ImageTransformation {
    val imageRatio = imageBitmap.width.toFloat() / imageBitmap.height
    val canvasRatio = canvasSize.width.toFloat() / canvasSize.height

    val scaleFactor: Float
    val displayedImageSize: Size
    val imageOffset: Offset

    if (imageRatio > canvasRatio) {
        scaleFactor = canvasSize.width.toFloat() / imageBitmap.width
        displayedImageSize = Size(
            width = canvasSize.width.toFloat(),
            height = imageBitmap.height * scaleFactor
        )
        imageOffset = Offset(
            x = 0f,
            y = (canvasSize.height - displayedImageSize.height) / 2
        )
    } else {
        scaleFactor = canvasSize.height.toFloat() / imageBitmap.height
        displayedImageSize = Size(
            width = imageBitmap.width * scaleFactor,
            height = canvasSize.height.toFloat()
        )
        imageOffset = Offset(
            x = (canvasSize.width - displayedImageSize.width) / 2,
            y = 0f
        )
    }

    return ImageTransformation(scaleFactor, imageOffset, displayedImageSize)
}

fun createTransparentBitmap(
    originalBitmap: ImageBitmap,
    maskPath: Path,
    currentStrokePath: Path,
    featheringSize: Float
): ImageBitmap {
    val androidBitmap = originalBitmap.asAndroidBitmap()
    val mutableBitmap = androidBitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = android.graphics.Canvas(mutableBitmap)
    val erasePaint = android.graphics.Paint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        // If featheringSize is greater than 0, apply a blur effect to the erase tool
        if (featheringSize > 0f) {
            maskFilter = BlurMaskFilter(featheringSize, BlurMaskFilter.Blur.NORMAL)
        }
    }
    val androidMaskPath = maskPath.asAndroidPath()
    val androidCurrentPath = currentStrokePath.asAndroidPath()
    canvas.drawPath(androidMaskPath, erasePaint)
    canvas.drawPath(androidCurrentPath, erasePaint)
    return mutableBitmap.asImageBitmap()
}
