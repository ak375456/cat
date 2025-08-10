package com.aftab.cat

import android.content.Context
import android.graphics.PixelFormat
import android.os.*
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import com.aftab.cat.home_screen.data.model.Characters
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class SimpleOverlayManager @Inject constructor(
    private val motionSensorManager: MotionSensorManager
) {

    private var windowManager: WindowManager? = null
    private var context: Context? = null
    private var screenWidth = 0
    private var systemBarsHeight = 0

    // Track all active characters
    private val activeCharacters = mutableMapOf<String, CharacterOverlay>()

    // Motion sensing state
    private var isMotionSensingEnabled = true

    private inner class CharacterOverlay(
        var character: Characters,
        val overlayView: View,
        val imageView: ImageView,
        val params: WindowManager.LayoutParams
    ) {
        var currentFrameIndex = 0
        var currentXPosition = 0
        var isMovingRight = true
        var handler = Handler(Looper.getMainLooper())
        var animationRunnable: Runnable? = null
        var isAnimating = false

        // For hanging characters motion
        var originalX = 0
        var originalY = 0
        var currentSwayX = 0f
        var currentSwayY = 0f
        var currentRotation = 0f // Add rotation for tilting effect

        // Physics simulation for hanging characters (pendulum-like)
        var velocityX = 0f
        var velocityY = 0f
        var rotationVelocity = 0f // Rotation velocity for tilting
        val damping = 0.94f // Higher damping for more realistic hanging motion
        val springStrength = 0.12f // Gentler spring for hanging effect
        val rotationDamping = 0.96f // Separate damping for rotation
        val rotationSpring = 0.08f // Spring strength for rotation

        fun startAnimation() {
            if (isAnimating) return

            // For hanging characters, just set the static image and handle motion
            if (character.isHanging) {
                if (character.frameIds.isNotEmpty()) {
                    imageView.setImageResource(character.frameIds[0])
                }
                // Store original position for hanging characters
                originalX = params.x
                originalY = params.y
                return
            }

            isAnimating = true

            animationRunnable = object : Runnable {
                override fun run() {
                    if (isAnimating) {
                        updateCharacterAnimation()
                        updateCharacterPosition()
                        handler.postDelayed(this, character.animationDelay)
                    }
                }
            }.also {
                handler.post(it)
            }
        }

        fun stopAnimation() {
            isAnimating = false
            animationRunnable?.let { handler.removeCallbacks(it) }
            animationRunnable = null

            // Reset rotation for hanging characters
            if (character.isHanging) {
                imageView.rotation = 0f
                currentRotation = 0f
            }
        }

        fun updateCharacterSettings(newCharacter: Characters) {
            val wasHanging = this.character.isHanging
            val isNowHanging = newCharacter.isHanging

            this.character = newCharacter

            context?.let { ctx ->
                imageView.layoutParams?.apply {
                    width = (character.width * ctx.resources.displayMetrics.density).toInt()
                    height = (character.height * ctx.resources.displayMetrics.density).toInt()
                }
                imageView.requestLayout()

                // Update Y position
                params.y = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    character.yPosition - (systemBarsHeight / 3)
                } else {
                    character.yPosition
                }

                // Update X position for hanging characters
                if (isNowHanging) {
                    params.x = character.xPosition
                    originalX = params.x
                    originalY = params.y
                }

                // If character changed from/to hanging, handle animation state
                if (wasHanging != isNowHanging) {
                    if (isNowHanging) {
                        // Stop animation for hanging character
                        stopAnimation()
                        if (character.frameIds.isNotEmpty()) {
                            imageView.setImageResource(character.frameIds[0])
                        }
                        // Position hanging character at specified X position
                        params.x = character.xPosition
                        originalX = params.x
                        originalY = params.y
                    } else {
                        // Start animation for non-hanging character
                        currentXPosition = 0
                        isMovingRight = true
                        imageView.scaleX = 1f
                        startAnimation()
                    }
                } else if (isNowHanging) {
                    // Update static image and position for hanging character
                    if (character.frameIds.isNotEmpty()) {
                        imageView.setImageResource(character.frameIds[0])
                    }
                    params.x = character.xPosition
                    originalX = params.x
                    originalY = params.y
                }

                try {
                    windowManager?.updateViewLayout(overlayView, params)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun applyMotion(swayX: Float, swayY: Float) {
            if (!character.isHanging) return

            try {
                // Define rope length (distance from attachment point to character center)
                val ropeLength = 80f // Adjust this based on your rope visual length

                // Calculate pendulum angle based on device tilt
                // Limit the maximum swing angle for realistic motion
                val maxAngle = 30f // Maximum swing angle in degrees
                val targetAngle = (swayX * 0.5f).coerceIn(-maxAngle, maxAngle)

                // Convert angle to radians for calculation
                val angleRad = Math.toRadians(targetAngle.toDouble()).toFloat()

                // Calculate character position based on pendulum physics
                // The rope attachment point stays at (originalX, originalY)
                val targetX = originalX + (sin(angleRad) * ropeLength)
                val targetY = originalY + (cos(angleRad) * ropeLength) - ropeLength // Subtract ropeLength to account for hanging

                // Apply spring physics for smooth swinging motion
                val forceX = (targetX - (originalX + currentSwayX)) * springStrength
                val forceY = (targetY - (originalY + currentSwayY)) * springStrength

                // Apply rotation force for character tilting
                val targetRotation = targetAngle * 0.8f // Character tilts with the swing
                val rotationForce = (targetRotation - currentRotation) * rotationSpring

                // Update velocities with forces
                velocityX += forceX
                velocityY += forceY
                rotationVelocity += rotationForce

                // Apply damping for realistic pendulum motion
                velocityX *= damping
                velocityY *= damping
                rotationVelocity *= rotationDamping

                // Update positions
                currentSwayX += velocityX
                currentSwayY += velocityY
                currentRotation += rotationVelocity

                // Apply constraints to keep motion realistic
                val maxSwayX = ropeLength * 0.8f // Based on rope length
                val maxSwayY = ropeLength * 0.3f // Limited vertical movement
                val maxRotation = 25f

                currentSwayX = currentSwayX.coerceIn(-maxSwayX, maxSwayX)
                currentSwayY = currentSwayY.coerceIn(-maxSwayY, maxSwayY)
                currentRotation = currentRotation.coerceIn(-maxRotation, maxRotation)

                // IMPORTANT: Only move the character, not the rope attachment point
                // The rope attachment point remains at (originalX, originalY)
                params.x = (originalX + currentSwayX).toInt()
                params.y = (originalY + currentSwayY).toInt()

                // Apply rotation to create tilting effect
                imageView.rotation = currentRotation

                windowManager?.updateViewLayout(overlayView, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun updateCharacterAnimation() {
            try {
                if (character.frameIds.isNotEmpty() && character.animationDelay > 0L) {
                    imageView.setImageResource(character.frameIds[currentFrameIndex])
                    currentFrameIndex = (currentFrameIndex + 1) % character.frameIds.size
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun updateCharacterPosition() {
            try {
                context?.let { ctx ->
                    // Only move non-hanging characters
                    if (!character.isHanging) {
                        if (isMovingRight) {
                            currentXPosition += character.speed
                            if (currentXPosition >= screenWidth - (character.width * ctx.resources.displayMetrics.density).toInt()) {
                                isMovingRight = false
                                imageView.scaleX = -1f
                            }
                        } else {
                            currentXPosition -= character.speed
                            if (currentXPosition <= 0) {
                                isMovingRight = true
                                imageView.scaleX = 1f
                            }
                        }

                        params.x = currentXPosition
                        windowManager?.updateViewLayout(overlayView, params)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun initialize(context: Context) {
        this.context = context.applicationContext
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val displayMetrics = context.resources.displayMetrics
        screenWidth = displayMetrics.widthPixels
        systemBarsHeight = getSystemBarsHeight(context)

        // Initialize motion sensor
        motionSensorManager.initialize(context)
        motionSensorManager.setMotionCallback { swayX, swayY ->
            if (isMotionSensingEnabled) {
                applyMotionToHangingCharacters(swayX, swayY)
            }
        }

        // Start motion sensing if there are hanging characters
        checkAndStartMotionSensing()
    }

    fun addCharacter(character: Characters): Boolean {
        return try {
            context?.let { ctx ->
                if (activeCharacters.containsKey(character.id)) {
                    updateCharacterSettings(character.id, character)
                    return true
                }
                val tempParent = FrameLayout(ctx)
                val overlayView = LayoutInflater.from(ctx).inflate(R.layout.character_overlay_layout, tempParent, false)
                val imageView = overlayView.findViewById<ImageView>(R.id.character_image_view)

                imageView.layoutParams?.apply {
                    width = (character.width * ctx.resources.displayMetrics.density).toInt()
                    height = (character.height * ctx.resources.displayMetrics.density).toInt()
                }

                val params = createWindowLayoutParams(character)

                windowManager?.addView(overlayView, params)
                val characterOverlay = CharacterOverlay(character, overlayView, imageView, params)
                activeCharacters[character.id] = characterOverlay
                characterOverlay.startAnimation()

                // Start motion sensing if this is a hanging character
                if (character.isHanging) {
                    checkAndStartMotionSensing()
                }

                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun removeCharacter(characterId: String) {
        activeCharacters[characterId]?.let { overlay ->
            overlay.stopAnimation()
            try {
                windowManager?.removeView(overlay.overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            activeCharacters.remove(characterId)

            // Stop motion sensing if no hanging characters remain
            checkAndStopMotionSensing()
        }
    }

    fun updateCharacterSettings(characterId: String, newCharacter: Characters) {
        activeCharacters[characterId]?.updateCharacterSettings(newCharacter)
        // Check if we need to start/stop motion sensing
        checkAndStartMotionSensing()
    }

    fun removeAllCharacters() {
        activeCharacters.values.forEach { overlay ->
            overlay.stopAnimation()
            try {
                windowManager?.removeView(overlay.overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        activeCharacters.clear()
        motionSensorManager.stopListening()
    }

    fun getActiveCharacterIds(): Set<String> = activeCharacters.keys.toSet()

    fun isCharacterActive(characterId: String): Boolean = activeCharacters.containsKey(characterId)

    fun setMotionSensingEnabled(enabled: Boolean) {
        isMotionSensingEnabled = enabled
        if (enabled) {
            checkAndStartMotionSensing()
        } else {
            motionSensorManager.stopListening()
        }
    }

    fun cleanup() {
        removeAllCharacters()
        motionSensorManager.cleanup()
        context = null
        windowManager = null
    }

    private fun hasHangingCharacters(): Boolean {
        return activeCharacters.values.any { it.character.isHanging }
    }

    private fun checkAndStartMotionSensing() {
        if (hasHangingCharacters() && isMotionSensingEnabled) {
            motionSensorManager.startListening()
        }
    }

    private fun checkAndStopMotionSensing() {
        if (!hasHangingCharacters()) {
            motionSensorManager.stopListening()
        }
    }

    private fun applyMotionToHangingCharacters(swayX: Float, swayY: Float) {
        activeCharacters.values.forEach { overlay ->
            if (overlay.character.isHanging) {
                overlay.applyMotion(swayX, swayY)
            }
        }
    }

    private fun getSystemBarsHeight(context: Context): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val windowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            insets.top
        } else {
            (24 * context.resources.displayMetrics.density).toInt()
        }
    }

    private fun createWindowLayoutParams(character: Characters): WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START

        // Position hanging characters at specified X position, others at left edge
        params.x = if (character.isHanging) {
            character.xPosition
        } else {
            0
        }

        params.y = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            character.yPosition - (systemBarsHeight / 3)
        } else {
            character.yPosition
        }

        return params
    }
}