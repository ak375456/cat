package com.lexur.yumo.custom_character.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lexur.yumo.ui.theme.*
import com.lexur.yumo.util.EmojiUtils

@Composable
fun EmojiInputDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onEmojiSelected: (String) -> Unit,
    errorMessage: String?
) {
    if (!showDialog) return

    var emojiInput by remember { mutableStateOf("") }

    // Real-time validation feedback
    val isValid = remember(emojiInput) {
        emojiInput.isNotBlank() && EmojiUtils.isValidSingleEmojiSimple(emojiInput)
    }

    val validationMessage = remember(emojiInput) {
        when {
            emojiInput.isBlank() -> ""
            isValid -> "✓ Valid emoji!"
            else -> "Please enter only one emoji"
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = DialogBackground
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Select an Emoji",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = OnDialog
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = IconSecondary
                        )
                    }
                }

                // Description
                Text(
                    "Tap the input field below and select a single emoji from your keyboard",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnContainerVariant,
                    textAlign = TextAlign.Center
                )

                // Emoji preview (if valid)
                if (emojiInput.isNotBlank() && isValid) {
                    Text(
                        text = emojiInput,
                        fontSize = 72.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // Emoji input field
                OutlinedTextField(
                    value = emojiInput,
                    onValueChange = { newValue ->
                        // Limit input length to prevent issues
                        if (newValue.length <= 50) {
                            emojiInput = newValue
                        }
                    },
                    label = { Text("Emoji") },
                    placeholder = { Text("😊") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = InputBackground,
                        unfocusedContainerColor = InputBackground,
                        focusedBorderColor = if (emojiInput.isNotBlank() && isValid) Primary else InputBorderFocused,
                        unfocusedBorderColor = InputBorder,
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = InputPlaceholder,
                        cursorColor = Primary,
                        focusedTextColor = InputText,
                        unfocusedTextColor = InputText,
                        errorBorderColor = Error,
                        errorLabelColor = Error
                    ),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        textAlign = TextAlign.Center
                    ),
                    isError = emojiInput.isNotBlank() && !isValid
                )

                // Validation feedback
                if (emojiInput.isNotBlank()) {
                    Text(
                        text = validationMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isValid) Primary else Error,
                        textAlign = TextAlign.Center
                    )
                }

                // Error message from parent
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Error,
                        textAlign = TextAlign.Center
                    )
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = ButtonSecondary,
                            contentColor = OnButtonSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, OutlinePrimary)
                    ) {
                        Text(
                            "Cancel",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Button(
                        onClick = {
                            if (emojiInput.isNotBlank() && isValid) {
                                onEmojiSelected(emojiInput.trim())
                            }
                        },
                        enabled = emojiInput.isNotBlank() && isValid,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonPrimary,
                            contentColor = OnButtonPrimary,
                            disabledContainerColor = Disabled,
                            disabledContentColor = OnDisabled
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Text(
                            "Continue",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // Helpful tip
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Container.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "💡 Examples of valid emojis:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = OnContainerVariant
                        )
                        Text(
                            "😊 🎉 🔥 ❤️ 👍 🌟 🎈 🦄 🌈 ⭐",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnContainerVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "✓ Single emojis work\n✓ Emojis with skin tones work (👍🏽)\n✓ Combined emojis work (👨‍👩‍👧)\n✗ Multiple emojis don't work\n✗ Text + emoji doesn't work",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnContainerVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}