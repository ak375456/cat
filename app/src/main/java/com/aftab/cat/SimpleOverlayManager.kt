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

@Singleton
class SimpleOverlayManager @Inject constructor() {

    private var windowManager: WindowManager? = null
    private var context: Context? = null
    private var screenWidth = 0
    private var systemBarsHeight = 0

    // Track all active characters
    private val activeCharacters = mutableMapOf<String, CharacterOverlay>()

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

        fun startAnimation() {
            if (isAnimating) return

            // Don't start animation for hanging characters (they are static)
            if (character.isHanging) {
                // Just set the static image and return
                if (character.frameIds.isNotEmpty()) {
                    imageView.setImageResource(character.frameIds[0])
                }
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
                }

                try {
                    windowManager?.updateViewLayout(overlayView, params)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
                    // Hanging characters don't move, so we skip position updates
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
        }
    }

    fun updateCharacterSettings(characterId: String, newCharacter: Characters) {
        activeCharacters[characterId]?.updateCharacterSettings(newCharacter)
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
    }

    fun getActiveCharacterIds(): Set<String> = activeCharacters.keys.toSet()

    fun isCharacterActive(characterId: String): Boolean = activeCharacters.containsKey(characterId)

    fun cleanup() {
        removeAllCharacters()
        context = null
        windowManager = null
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