package com.example.remainderapplication


import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
const val GEOFENCE_RADIUS=300
const val GEOFENCE_EXPIRATION= 7*24*60*60*1000 // 7 days
const val GEOFENCE_DWELL_DELAY= 300000 // 5 minutes



// HubActivity extends AppCompatActivity to utilize Android lifecycle methods.
class HubActivity : AppCompatActivity() {
    // Variable declarations for UI elements and Firebase Auth.
    private lateinit var tvDatePicker: TextView // TextView to display the selected date.
    private lateinit var btDatePicker: Button // Button to trigger the date picker dialog.
    private lateinit var txtDescription: TextView // TextView for the reminder's description.
    private lateinit var btSavedata: Button // Button to save the reminder data.
    private lateinit var reff: DatabaseReference // Reference to the Firebase database.
    private var latitude: String = "" // String to store latitude of the reminder location.
    private var longitude: String = "" // String to store longitude of the reminder location.
    private lateinit var remainderName: EditText // EditText for the name of the reminder.
    private lateinit var geofencingClient: GeofencingClient // Client for handling geofencing tasks.
    private lateinit var auth: FirebaseAuth // Firebase Authentication instance.

    // Companion object to hold constant values and static variables.
    companion object {
        const val MAP_ACTIVITY_REQUEST_CODE =
            100 // Unique request code for starting the MapActivity.


        val API: String = "YOUR_API_KEY" // OpenWeather API key


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hub)


        // Initialization of variables.
        auth = FirebaseAuth.getInstance() // Getting an instance of FirebaseAuth.
        tvDatePicker = findViewById(R.id.tvDate) // Initializing the TextView for date display.
        btDatePicker = findViewById(R.id.button_date) // Initializing the date picker button.
        btSavedata = findViewById(R.id.bt_savedata) // Initializing the save data button.
        remainderName = findViewById(R.id.RemainderName) // Initializing the reminder name EditText.
        txtDescription =
            findViewById(R.id.RemainderDescription) // Initializing the description TextView.
        geofencingClient =
            LocationServices.getGeofencingClient(this) // Initializing the geofencing client.

        // Calendar instance for handling date operations.
        val myCalendar = Calendar.getInstance()

        // Setting up a DatePickerDialog to select a date for the reminder.
        btDatePicker.setOnClickListener {
            // First, show the DatePickerDialog
            DatePickerDialog(
                this,
                R.style.DatePickerDialogTheme,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    // Set the chosen date on the calendar
                    myCalendar.set(Calendar.YEAR, year)
                    myCalendar.set(Calendar.MONTH, month)
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    updateLabel(myCalendar)// Update the TextView with the selected date.

                },
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Intent to open MapActivity for selecting a location for the reminder.
        val buttonMap = findViewById<Button>(R.id.buttonLocation)
        buttonMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivityForResult(intent, MAP_ACTIVITY_REQUEST_CODE)
        }

        // OnClickListener for the save data button.
        btSavedata.setOnClickListener {
            val selectedDateString = tvDatePicker.text.toString()
            val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val selectedDate = dateFormatter.parse(selectedDateString)

            if (selectedDate != null) {
                if (selectedDate.isToday()) {
                    // Fetch weather data and save reminder immediately
                    fetchWeatherAndSaveReminder(latitude.toDouble(), longitude.toDouble())
                } else {
                    val reminderId =
                        generateReminderId() // You need a method to generate a unique reminder ID.
                    // Schedule weather data fetch for the reminder date
                    scheduleWeatherFetchForReminder(
                        selectedDate,
                        latitude.toDouble(),
                        longitude.toDouble(),
                        reminderId
                    )
                    // Save the reminder with "no data yet" as the weather description
                    saveData("no data yet")
                }
            } else {
                Toast.makeText(this, "Invalid date selected", Toast.LENGTH_SHORT).show()
            }
        }


    }

    // Generates a unique ID for each reminder.
    fun generateReminderId(): String {
        // Implementation depends on how you manage reminders. It could be a simple UUID.
        return UUID.randomUUID().toString()
    }

    // Checks if a given Date object represents today's date.
    fun Date.isToday(): Boolean {
        val today = Calendar.getInstance()
        val selectedDate = Calendar.getInstance()
        selectedDate.time = this
        return today.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH)
    }

    // Handles the result from MapActivity.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MAP_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            // Extracting data from the result Intent.
            data?.getStringExtra("EXTRA_ADDRESS")?.let { receivedData ->
                // Update UI with received data, for example:
                val textAddressView = findViewById<TextView>(R.id.textAddress)
                textAddressView.text = receivedData
            }
            latitude = data?.getStringExtra("EXTRA_LATITUDE") ?: ""
            longitude = data?.getStringExtra("EXTRA_LONGITUDE") ?: ""
        }
    }

    // Method to save the data of the reminder including weather information if available.
    private fun saveData(weatherDescription: String) {
        // Checking for a currently authenticated user.
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "No authenticated user found. Please log in.", Toast.LENGTH_SHORT)
                .show()
            return
        }
        // Formatting the selected date and preparing data for saving
        val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val date = dateFormatter.parse(tvDatePicker.text.toString()) ?: Date()

        // Preparing data for saving.
        val userUid = currentUser.uid // User's unique ID.
        val email = currentUser.email ?: "Unknown" // User's email, defaulting to "Unknown" if null.
        val Date = tvDatePicker.text.toString() // The selected date for the reminder.
        val Name = remainderName.text.toString() // The entered name for the reminder.
        val Description =
            txtDescription.text.toString() // The entered description for the reminder.
        val address =
            findViewById<TextView>(R.id.textAddress).text.toString() // The selected address for the reminder.
        val latitudeDouble = latitude.toDoubleOrNull() // Converted latitude string to Double.
        val longitudeDouble = longitude.toDoubleOrNull() // Converted longitude string to Double.
        val setReminderTime = SimpleDateFormat(
            "HH:mm",
            Locale.getDefault()
        ).format(Date()) // Current time for setting the reminder.
        val setReminderDate = SimpleDateFormat(
            "dd-MM-yyyy",
            Locale.getDefault()
        ).format(date) // Formatted date for setting the reminder.
        val actStatus =
            false // Activity status, can be used to mark if the reminder is active or not.
        val remainderDay = SimpleDateFormat("EEE", Locale.ENGLISH).format(date)
            .uppercase(Locale.ENGLISH) // Day of the week for the reminder.
        val remainderSetupDay = SimpleDateFormat("EEE", Locale.ENGLISH).format(Date())
            .uppercase(Locale.ENGLISH) // Day of the week when the reminder was set.




        if (Name.isEmpty() || Date.isEmpty() || address.isEmpty() || latitudeDouble == null || longitudeDouble == null || setReminderTime.isEmpty() || setReminderDate.isEmpty() || remainderDay.isEmpty() || remainderSetupDay.isEmpty()) {
            Toast.makeText(
                this,
                "Please make sure all fields are correctly filled",
                Toast.LENGTH_SHORT
            ).show()
            return // Validate all fields are filled
        }

        // Get a reference to the 'Users' node in Firebase database
        val usersRef = FirebaseDatabase.getInstance().getReference("Users")

        // Generate a unique ID for a new reminder under the current user's 'Reminders' node
        val RemID = usersRef.child(userUid).child("Reminders").push().key!!

        // Create a new reminder object with the collected data
        val newReminder = Member(
            RemID, // The unique reminder ID
            Name, // Name of the reminder
            address, // Address related to the reminder
            longitudeDouble, // Longitude for the reminder's location
            latitudeDouble, // Latitude for the reminder's location
            Date, // Selected date for the reminder
            setReminderTime, // Time when the reminder should trigger
            setReminderDate, // Date when the reminder should trigger
            actStatus, // Activation status of the reminder (e.g., active/inactive)
            remainderDay, // The day of the week for the reminder
            remainderSetupDay, // The day the reminder was set up
            Description, // Description of the reminder
            weatherDescription // Weather description at the time of creating the reminder
        )

        // Update the user's email in the Firebase database (ensures the latest email is always stored)
        usersRef.child(userUid).child("email").setValue(email)

        // Save the new reminder to the database under the generated unique ID
        usersRef.child(userUid).child("Reminders").child(RemID).setValue(newReminder)
            .addOnSuccessListener {
                // Display a success message upon successful saving
                Toast.makeText(
                    this@HubActivity,
                    "Reminder saved successfully",
                    Toast.LENGTH_SHORT
                ).show()

                // Create a geofence for the reminder, allowing for location-based notifications
                createGeofence(RemID, latitudeDouble, longitudeDouble, geofencingClient)

                // Finish the current activity, possibly returning to a previous screen
                finish()
            }
            .addOnFailureListener { e ->
                // Display an error message if saving the reminder fails
                Toast.makeText(
                    this@HubActivity,
                    "Failed to save reminder: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }


    // Function to update the date display in the TextView after a date has been selected.
    private fun updateLabel(myCalendar: Calendar) {
        // Define the date format as day-month-year, which is a common format in the UK.
        val myFormat = "dd-MM-yyyy"
        // Create a SimpleDateFormat object for formatting the date, specifying the desired format and locale.
        // The Locale.UK parameter ensures that the date is formatted according to UK conventions, such as the order of day and month.
        val sdf = SimpleDateFormat(myFormat, Locale.UK)
        // Use the SimpleDateFormat object to format the Calendar object's time (the selected date) into a string.
        // Then, set this string as the text of the tvDatePicker TextView, displaying the formatted date to the user.
        tvDatePicker.text = sdf.format(myCalendar.time)
    }


    // Function to create and register a geofence using provided parameters.
    private fun createGeofence(
        RemID: String, // Unique identifier for the reminder.
        latitudeDouble: Double, // Latitude for the geofence's center.
        longitudeDouble: Double, // Longitude for the geofence's center.
        geofencingClient: GeofencingClient // Client used to interact with the Geofencing API.
    ) {

        // Configure the geofence parameters.
        val geofence = Geofence.Builder()
            .setRequestId(RemID) // Set the ID to uniquely identify this geofence.
            .setCircularRegion(
                latitudeDouble, longitudeDouble, GEOFENCE_RADIUS.toFloat()
            ) // Define the geofence's location and radius.
            .setExpirationDuration(GEOFENCE_EXPIRATION.toLong()) // Set how long the geofence should be active.
            .setTransitionTypes( // Specify the types of geofence transitions to monitor.
                Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL or
                        Geofence.GEOFENCE_TRANSITION_EXIT
            ).setLoiteringDelay(GEOFENCE_DWELL_DELAY) // Set the delay for the dwell transition.
            .build()

        // Prepare the geofence request, setting the initial trigger to "enter".
        val geofenceRequest = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence) // Add the configured geofence.
        }.build()

        // Create an Intent to handle geofence transitions.
        val intent = Intent(this, GeofenceReceiver::class.java).apply {
            putExtra("UserID", FirebaseAuth.getInstance().currentUser?.uid) // Pass current user ID.
            putExtra("RemID", RemID) // Pass the reminder ID.
        }

        // Generate a unique request code for the PendingIntent.
        val uniqueRequestCode = (FirebaseAuth.getInstance().currentUser?.uid + RemID).hashCode()
        // Create a PendingIntent that triggers when the geofence transition occurs.
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            uniqueRequestCode, intent, PendingIntent.FLAG_MUTABLE
        )

        // Check and request necessary permissions based on Android version.
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            // For Android Q, check for background location access permission.
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request permission if not already granted.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    GEOFENCE_LOCATION_REQUEST_CODE
                )
            } else {
                // If permission is granted, add the geofence.
                geofencingClient.addGeofences(geofenceRequest, pendingIntent)
                Log.d("HubActivity", "Geofence added with ID: $RemID")
            }
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            // For versions higher than Android Q, handle background location permission differently.
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Show dialog to direct user to settings for enabling background location.
                AlertDialog.Builder(this, R.style.DatePickerDialogTheme)
                    .setTitle("Background Location Permission")
                    .setMessage("This app needs background location, please enable location to allow all time.")
                    .setPositiveButton("OK") { _, _ ->
                        // Direct user to app settings.
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show()
            }
        } else {
            // For versions before Android Q, directly add the geofence if permissions are in place.
            geofencingClient.addGeofences(geofenceRequest, pendingIntent)
        }
    }


    private fun fetchWeatherAndSaveReminder(latitudeDouble: Double, longitudeDouble: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Modify the API request URL to use latitude and longitude
                val apiResponse =
                    URL("https://api.openweathermap.org/data/2.5/weather?lat=$latitudeDouble&lon=$longitudeDouble&units=metric&appid=$API")
                        .readText(Charsets.UTF_8)
                val weatherDescription = parseWeatherDescription(apiResponse)
                withContext(Dispatchers.Main) {
                    // Now proceed to save the data including the weather description
                    saveData(weatherDescription)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "Failed to fetch weather data: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // Parses the "description" field from the weather information in the API response.
    private fun parseWeatherDescription(apiResponse: String): String {
        // Converts the JSON string response into a JSONObject.
        val jsonObj = JSONObject(apiResponse)
        // Retrieves the "weather" array from the JSONObject. This array contains weather information including the description.
        val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
        // Extracts and returns the "description" value from the first object in the "weather" array.
        return weather.getString("description")
    }


    // Schedules a weather data fetch operation for a specific reminder.
    // Schedules a weather data fetch operation for a specific reminder.
    private fun scheduleWeatherFetchForReminder(date: Date, latitude: Double, longitude: Double, reminderId: String) {
        // Gets the AlarmManager system service to schedule alarms.
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Creates an explicit intent for the WeatherFetchReceiver, which is a BroadcastReceiver that handles the weather data fetching.
        val intent = Intent(this, WeatherFetchReceiver::class.java).apply {
            // Adds extra information to the intent, including the reminder's latitude, longitude, and ID.
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
            putExtra("reminderId", reminderId)
        }
        // Creates a PendingIntent that wraps the intent. This PendingIntent can be used by the AlarmManager to broadcast the intent at the scheduled time.
        // The reminderId.hashCode() is used to ensure a unique PendingIntent identifier for each reminder.
        val pendingIntent = PendingIntent.getBroadcast(this, reminderId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        // Schedules an exact alarm to wake up the device and broadcast the intent at the reminder's date and time.
        // RTC_WAKEUP means the alarm is based on the real-time clock and will wake up the device if it's asleep.
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, date.time, pendingIntent)
    }

}
























