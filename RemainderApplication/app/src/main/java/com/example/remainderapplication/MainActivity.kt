package com.example.remainderapplication

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.vo.Database
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.random.Random


class MainActivity : AppCompatActivity(), Adapter.OnItemDeleteListener {

    private lateinit var dbref: DatabaseReference
    private lateinit var memberRecyclerView: RecyclerView
    private lateinit var memberArrayList: ArrayList<Member>

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isCoarseLocationPermissionGranted = false
    private var isFineLocationPermissionGranted = false
    private var isBackgroundLocationPermissionGranted = false
    private var isInternetPermissionGranted = false
    private var isWakeLockPermissionGranted = false
    private var isReadExternalStoragePermissionGranted = false
    private var isWriteExternalStoragePermissionGranted = false
    private var isManageExternalStoragePermissionGranted = false
    private var isNetworkPermissionGranted = false
    private lateinit var singouttext: TextView
    private var isDataFetchedInitially = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        memberArrayList = arrayListOf<Member>()

        memberRecyclerView = findViewById(R.id.RemList)
        memberRecyclerView.layoutManager = LinearLayoutManager(this)
        memberRecyclerView.setHasFixedSize(true)
        memberRecyclerView.adapter = Adapter(memberArrayList, this)
        getUserData()

        singouttext = findViewById(R.id.textSingOut)

        val content = SpannableString(singouttext.text.toString())
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        singouttext.text = content

        singouttext.setOnClickListener {
            intent = Intent(this, SingInActivity::class.java)
            startActivity(intent)
            finish()
        }


        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isCoarseLocationPermissionGranted =
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION]
                        ?: isCoarseLocationPermissionGranted
                isFineLocationPermissionGranted =
                    permissions[Manifest.permission.ACCESS_FINE_LOCATION]
                        ?: isFineLocationPermissionGranted
                isBackgroundLocationPermissionGranted =
                    permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION]
                        ?: isBackgroundLocationPermissionGranted
                isInternetPermissionGranted =
                    permissions[Manifest.permission.INTERNET] ?: isInternetPermissionGranted
                isWakeLockPermissionGranted =
                    permissions[Manifest.permission.WAKE_LOCK] ?: isWakeLockPermissionGranted
                isReadExternalStoragePermissionGranted =
                    permissions[Manifest.permission.READ_EXTERNAL_STORAGE]
                        ?: isReadExternalStoragePermissionGranted
                isWriteExternalStoragePermissionGranted =
                    permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
                        ?: isWriteExternalStoragePermissionGranted
                isManageExternalStoragePermissionGranted =
                    permissions[Manifest.permission.MANAGE_EXTERNAL_STORAGE]
                        ?: isManageExternalStoragePermissionGranted
                isNetworkPermissionGranted = permissions[Manifest.permission.ACCESS_NETWORK_STATE]
                    ?: isNetworkPermissionGranted

            }
        requestPermission()

        val buttonAddReminder = findViewById<Button>(R.id.buttonAddReminder)
        buttonAddReminder.setOnClickListener {
            val intent = Intent(this, HubActivity::class.java)
            startActivity(intent)
        }

    }

    private fun requestPermission() {
        isCoarseLocationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        isFineLocationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        isBackgroundLocationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        isInternetPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.INTERNET
        ) == PackageManager.PERMISSION_GRANTED

        isWakeLockPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WAKE_LOCK
        ) == PackageManager.PERMISSION_GRANTED

        isReadExternalStoragePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isWriteExternalStoragePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isManageExternalStoragePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isNetworkPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_NETWORK_STATE
        ) == PackageManager.PERMISSION_GRANTED

        val permissionRequest: MutableList<String> = ArrayList()

        if (!isCoarseLocationPermissionGranted) {
            permissionRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        }
        if (!isFineLocationPermissionGranted) {
            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!isBackgroundLocationPermissionGranted) {
            permissionRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        if (!isInternetPermissionGranted) {
            permissionRequest.add(Manifest.permission.INTERNET)
        }
        if (!isWakeLockPermissionGranted) {
            permissionRequest.add(Manifest.permission.WAKE_LOCK)
        }
        if (!isReadExternalStoragePermissionGranted) {
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!isWriteExternalStoragePermissionGranted) {
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!isManageExternalStoragePermissionGranted) {
            permissionRequest.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        }
        if (!isNetworkPermissionGranted) {
            permissionRequest.add(Manifest.permission.ACCESS_NETWORK_STATE)
        }
        if (permissionRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }
    }

    private fun getUserData() {

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("MainActivity", "User is not logged in.")
            return
        }
        dbref =
            FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Reminders")

        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isDataFetchedInitially) {
                    memberArrayList.clear()

                    for (userSnapshot in snapshot.children) {

                        val reminder = userSnapshot.getValue(Member::class.java)
                        if (reminder != null) { // Check for null to avoid adding null values to the list
                            reminder?.let {
                                memberArrayList.add(it)
                            }
                        }
                    }
                        memberRecyclerView.adapter?.notifyDataSetChanged()
                        isDataFetchedInitially = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Failed to fetch members: ${error.message}")
            }
        })
    }

    override fun onDeleteClick(position: Int) {
        // Get the ID of the reminder you want to delete
        val reminderToDelete = memberArrayList[position]

        // Assuming each Member has a unique ID field
        val reminderId = reminderToDelete.RemID

        deleteReminderFromFirebase(reminderId, position)
    }

    private fun deleteReminderFromFirebase(reminderId: String, position: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        dbref =
            FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Reminders")
                .child(reminderId)

        dbref.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Reminder deleted successfully", Toast.LENGTH_SHORT).show()
                // Remove the item from your list and notify the adapter
                if (position >= 0 && position < memberArrayList.size) {
                    memberArrayList.removeAt(position)
                    memberRecyclerView.adapter?.notifyItemRemoved(position)
                    memberRecyclerView.adapter?.notifyItemRangeChanged(
                        position,
                        memberArrayList.size - position
                    )
                    Toast.makeText(this, "Reminder deleted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to delete reminder", Toast.LENGTH_SHORT).show()


                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        refreshReminders() // Refresh your list of reminders
    }

    private fun refreshReminders() {
        isDataFetchedInitially = false
        getUserData() // Call your existing method to fetch data
    }
}



