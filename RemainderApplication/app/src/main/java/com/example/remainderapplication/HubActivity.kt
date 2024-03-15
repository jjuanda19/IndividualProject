package com.example.remainderapplication


import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID


const val GEOFENCE_LOCATION_REQUEST_CODE =12345
const val GEOFENCE_RADIUS=100
//const val GEOFENCE_ID= "REMINDER_GEOFENCE_ID"
const val GEOFENCE_EXPIRATION= 10*24*60*60*100
const val GEOFENCE_DWELL_DELAY= 5*60*1000 //5 min



class HubActivity : AppCompatActivity() {
    private lateinit var tvDatePicker: TextView
    private lateinit var btDatePicker: Button
    private lateinit var txtDescription: TextView
    private lateinit var btSavedata: Button
    private lateinit var reff: DatabaseReference
    private var latitude: String = ""
    private var longitude: String = ""
    private lateinit var remainderName: EditText
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var auth: FirebaseAuth


    companion object {
        const val MAP_ACTIVITY_REQUEST_CODE = 100 // A unique request code


        val API: String = "7d69c4543ba6d50c2d5cc7bd6a7669ea"


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hub)




        auth = FirebaseAuth.getInstance()

        tvDatePicker = findViewById(R.id.tvDate)
        btDatePicker = findViewById(R.id.button_date)
        btSavedata = findViewById(R.id.bt_savedata)
        remainderName = findViewById(R.id.RemainderName)
        txtDescription = findViewById(R.id.RemainderDescription)
        geofencingClient = LocationServices.getGeofencingClient(this)


        val myCalendar = Calendar.getInstance()


        btDatePicker.setOnClickListener {
            // First, show the DatePickerDialog
            DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    // Set the chosen date on the calendar
                    myCalendar.set(Calendar.YEAR, year)
                    myCalendar.set(Calendar.MONTH, month)
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    updateLabel(myCalendar)

                },
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }


        val buttonMap = findViewById<Button>(R.id.buttonLocation)
        buttonMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivityForResult(intent, MAP_ACTIVITY_REQUEST_CODE)
        }

        btSavedata.setOnClickListener {
            val selectedDateString = tvDatePicker.text.toString()
            val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val selectedDate = dateFormatter.parse(selectedDateString)

            if (selectedDate != null) {
                if (selectedDate.isToday()) {
                    // Fetch weather data and save reminder immediately
                    fetchWeatherAndSaveReminder(latitude.toDouble(), longitude.toDouble())
                } else {
                    val reminderId = generateReminderId() // You need a method to generate a unique reminder ID.
                    // Schedule weather data fetch for the reminder date
                    scheduleWeatherFetchForReminder(selectedDate, latitude.toDouble(), longitude.toDouble(), reminderId)
                    // Save the reminder with "no data yet" as the weather description
                    saveData("no data yet")
                }
            } else {
                Toast.makeText(this, "Invalid date selected", Toast.LENGTH_SHORT).show()
            }
        }


    }
    fun generateReminderId(): String {
        // Implementation depends on how you manage reminders. It could be a simple UUID.
        return UUID.randomUUID().toString()
    }

    fun Date.isToday(): Boolean {
        val today = Calendar.getInstance()
        val selectedDate = Calendar.getInstance()
        selectedDate.time = this
        return today.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH)
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

    private fun saveData(weatherDescription: String) {
        val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val date = dateFormatter.parse(tvDatePicker.text.toString()) ?: Date()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "No authenticated user found. Please log in.", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val userUid = currentUser.uid // Get the unique user ID from Firebase Auth
        val email = currentUser.email ?: "Unknown" // Handle null email
        val Date = tvDatePicker.text.toString()
        val Name = remainderName.text.toString()
        val Description = txtDescription.text.toString()
        val address = findViewById<TextView>(R.id.textAddress).text.toString()
        val latitudeDouble = latitude.toDoubleOrNull()
        val longitudeDouble = longitude.toDoubleOrNull()
        val setReminderTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val setReminderDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date)
        val actStatus = false
        val remainderDay = SimpleDateFormat("EEE", Locale.ENGLISH).format(date)
            .uppercase(Locale.ENGLISH)
        val remainderSetupDay = SimpleDateFormat("EEE", Locale.ENGLISH).format(date)
            .uppercase(Locale.ENGLISH)




        if (Name.isEmpty() || Date.isEmpty() || address.isEmpty() || latitudeDouble == null || longitudeDouble == null || setReminderTime.isEmpty() || setReminderDate.isEmpty() || remainderDay.isEmpty() || remainderSetupDay.isEmpty()) {
            Toast.makeText(
                this,
                "Please make sure all fields are correctly filled",
                Toast.LENGTH_SHORT
            ).show()
            return // Validate all fields are filled
        }

        val usersRef = FirebaseDatabase.getInstance().getReference("Users")
        val RemID = usersRef.child(userUid).child("Reminders").push().key!!


        val newReminder = Member(
            RemID,
            Name,
            address,
            longitudeDouble,
            latitudeDouble,
            Date,
            setReminderTime,
            setReminderDate,
            actStatus,
            remainderDay,
            remainderSetupDay,
            Description,
            weatherDescription



            )
        usersRef.child(userUid).child("email").setValue(email) // Always set/update the email
        usersRef.child(userUid).child("Reminders").child(RemID).setValue(newReminder)
            .addOnSuccessListener {
                Toast.makeText(
                    this@HubActivity,
                    "Reminder saved successfully",
                    Toast.LENGTH_SHORT
                ).show()
                createGeofence(RemID, latitudeDouble, longitudeDouble, geofencingClient)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@HubActivity,
                    "Failed to save reminder: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun updateLabel(myCalendar: Calendar) {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.UK)
        tvDatePicker.text = sdf.format(myCalendar.time)

    }

    private fun createGeofence(
        RemID: String,
        latitudeDouble: Double,
        longitudeDouble: Double,
        geofencingClient: GeofencingClient
    ) {


        val geofence = Geofence.Builder()
            .setRequestId(RemID)
            .setCircularRegion(latitudeDouble, longitudeDouble, GEOFENCE_RADIUS.toFloat())
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

        val intent = Intent(this, GeofenceReceiver::class.java).apply {

            putExtra("UserID", FirebaseAuth.getInstance().currentUser?.uid)
            putExtra("RemID", RemID)
        }

        val uniqueRequestCode = (FirebaseAuth.getInstance().currentUser?.uid + RemID).hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            uniqueRequestCode, intent, PendingIntent.FLAG_MUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    GEOFENCE_LOCATION_REQUEST_CODE
                )
            } else {
                geofencingClient.addGeofences(geofenceRequest, pendingIntent)
                Log.d("HubActivity", "Geofence added with ID: $RemID")

            }
        } else {
            geofencingClient.addGeofences(geofenceRequest, pendingIntent)

        }


    }



    private fun fetchWeatherAndSaveReminder(latitudeDouble: Double, longitudeDouble: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Modify the API request URL to use latitude and longitude
                val apiResponse = URL("https://api.openweathermap.org/data/2.5/weather?lat=$latitudeDouble&lon=$longitudeDouble&units=metric&appid=$API").readText(Charsets.UTF_8)
                val weatherDescription = parseWeatherDescription(apiResponse)
                withContext(Dispatchers.Main) {
                    // Now proceed to save the data including the weather description
                    // Assuming saveData now accepts the weather description as a parameter
                    saveData(weatherDescription)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Failed to fetch weather data: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun parseWeatherDescription(apiResponse: String): String {
        val jsonObj = JSONObject(apiResponse)
        val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
        return weather.getString("description")
    }

    private fun scheduleWeatherFetchForReminder(date: Date, latitude: Double, longitude: Double, reminderId: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, WeatherFetchReceiver::class.java).apply {
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
            putExtra("reminderId", reminderId)
        }
        val pendingIntent = PendingIntent.getBroadcast(this, reminderId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, date.time, pendingIntent)
    }


}
























