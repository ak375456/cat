package com.lexur.yumo

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.os.*
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import com.lexur.yumo.home_screen.data.model.Characters
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
    private var screenHeight = 0
    private var systemBarsHeight = 0

    // Track all active characters
    private val activeCharacters = mutableMapOf<String, CharacterOverlay>()

    // Motion sensing state
    private var isMotionSensingEnabled = true

    // State for landscape/rotation handling
    private var landscapePreferenceEnabled = false // User's setting
    private var isCurrentlyLandscape = false       // Current device orientation

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
        var baseRotation = 0f
        var currentSwayRotation = 0f

        // Physics simulation for hanging characters (pendulum-like)
        var velocityX = 0f
        var velocityY = 0f
        var rotationVelocity = 0f
        val damping = 0.94f
        val springStrength = 0.12f
        val rotationDamping = 0.96f
        val rotationSpring = 0.08f

        fun startAnimation() {
            if (isAnimating || !shouldBeVisible()) return

            if (character.isHanging) {
                if (character.isCustom && character.imagePath != null) {
                    val bitmap = BitmapFactory.decodeFile(character.imagePath)
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap)
                    }
                } else if (character.frameIds.isNotEmpty()) {
                    imageView.setImageResource(character.frameIds[0])
                }
                originalX = params.x
                originalY = params.y
                baseRotation = character.rotation
                imageView.rotation = baseRotation
                isAnimating = true
                return
            }

            isAnimating = true
            baseRotation = 0f
            imageView.rotation = 0f

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

            imageView.rotation = 0f
            baseRotation = 0f
            currentSwayRotation = 0f
        }

        fun updateCharacterSettings(newCharacter: Characters) {
            val wasHanging = this.character.isHanging
            val isNowHanging = newCharacter.isHanging
            val wasAtBottom = this.character.atBottom
            val isNowAtBottom = newCharacter.atBottom

            this.character = newCharacter
            this.baseRotation = newCharacter.rotation

            context?.let { ctx ->
                imageView.layoutParams?.apply {
                    width = (character.width * ctx.resources.displayMetrics.density).toInt()
                    height = (character.height * ctx.resources.displayMetrics.density).toInt()
                }
                imageView.requestLayout()

                if (isNowAtBottom) {
                    params.gravity = Gravity.BOTTOM or Gravity.START
                    params.y = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        character.yPosition
                    } else {
                        character.yPosition
                    }
                } else {
                    params.gravity = Gravity.TOP or Gravity.START
                    params.y = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        character.yPosition - (systemBarsHeight / 3)
                    } else {
                        character.yPosition
                    }
                }

                if (isNowHanging) {
                    params.x = character.xPosition
                    originalX = params.x
                    originalY = params.y
                } else {
                    if (wasHanging) {
                        currentXPosition = 0
                        params.x = 0
                    }
                }

                if (wasHanging != isNowHanging) {
                    if (isNowHanging) {
                        stopAnimation()
                        if (character.isCustom && character.imagePath != null) {
                            val bitmap = BitmapFactory.decodeFile(character.imagePath)
                            if (bitmap != null) {
                                imageView.setImageBitmap(bitmap)
                            }
                        } else if (character.frameIds.isNotEmpty()) {
                            imageView.setImageResource(character.frameIds[0])
                        }
                        params.x = character.xPosition
                        originalX = params.x
                        originalY = params.y
                        imageView.rotation = baseRotation
                        if (shouldBeVisible()) startAnimation()
                    } else {
                        currentXPosition = 0
                        isMovingRight = true
                        imageView.scaleX = 1f
                        imageView.rotation = 0f
                        currentSwayRotation = 0f
                        startAnimation()
                    }
                } else if (isNowHanging) {
                    if (character.isCustom && character.imagePath != null) {
                        val bitmap = BitmapFactory.decodeFile(character.imagePath)
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap)
                        }
                    } else if (character.frameIds.isNotEmpty()) {
                        imageView.setImageResource(character.frameIds[0])
                    }
                    params.x = character.xPosition
                    originalX = params.x
                    originalY = params.y
                    imageView.rotation = baseRotation + currentSwayRotation
                    if (shouldBeVisible() && !isAnimating) startAnimation()
                }

                if (wasAtBottom != isNowAtBottom && isNowHanging) {
                    originalY = params.y
                    currentSwayX = 0f
                    currentSwayY = 0f
                    currentSwayRotation = 0f
                    velocityX = 0f
                    velocityY = 0f
                    rotationVelocity = 0f
                    imageView.rotation = baseRotation
                }

                try {
                    windowManager?.updateViewLayout(overlayView, params)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun applyMotion(swayX: Float, swayY: Float) {
            if (!character.isHanging || !isAnimating) return

            try {
                val ropeLength = 80f
                val maxAngle = 30f
                val targetAngle = (swayX * 0.5f).coerceIn(-maxAngle, maxAngle)
                val angleRad = Math.toRadians(targetAngle.toDouble()).toFloat()

                val targetX = originalX + (sin(angleRad) * ropeLength)
                val targetY = originalY + (cos(angleRad) * ropeLength) - ropeLength

                val forceX = (targetX - (originalX + currentSwayX)) * springStrength
                val forceY = (targetY - (originalY + currentSwayY)) * springStrength

                val targetRotation = targetAngle * 0.8f
                val rotationForce = (targetRotation - currentSwayRotation) * rotationSpring

                velocityX += forceX
                velocityY += forceY
                rotationVelocity += rotationForce

                velocityX *= damping
                velocityY *= damping
                rotationVelocity *= rotationDamping

                currentSwayX += velocityX
                currentSwayY += velocityY
                currentSwayRotation += rotationVelocity

                val maxSwayX = ropeLength * 0.8f
                val maxSwayY = ropeLength * 0.3f
                val maxRotation = 25f

                currentSwayX = currentSwayX.coerceIn(-maxSwayX, maxSwayX)
                currentSwayY = currentSwayY.coerceIn(-maxSwayY, maxSwayY)
                currentSwayRotation = currentSwayRotation.coerceIn(-maxRotation, maxRotation)

                params.x = (originalX + currentSwayX).toInt()
                params.y = (originalY + currentSwayY).toInt()

                imageView.rotation = baseRotation + currentSwayRotation

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
                    if (!character.isHanging) {
                        val effectiveWidth = screenWidth - (character.width * ctx.resources.displayMetrics.density).toInt()

                        if (isMovingRight) {
                            currentXPosition += character.speed
                            if (currentXPosition >= effectiveWidth) {
                                isMovingRight = false
                                imageView.scaleX = -1f
                                currentXPosition = effectiveWidth
                            }
                        } else {
                            currentXPosition -= character.speed
                            if (currentXPosition <= 0) {
                                isMovingRight = true
                                imageView.scaleX = 1f
                                currentXPosition = 0
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

        fun shouldBeVisible(): Boolean {
            // Check the character's individual enableInLandscape setting
            return if (isCurrentlyLandscape) character.enableInLandscape else true
        }

        fun refreshVisibility() {
            val visible = shouldBeVisible()
            overlayView.visibility = if (visible) View.VISIBLE else View.GONE
            if (visible && !isAnimating) {
                startAnimation()
            } else if (!visible && isAnimating) {
                stopAnimation()
            }
        }
    }

    fun initialize(context: Context) {
        this.context = context.applicationContext
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val displayMetrics = context.resources.displayMetrics
        screenWidth = displayMetrics.widthPixels
        Log.d("Screen width", screenWidth.toString())
        screenHeight = displayMetrics.heightPixels
        systemBarsHeight = getSystemBarsHeight(context)

        isCurrentlyLandscape = context.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

        motionSensorManager.initialize(context)
        motionSensorManager.setMotionCallback { swayX, swayY ->
            if (isMotionSensingEnabled) {
                applyMotionToHangingCharacters(swayX, swayY)
            }
        }

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

                characterOverlay.refreshVisibility()

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

            checkAndStopMotionSensing()
        }
    }

    fun updateCharacterSettings(characterId: String, newCharacter: Characters) {
        activeCharacters[characterId]?.updateCharacterSettings(newCharacter)
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

    fun setEnableInLandscape(enabled: Boolean) {
        landscapePreferenceEnabled = enabled
        refreshAllViewsVisibility()
    }

    fun updateOrientation(isLandscape: Boolean) {
        isCurrentlyLandscape = isLandscape
        context?.let {
            val displayMetrics = it.resources.displayMetrics
            screenWidth = displayMetrics.widthPixels
            screenHeight = displayMetrics.heightPixels
        }
        refreshAllViewsVisibility()
    }

    // NEW: Get current screen width for use in UI
    fun getCurrentScreenWidth(): Int = screenWidth

    private fun refreshAllViewsVisibility() {
        activeCharacters.values.forEach {
            it.refreshVisibility()
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

        if (character.atBottom) {
            params.gravity = Gravity.BOTTOM or Gravity.START
            params.y = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                character.yPosition
            } else {
                character.yPosition
            }
        } else {
            params.gravity = Gravity.TOP or Gravity.START
            params.y = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                character.yPosition - (systemBarsHeight / 3)
            } else {
                character.yPosition
            }
        }

        params.x = if (character.isHanging) {
            character.xPosition
        } else {
            0
        }

        return params
    }
}