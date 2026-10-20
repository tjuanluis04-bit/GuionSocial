package com.guiones.sociales

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object Notifier {
    const val CHANNEL = "guion_fijado"
    const val NOTIF_ID = 1001
    const val ACTION_NEXT = "com.guiones.sociales.NEXT"
    const val ACTION_PREV = "com.guiones.sociales.PREV"
    const val ACTION_DISMISS = "com.guiones.sociales.DISMISS"

    fun actionIntent(ctx: Context, action: String): PendingIntent =
        PendingIntent.getBroadcast(
            ctx, action.hashCode(),
            Intent(ctx, ActionReceiver::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    fun openAppIntent(ctx: Context): PendingIntent =
        PendingIntent.getActivity(
            ctx, 0, Intent(ctx, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel(CHANNEL, "Guion fijado", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Muestra tu guion en la pantalla de bloqueo"
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setShowBadge(false)
            }
            ctx.getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    @SuppressLint("MissingPermission")
    fun show(ctx: Context) {
        val store = Store(ctx)
        val script = store.loadScripts().firstOrNull { it.id == store.pinnedId }
        if (script == null) { cancel(ctx); return }
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        ensureChannel(ctx)
        val phrases = Phrases.split(script.text)
        val idx = store.pinnedIndex.coerceIn(0, phrases.lastIndex)

        val builder = NotificationCompat.Builder(ctx, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_script)
            .setContentTitle("${script.title}  ·  ${idx + 1}/${phrases.size}")
            .setContentText(phrases[idx])
            .setStyle(NotificationCompat.BigTextStyle().bigText(phrases[idx]))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setColor(0xFF2B6E8F.toInt())
            .setContentIntent(openAppIntent(ctx))
            .addAction(0, "‹ Anterior", actionIntent(ctx, ACTION_PREV))
            .addAction(0, "Siguiente ›", actionIntent(ctx, ACTION_NEXT))
            .addAction(0, "Quitar", actionIntent(ctx, ACTION_DISMISS))

        script.imagePath?.let { path -> Store.decode(path, 128)?.let { bmp -> builder.setLargeIcon(bmp) } }
        NotificationManagerCompat.from(ctx).notify(NOTIF_ID, builder.build())
    }

    fun cancel(ctx: Context) = NotificationManagerCompat.from(ctx).cancel(NOTIF_ID)
}
