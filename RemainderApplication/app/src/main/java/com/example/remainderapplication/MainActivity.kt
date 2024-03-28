package com.example.remainderapplication

import android.Manifest
import android.app.AlertDialog

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.provider.Settings
import android.view.View


// MainActivity extends AppCompatActivity and implements the OnItemDeleteListener interface from the Adapter class.
class MainActivity : AppCompatActivity(), Adapter.OnItemDeleteListener {

    private lateinit var dbref: DatabaseReference // Reference to the Firebase database.
    private lateinit var memberRecyclerView: RecyclerView // RecyclerView for displaying the list of members (reminders).
    private lateinit var memberArrayList: ArrayList<Member> // ArrayList to hold the members (reminders).
    private lateinit var noRemindersTextView: TextView // TextView to show when there are no reminders.

    // Variables for managing permission requests using the new ActivityResultLauncher.
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    // Permissions flags to keep track of which permissions have been granted.
    private var isCoarseLocationPermissionGranted = false
    private var isFineLocationPermissionGranted = false
    private var isBackgroundLocationPermissionGranted = false
    private var isInternetPermissionGranted = false
    private var isWakeLockPermissionGranted = false
    private var isReadExternalStoragePermissionGranted = false
    private var isWriteExternalStoragePermissionGranted = false
    private var isManageExternalStoragePermissionGranted = false
    private var isNetworkPermissionGranted = false

    private lateinit var singouttext: TextView // TextView for the sign-out option.
    private var isDataFetchedInitially = false // Flag to track if data has been initially fetched.


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialization of variables and setting up the RecyclerView.
        memberArrayList = arrayListOf<Member>()
        noRemindersTextView = findViewById(R.id.NoRemainderText)

        memberRecyclerView = findViewById(R.id.RemList)
        memberRecyclerView.layoutManager = LinearLayoutManager(this)
        memberRecyclerView.setHasFixedSize(true)
        memberRecyclerView.adapter = Adapter(memberArrayList, this)

        // Fetch user data to populate the RecyclerView.
        getUserData()

        // Setting up the sign-out TextView.
        singouttext = findViewById(R.id.textSingOut)

        val content = SpannableString(singouttext.text.toString())
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        singouttext.text = content
        singouttext.setOnClickListener {
            // Navigates to the SignInActivity when the sign-out text is clicked.
            intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Initialize and request permissions.
        initPermissionLauncher()
        requestPermission()

        // Button to add a new reminder.
        val buttonAddReminder = findViewById<Button>(R.id.buttonAddReminder)
        buttonAddReminder.setOnClickListener {
            // Navigates to the HubActivity when the add reminder button is clicked.
            val intent = Intent(this, HubActivity::class.java)
            startActivity(intent)
        }

    }

    // Initializes the permission launcher which is a part of the new Android permissions
    private fun initPermissionLauncher(){
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                // Update permission flags based on user response.
                // Proceed with app functionality if permissions are granted, otherwise show a dialog to retry or exit.
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
                if (permissions.values.any { !it }) {
                    // Not all permissions were granted, show rationale and request again
                    AlertDialog.Builder(this, R.style.DatePickerDialogTheme)
                        .setTitle("Permissions required")
                        .setMessage("All requested permissions are necessary for the app to function properly. Please grant them.")
                        .setPositiveButton("Retry") { _, _ ->
                            requestPermission() // Request permissions again
                        }
                        .setNegativeButton("Exit") { _, _ ->
                            Toast.makeText(this, "The app cannot function without the required permissions.", Toast.LENGTH_LONG).show()
                            finish() // Close app or navigate the user to a non-functional part of your app
                        }
                        .create()
                        .show()
                } else {
                    // All permissions were granted, you can proceed with your app functionality
                    // TODO: Add your app's functionality here
                }
            }




    }

    // Requests the necessary permissions for the app to function.
        private fun requestPermission() {
        // Builds the list of permissions to request and checks if each permission has been granted.
        // If necessary, launches the permission request.
            val permissionRequest: MutableList<String> = ArrayList()

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
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED


            // Check if we need to request background location permission.
            val backgroundLocationRequired = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    !isBackgroundLocationPermissionGranted

            if (backgroundLocationRequired) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // For Android 11 and above, direct the user to settings as background permission request
                    // cannot be combined with other permissions
                    AlertDialog.Builder(this, R.style.DatePickerDialogTheme)
                        .setTitle("Background Location Permission")
                        .setMessage("This app needs background location, please enable location to allow all time.")
                        .setPositiveButton("OK") { _, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show()
                } else if( Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    // For Android 10, request the background location permission directly
                    permissionRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            }

            // Common permissions for all Android versions
            if (!isCoarseLocationPermissionGranted) {
                permissionRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)

            }
            if (!isFineLocationPermissionGranted) {
                permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !isManageExternalStoragePermissionGranted) {
                // For Android 11 and above, manage external storage permission is a special case
                permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && !isManageExternalStoragePermissionGranted) {
                permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (!isNetworkPermissionGranted) {
                permissionRequest.add(Manifest.permission.ACCESS_NETWORK_STATE)
            }

            if (permissionRequest.isNotEmpty()) {
                permissionLauncher.launch(permissionRequest.toTypedArray())
            }
        }

    // Fetches user data from Firebase and updates the RecyclerView.
    private fun getUserData() {
        // Implementation to fetch user data from Firebase.
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
                        toggleNoRemindersTextView()
                        isDataFetchedInitially = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Failed to fetch members: ${error.message}")
            }
        })
    }
    // Toggles the visibility of the noRemindersTextView based on whether there are reminders to show.
    private fun toggleNoRemindersTextView() {
        noRemindersTextView.visibility = if (memberArrayList.isEmpty()) View.VISIBLE else View.GONE
    }

    // Handles the deletion of a reminder when the delete icon is clicked in the RecyclerView.
    override fun onDeleteClick(position: Int) {
        // Get the ID of the reminder you want to delete
        val reminderToDelete = memberArrayList[position]

        // Assuming each Member has a unique ID field
        val reminderId = reminderToDelete.RemID

        deleteReminderFromFirebase(reminderId, position)
    }

    // Implementation to delete a reminder from Firebase.
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
                    toggleNoRemindersTextView()
                    Toast.makeText(this, "Reminder deleted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to delete reminder", Toast.LENGTH_SHORT).show()


                }
            }
        }
    }

    // Refreshes the reminders list when the activity resumes.
    override fun onResume() {
        super.onResume()
        refreshReminders()
    }

    private fun refreshReminders() {
        isDataFetchedInitially = false
        getUserData() // Call your existing method to fetch data
    }
}



