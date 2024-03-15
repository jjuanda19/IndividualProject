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

        val geofenceTransition = geofencingEvent?.geofenceTransition
        val transitionMessage = when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "Do not forget to"
            Geofence.GEOFENCE_TRANSITION_DWELL -> "Did you already"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "Hope you have achieved your task"
            else -> "Unknown geofence transition at"
        }

        // Pass the transition message directly to handleTransition
        handleTransition(context, userId, remId, transitionMessage, geofenceTransition)


    }

    private fun handleTransition(context: Context?,  userId: String, remId: String, transitionMessage: String, transitionType: Int?) {


        // Retrieve the reminder details from Firebase
        dbref = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Reminders").child(remId)
        dbref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val member = snapshot.getValue(Member::class.java)
                if (member != null) {
                    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                    val reminderDate = LocalDate.parse(member.date, formatter)
                    val currentDate = LocalDate.now()

                    if (reminderDate.isEqual(currentDate)) {
                        context?.let {
                            showNotification(it, member, transitionMessage, remId, transitionType)
                            dbref.child("actStatus").setValue(true)
                        }
                    } else {
                        // Optionally, set the geofence activation status to 0 if the conditions are not met
                        dbref.child("actStatus").setValue(false)
                    }
                }
            }



            override fun onCancelled(error: DatabaseError) {
                Log.e("GeofenceReceiver", "Failed to fetch reminder details: ${error.message}")
            }
        })
    }

    private fun showNotification(context: Context, member: Member, transitionMessage: String, remId: String,transitionType: Int?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = remId.hashCode() // Example to derive notification ID from remID

        // Determine the message based on transition type
        val detailedMessage = when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "$transitionMessage ${member.name}"
            Geofence.GEOFENCE_TRANSITION_DWELL -> "$transitionMessage ${member.name} ?"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "$transitionMessage ${member.name}"
            else -> "You're near ${member.name}'s location"
        }
        // Create a notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("ReminderNotifications", "Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications for Reminders"
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
            }
            notificationManager.createNotificationChannel(channel)
        }


        val notification = NotificationCompat.Builder(context, "ReminderNotifications")
            .setContentTitle(detailedMessage)
            .setContentText("At ${member.address}")
            .setSmallIcon(R.drawable.alarm_24) // Adjust icon as needed
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}