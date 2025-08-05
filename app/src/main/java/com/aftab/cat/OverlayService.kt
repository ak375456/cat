package com.aftab.cat


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.aftab.cat.home_screen.data.model.Characters
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OverlayService : Service() {

    @Inject
    lateinit var overlayManager: SimpleOverlayManager

    private val binder = OverlayServiceBinder()

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "overlay_service_channel"
        const val ACTION_START_CHARACTER = "START_CHARACTER"
        const val ACTION_STOP_CHARACTER = "STOP_CHARACTER"
        const val ACTION_STOP_ALL = "STOP_ALL"
        const val EXTRA_CHARACTER = "character"
        const val EXTRA_CHARACTER_ID = "character_id"

        fun startCharacter(context: Context, character: Characters) {
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_START_CHARACTER
                putExtra(EXTRA_CHARACTER, character)
            }
            context.startForegroundService(intent)
        }

        fun stopCharacter(context: Context, characterId: String) {
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_STOP_CHARACTER
                putExtra(EXTRA_CHARACTER_ID, characterId)
            }
            context.startService(intent)
        }

        fun stopAllCharacters(context: Context) {
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_STOP_ALL
            }
            context.startService(intent)
        }
    }

    inner class OverlayServiceBinder : Binder() {
        fun getService(): OverlayService = this@OverlayService
    }

    override fun onCreate() {
        super.onCreate()
        overlayManager.initialize(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_CHARACTER -> {
                val character = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_CHARACTER, Characters::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_CHARACTER)
                }

                character?.let {
                    overlayManager.addCharacter(it)
                    startForeground(NOTIFICATION_ID, createNotification())
                }
            }
            ACTION_STOP_CHARACTER -> {
                val characterId = intent.getStringExtra(EXTRA_CHARACTER_ID)
                characterId?.let {
                    overlayManager.removeCharacter(it)

                    // Stop service if no characters are running
                    if (overlayManager.getActiveCharacterIds().isEmpty()) {
                        stopSelf()
                    } else {
                        // Update notification
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(NOTIFICATION_ID, createNotification())
                    }
                }
            }
            ACTION_STOP_ALL -> {
                overlayManager.removeAllCharacters()
                stopSelf()
            }
        }

        return START_STICKY // Restart service if killed by system
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        overlayManager.cleanup()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Overlay Pets Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps your overlay pets running"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val activeCount = overlayManager.getActiveCharacterIds().size

        // Intent to open the app when notification is tapped
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop all action
        val stopAllIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, OverlayService::class.java).apply { action = ACTION_STOP_ALL },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Overlay Pets Running")
            .setContentText("$activeCount pet${if (activeCount != 1) "s" else ""} active")
            .setSmallIcon(R.drawable.ic_notification) // You'll need to add this icon
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_stop, "Stop All", stopAllIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    // Methods to interact with the overlay manager
    fun addCharacter(character: Characters): Boolean {
        val success = overlayManager.addCharacter(character)
        if (success) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, createNotification())
        }
        return success
    }

    fun removeCharacter(characterId: String) {
        overlayManager.removeCharacter(characterId)

        if (overlayManager.getActiveCharacterIds().isEmpty()) {
            stopSelf()
        } else {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, createNotification())
        }
    }

    fun getActiveCharacterIds(): Set<String> = overlayManager.getActiveCharacterIds()

    fun isCharacterActive(characterId: String): Boolean = overlayManager.isCharacterActive(characterId)
}