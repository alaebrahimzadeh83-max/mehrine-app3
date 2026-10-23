package ir.mehrineh.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.app.Notification

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel="mehrineh_reminders"
        if(Build.VERSION.SDK_INT>=26) manager.createNotificationChannel(NotificationChannel(channel,"یادآوری‌های مهرینه",NotificationManager.IMPORTANCE_HIGH))
        val open=PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification=Notification.Builder(context,channel)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("یادآوری مهرینه")
            .setContentText(intent.getStringExtra("message") ?: "وقت یادآوری شماست")
            .setAutoCancel(true).setContentIntent(open).build()
        try { manager.notify((System.currentTimeMillis()%Int.MAX_VALUE).toInt(),notification) } catch(_:SecurityException) { }
    }
}
