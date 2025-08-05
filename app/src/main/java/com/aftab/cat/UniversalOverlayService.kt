package com.aftab.cat

import android.app.*
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.view.*
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import com.aftab.cat.home_screen.data.CharacterRepository
import com.aftab.cat.home_screen.data.model.Characters
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UniversalOverlayService : Service() {

    @Inject
    lateinit var characterRepository: CharacterRepository

    private val notificationId = 101
    private val channelId = "OverlayChannel"
    private val channelName = "Overlay Service"

    private var windowManager: WindowManager? = null
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

        fun startAnimation() {
            animationRunnable = object : Runnable {
                override fun run() {
                    updateCharacterAnimation()
                    updateCharacterPosition()
                    handler.postDelayed(this, character.animationDelay)
                }
            }.also {
                handler.post(it)
            }
        }

        fun stopAnimation() {
            animationRunnable?.let { handler.removeCallbacks(it) }
        }

        // Method to update character settings in real-time
        fun updateCharacterSettings(newCharacter: Characters) {
            this.character = newCharacter

            // Update image size
            imageView.layoutParams?.apply {
                width = (character.width * resources.displayMetrics.density).toInt()
                height = (character.height * resources.displayMetrics.density).toInt()
            }
            imageView.requestLayout()

            // Update Y position immediately
            params.y = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                character.yPosition - (systemBarsHeight / 3)
            } else {
                character.yPosition
            }

            try {
                windowManager?.updateViewLayout(overlayView, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun updateCharacterAnimation() {
            if (character.frameIds.isNotEmpty()) {
                imageView.setImageResource(character.frameIds[currentFrameIndex])
                currentFrameIndex = (currentFrameIndex + 1) % character.frameIds.size
            }
        }

        private fun updateCharacterPosition() {
            if (isMovingRight) {
                currentXPosition += character.speed
                if (currentXPosition >= screenWidth - (character.width * resources.displayMetrics.density).toInt()) {
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
            try {
                windowManager?.updateViewLayout(overlayView, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val displayMetrics = resources.displayMetrics
        screenWidth = displayMetrics.widthPixels
        systemBarsHeight = getSystemBarsHeight()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "STOP" -> {
                val characterId = intent.getStringExtra("character_id")
                if (characterId != null) {
                    stopCharacter(characterId)
                } else {
                    stopAllCharacters()
                }
                return START_NOT_STICKY
            }
            "UPDATE_SETTINGS" -> {
                val characterId = intent.getStringExtra("character_id") ?: return START_NOT_STICKY
                val character = characterRepository.getCharacterById(characterId) ?: return START_NOT_STICKY
                updateCharacterSettings(characterId, character)
                return START_STICKY
            }
            else -> {
                val characterId = intent?.getStringExtra("character_id") ?: return START_NOT_STICKY
                val character = characterRepository.getCharacterById(characterId) ?: return START_NOT_STICKY

                if (activeCharacters.containsKey(characterId)) {
                    // Character already exists, update its settings
                    updateCharacterSettings(characterId, character)
                    return START_STICKY
                }

                createCharacterOverlay(character)
                updateNotification()
                return START_STICKY
            }
        }
    }

    private fun updateCharacterSettings(characterId: String, newCharacter: Characters) {
        activeCharacters[characterId]?.updateCharacterSettings(newCharacter)
    }

    private fun createCharacterOverlay(character: Characters) {
        val overlayView = LayoutInflater.from(this).inflate(R.layout.character_overlay_layout, null)
        val imageView = overlayView.findViewById<ImageView>(R.id.character_image_view)

        // Set character size
        imageView.layoutParams?.apply {
            width = (character.width * resources.displayMetrics.density).toInt()
            height = (character.height * resources.displayMetrics.density).toInt()
        }

        val params = createWindowLayoutParams(character)

        try {
            windowManager?.addView(overlayView, params)
            val characterOverlay = CharacterOverlay(character, overlayView, imageView, params)
            activeCharacters[character.id] = characterOverlay
            characterOverlay.startAnimation()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopCharacter(characterId: String) {
        activeCharacters[characterId]?.let { overlay ->
            overlay.stopAnimation()
            try {
                windowManager?.removeView(overlay.overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            activeCharacters.remove(characterId)
        }
        updateNotification()

        if (activeCharacters.isEmpty()) {
            stopSelf()
        }
    }

    private fun stopAllCharacters() {
        activeCharacters.values.forEach { overlay ->
            overlay.stopAnimation()
            try {
                windowManager?.removeView(overlay.overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        activeCharacters.clear()
        stopSelf()
    }

    private fun updateNotification() {
        val notificationText = when (activeCharacters.size) {
            0 -> "No active pets"
            1 -> "${activeCharacters.values.first().character.name} is walking on your screen"
            else -> "${activeCharacters.size} pets are walking on your screen"
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Overlay Pets")
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.cat_walk_01)
            .build()

        startForeground(notificationId, notification)
    }

    private fun getSystemBarsHeight(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            val windowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            insets.top
        } else {
            (24 * resources.displayMetrics.density).toInt()
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
        params.x = 0
        params.y = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            character.yPosition - (systemBarsHeight / 3)
        } else {
            character.yPosition
        }

        return params
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAllCharacters()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}