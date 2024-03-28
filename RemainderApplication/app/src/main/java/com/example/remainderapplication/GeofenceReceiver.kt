package com.example.remainderapplication


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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class GeofenceReceiver : BroadcastReceiver() {
    // DatabaseReference for Firebase operations
    private lateinit var dbref: DatabaseReference

    // This method is called when the BroadcastReceiver receives an Intent broadcast.
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("GeofenceReceiver", "onReceive triggered")

        // Log all extras included with the intent for debugging
        intent?.extras?.keySet()?.forEach { key ->
            Log.d("GeofenceReceiver", "Extra $key: ${intent.extras?.get(key)}")
        }

        // Parse the Geofencing event from the received intent
        val geofencingEvent = intent?.let { GeofencingEvent.fromIntent(it) }

        // Check if the event has errors and log the error
        if (geofencingEvent?.hasError() == true) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e("GeofenceReceiver", "Geofencing event error: $errorMessage")
            return
        }

        // Retrieve UserID and RemID from the intent, crucial for identifying which geofence and user this event is for
        val userId = intent?.getStringExtra("UserID")
        val remId = intent?.getStringExtra("RemID")
        Log.d("GeofenceReceiver", "UserID from intent: $userId")
        Log.d("GeofenceReceiver", "RemID from intent: $remId")

        // If UserID or RemID is missing, log an error and return early
        if (userId == null || remId == null) {
            Log.e("GeofenceReceiver", "Intent is missing UserID or RemID")
            return
        }

        // Determine the type of geofence transition (enter, dwell, exit)
        val geofenceTransition = geofencingEvent?.geofenceTransition

        // Create a message based on the type of transition
        val transitionMessage = when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "Do not forget to"
            Geofence.GEOFENCE_TRANSITION_DWELL -> "Did you already"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "Hope you have achieved your task"
            else -> "Unknown geofence transition at"
        }

        // Handle the transition, potentially performing additional tasks like showing notifications
        handleTransition(context, userId, remId, transitionMessage, geofenceTransition)
    }

    // Handles the transition based on the geofence event
    private fun handleTransition(context: Context?, userId: String, remId: String, transitionMessage: String, transitionType: Int?) {
        // Query the Firebase database to retrieve the details of the reminder
        dbref = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Reminders").child(remId)
        dbref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Get the reminder details from Firebase and create a Member object
                val member = snapshot.getValue(Member::class.java)

                // If the member data is not null, proceed with the notification
                if (member != null) {
                    // Formatter for parsing the date in the reminder
                    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

                    // Parse the reminder date and compare it with the current date
                    val reminderDate = LocalDate.parse(member.date, formatter)
                    val currentDate = LocalDate.now()

                    // If the reminder date matches the current date, show a notification
                    if (reminderDate.isEqual(currentDate)) {
                        // Ensure that the context is not null before showing a notification
                        context?.let {
                            showNotification(it, member, transitionMessage, remId, transitionType)
                            // Update the reminder's activation status in Firebase
                            dbref.child("actStatus").setValue(true)
                        }
                    } else {
                        // If the date does not match, you can reset the activation status
                        dbref.child("actStatus").setValue(false)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Log an error if the Firebase operation is cancelled
                Log.e("GeofenceReceiver", "Failed to fetch reminder details: ${error.message}")
            }
        })
    }

    // Creates and displays a notification with the given details
    private fun showNotification(context: Context, member: Member, transitionMessage: String, remId: String, transitionType: Int?) {
        // Obtain the NotificationManager system service
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Use the remId to create a unique notification ID
        val notificationId = remId.hashCode()

        // Message details vary based on the type of geofence transition
        val detailedMessage = when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "$transitionMessage ${member.name}"
            Geofence.GEOFENCE_TRANSITION_DWELL -> "$transitionMessage ${member.name} ?"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "$transitionMessage ${member.name}"
            else -> "You're near ${member.name}'s location"
        }
        // For Android O and above, it's necessary to create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "ReminderNotifications",
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for Reminders"
                // Set the sound for the notification
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
                )
            }
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }

        // Build and issue the notification
        val notification = NotificationCompat.Builder(context, "ReminderNotifications")
            .setContentTitle(detailedMessage) // Title for the notification
            .setContentText("At ${member.address}") // Text for the notification
            .setSmallIcon(R.drawable.alarm_24) // Small icon for the notification
            .setPriority(NotificationCompat.PRIORITY_MAX) // Priority flag for the notification; max priority for heads-up display
            .build()

        // Notify the user with the built notification
        notificationManager.notify(notificationId, notification)
    }
}