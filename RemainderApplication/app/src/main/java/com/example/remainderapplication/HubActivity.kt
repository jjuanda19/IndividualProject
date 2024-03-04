package com.example.remainderapplication


import android.app.DatePickerDialog
import android.app.PendingIntent

import android.content.Intent
import android.content.pm.PackageManager

import android.os.Build
import android.os.Bundle

import android.widget.Button
import android.widget.EditText
import android.Manifest
import android.util.Log

import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase




const val GEOFENCE_LOCATION_REQUEST_CODE =12345
const val GEOFENCE_RADIUS=100
//const val GEOFENCE_ID= "REMINDER_GEOFENCE_ID"
const val GEOFENCE_EXPIRATION= 10*24*60*60*100
const val GEOFENCE_DWELL_DELAY= 10*1000 //10 sec

class HubActivity : AppCompatActivity() {
    private lateinit var tvDatePicker: TextView
    private lateinit var btDatePicker: Button
    private lateinit var btSavedata: Button
    private lateinit var reff: DatabaseReference
    private var latitude: String = ""
    private var longitude: String = ""
    private lateinit var remainderName: EditText
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var auth: FirebaseAuth



    companion object {
        const val MAP_ACTIVITY_REQUEST_CODE = 100 // A unique request code
        const val ACTION_GEOFENCE_EVENT = "com.example.remainderapplication.ACTION_GEOFENCE_EVENT"



    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hub)
        auth = FirebaseAuth.getInstance()

        tvDatePicker = findViewById(R.id.tvDate)
        btDatePicker = findViewById(R.id.button_date)
        btSavedata = findViewById(R.id.bt_savedata)
        remainderName = findViewById(R.id.RemainderName)
        geofencingClient = LocationServices.getGeofencingClient(this)


        val myCalendar = Calendar.getInstance()

        val datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLable(myCalendar)
        }

        btDatePicker.setOnClickListener {
            DatePickerDialog(
                this, datePicker, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()

        }


        val buttonMap = findViewById<Button>(R.id.buttonLocation)
        buttonMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivityForResult(intent, MAP_ACTIVITY_REQUEST_CODE)
        }

        btSavedata.setOnClickListener {
            saveData()


            //Toast.makeText(this,"Remainder Saved",Toast.LENGTH_SHORT).show()
            //finish()
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MAP_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.getStringExtra("EXTRA_ADDRESS")?.let { receivedData ->
                // Update UI with received data, for example:
                val textAddressView = findViewById<TextView>(R.id.textAddress)
                textAddressView.text = receivedData
            }
            latitude = data?.getStringExtra("EXTRA_LATITUDE") ?: ""
            longitude = data?.getStringExtra("EXTRA_LONGITUDE") ?: ""
        }
    }

    private fun saveData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "No authenticated user found. Please log in.", Toast.LENGTH_SHORT).show()
            return
        }

        val userUid = currentUser.uid // Get the unique user ID from Firebase Auth
        val email = currentUser.email ?: "Unknown" // Handle null email
        val Date = tvDatePicker.text.toString()
        val Name = remainderName.text.toString()
        val address = findViewById<TextView>(R.id.textAddress).text.toString()
        val latitudeDouble = latitude.toDoubleOrNull()
        val longitudeDouble = longitude.toDoubleOrNull()

        if (Name.isEmpty() || Date.isEmpty() || address.isEmpty() || latitudeDouble == null || longitudeDouble == null) {
            Toast.makeText(this, "Please make sure all fields are correctly filled", Toast.LENGTH_SHORT).show()
            return // Validate all fields are filled
        }

        val usersRef = FirebaseDatabase.getInstance().getReference("Users")
        val RemID = usersRef.child(userUid).child("Reminders").push().key!!


        val newReminder = Member(RemID, Name, address, longitudeDouble, latitudeDouble, Date)

        usersRef.child(userUid).child("email").setValue(email) // Always set/update the email
        usersRef.child(userUid).child("Reminders").child(RemID).setValue(newReminder)
            .addOnSuccessListener {
                Toast.makeText(this@HubActivity, "Reminder saved successfully", Toast.LENGTH_SHORT).show()
                createGeofence(RemID, latitudeDouble, longitudeDouble, geofencingClient)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@HubActivity, "Failed to save reminder: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateLable(myCalendar: Calendar) {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.UK)
        tvDatePicker.text =sdf.format(myCalendar.time)

    }
    private fun createGeofence(RemID: String, latitudeDouble: Double, longitudeDouble: Double,geofencingClient: GeofencingClient ) {
        val geofence = Geofence.Builder()
            .setRequestId(RemID)
            .setCircularRegion(latitudeDouble,longitudeDouble, GEOFENCE_RADIUS.toFloat())
            .setExpirationDuration(GEOFENCE_EXPIRATION.toLong())
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL or
                        Geofence.GEOFENCE_TRANSITION_EXIT
            ).setLoiteringDelay(GEOFENCE_DWELL_DELAY)
            .build()

        val geofenceRequest = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
        }.build()

        val intent = Intent(this,GeofenceReceiver::class.java).apply {

            putExtra("UserID", FirebaseAuth.getInstance().currentUser?.uid)
            putExtra("RemID", RemID)
        }

        val uniqueRequestCode=(FirebaseAuth.getInstance().currentUser?.uid + RemID).hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            uniqueRequestCode,intent,PendingIntent.FLAG_MUTABLE
        )
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            if(ContextCompat.checkSelfPermission(
                applicationContext,Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    GEOFENCE_LOCATION_REQUEST_CODE
                )
            }else{
                geofencingClient.addGeofences(geofenceRequest,pendingIntent)
                Log.d("HubActivity", "Geofence added with ID: $RemID")

            }
        }else{
            geofencingClient.addGeofences(geofenceRequest,pendingIntent)

        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == GEOFENCE_LOCATION_REQUEST_CODE){
            if(permissions.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(
                    this,"Need back ground location to be enabled",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    }


}




