package com.example.remainderapplication

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random


class GeofenceReceiver : BroadcastReceiver() {
    private lateinit var dbref: DatabaseReference

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("GeofenceReceiver", "onReceive triggered")
        intent?.extras?.keySet()?.forEach { key ->
            Log.d("GeofenceReceiver", "Extra $key: ${intent.extras?.get(key)}")
        }

        val geofencingEvent = intent?.let { GeofencingEvent.fromIntent(it) }
        if (geofencingEvent?.hasError() == true) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e("GeofenceReceiver", "Geofencing event error: $errorMessage")
            return
        }

        // Retrieve UserID and RemID from the intent
        val userId = intent?.getStringExtra("UserID")
        val remId = intent?.getStringExtra("RemID")
        Log.d("GeofenceReceiver", "UserID from intent: $userId")
        Log.d("GeofenceReceiver", "RemID from intent: $remId")

        if (userId == null || remId == null) {
            Log.e("GeofenceReceiver", "Intent is missing UserID or RemID")
            return
        // Stop execution if UserID or RemID is missing
        }

        // Handle each transition type distinctly
        when (geofencingEvent?.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                // Handle enter transition
                handleTransition(context, userId, remId, "Entered geofence area")
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                // Optionally handle dwell transition if needed
                handleTransition(context, userId, remId, "Dwelling in geofence area")
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                // Handle exit transition if needed
                handleTransition(context,userId, remId, "Leaving geofence area")
            }
            else -> {
                Log.e("GeofenceReceiver", "Unknown geofence transition")
            }
        }
    }

    private fun handleTransition(context: Context?,  userId: String, remId: String, transitionMessage: String) {


        // Retrieve the reminder details from Firebase
        dbref = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Reminders").child(remId)
        dbref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val member = snapshot.getValue(Member::class.java)
                if (member != null) {

                    context?.let { showNotification(it, member, transitionMessage, remId)
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("GeofenceReceiver", "Failed to fetch reminder details: ${error.message}")
            }
        })
    }

    private fun showNotification(context: Context, member: Member, transitionMessage: String, remId: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = remId.hashCode() // Example to derive notification ID from remID


        // Create a notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("ReminderNotifications", "Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications for Reminders"
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "ReminderNotifications")
            .setContentTitle("$transitionMessage: ${member.name}")
            .setContentText("Location: ${member.address}")
            .setSmallIcon(R.drawable.alarm_24) // Adjust icon as needed
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}