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

        val geofencingEvent = intent?.let { GeofencingEvent.fromIntent(it) }
        if (geofencingEvent?.hasError() == true) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e("GeofenceReceiver", "Geofencing event error: $errorMessage")
            return
        }

        // Check for null context
        if (context == null) {
            Log.e("GeofenceReceiver", "Context is null")
            return
        }

        // Handle each transition type distinctly
        when (geofencingEvent?.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                // Handle enter transition
                handleTransition(context, intent, "Entered geofence area")
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                // Optionally handle dwell transition if needed
                 handleTransition(context, intent, "Dwelling in geofence area")
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                // Handle exit transition if needed
                handleTransition(context, intent, "Leaving geofence area")
            }
            else -> {
                Log.e("GeofenceReceiver", "Unknown geofence transition")
            }
        }
    }

    private fun handleTransition(context: Context, intent: Intent?, transitionMessage: String) {
        val remId = intent?.getStringExtra("RemID") ?: run {
            Log.e("GeofenceReceiver", "Intent is null or missing RemID")
            return // Stop execution if 'intent' is null or RemID is not found
        }
        val currentDate = LocalDate.now()
        // Retrieve the reminder details from Firebase
        dbref = FirebaseDatabase.getInstance().getReference("Member").child(remId)
        dbref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val member = snapshot.getValue(Member::class.java)
                if (member != null) {
                    val geofenceDate = LocalDate.parse(member.date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                // Check if the current date is the date when the geofence should trigger
                    if (currentDate.isEqual(geofenceDate)){
                    Log.d("GeofenceReceiver", "Fetched Member: $member") // Log the fetched member details
                    // Show a detailed notification with reminder details
                    showNotification(context, member, transitionMessage, remId)}
                    else{
                        // The dates do not match, do not show a notification
                        Log.d("GeofenceReceiver", "Geofence event date does not match, no notification shown.")
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
            val channel = NotificationChannel(remId, "Member", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Channel for Reminder notifications"
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())

            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, remId)
            .setContentTitle("$transitionMessage: ${member.name}")
            .setContentText("Location: ${member.address}")
            .setSmallIcon(R.drawable.alarm_24) // Adjust icon as needed
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
