package com.lexur.yumo.navigation.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    var characterRequestText by mutableStateOf("")
        private set

    var isSubmitting by mutableStateOf(false)
        private set

    var submitSuccess by mutableStateOf<Boolean?>(null)
        private set

    fun onCharacterRequestChange(newText: String) {
        characterRequestText = newText
        // Clear previous success/error state when user starts typing
        if (submitSuccess != null) {
            submitSuccess = null
        }
    }

    fun submitCharacterRequest(userId: String) {
        if (characterRequestText.isBlank()) return

        isSubmitting = true

        val characterRequest = mapOf(
            "userId" to userId,
            "characterRequest" to characterRequestText.trim(),
            "timestamp" to com.google.firebase.Timestamp.now(),
            "type" to "character_request"
        )

        // Using a more specific collection for character requests
        db.collection("character_requests")
            .document() // Auto-generate document ID to allow multiple requests
            .set(characterRequest)
            .addOnSuccessListener {
                submitSuccess = true
                characterRequestText = ""
            }
            .addOnFailureListener { exception ->
                submitSuccess = false
                // Log error for debugging (you might want to use proper logging)
                println("Error submitting character request: ${exception.message}")
            }
            .addOnCompleteListener {
                isSubmitting = false
            }
    }

    // Method to clear the success/error state
    fun clearSubmissionState() {
        submitSuccess = null
    }
}