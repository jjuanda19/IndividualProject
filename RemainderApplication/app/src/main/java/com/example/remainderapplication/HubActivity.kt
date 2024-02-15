package com.example.remainderapplication


import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText

import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.remainderapplication.R.id.textAddress
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class HubActivity : AppCompatActivity() {
    private lateinit var  tvDatePicker: TextView
    private lateinit var  btDatePicker:  Button
    private lateinit var  btSavedata: Button
    private lateinit var reff: DatabaseReference
    private var latitude: String = ""
    private var longitude: String = ""
    private lateinit var remainderName: EditText

    companion object {
        const val MAP_ACTIVITY_REQUEST_CODE = 100 // A unique request code
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hub)

        tvDatePicker=findViewById(R.id.tvDate)
        btDatePicker=findViewById(R.id.button_date)
        btSavedata=findViewById(R.id.bt_savedata)
        remainderName=findViewById(R.id.RemainderName)
        reff=FirebaseDatabase.getInstance().getReference().child("Member")


        val myCalendar = Calendar.getInstance()

        val datePicker = DatePickerDialog.OnDateSetListener{ view, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR,year)
            myCalendar.set(Calendar.MONTH,month)
            myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateLable(myCalendar)
        }

        btDatePicker.setOnClickListener {
            DatePickerDialog(this,datePicker,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show()

        }




       val buttonMap=findViewById<Button>(R.id.buttonLocation)
        buttonMap.setOnClickListener{
            val intent=Intent(this,MapActivity::class.java)
            startActivityForResult(intent, MAP_ACTIVITY_REQUEST_CODE)
        }

        btSavedata.setOnClickListener{
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
        val Date = tvDatePicker.text.toString()
        val Name = remainderName.text.toString()
        val address =
            findViewById<TextView>(R.id.textAddress).text.toString() // Assuming you stored this value
        latitude
        longitude

        if (Name.isEmpty()) {
            remainderName.error = "Please select a name for the Remainder"
            return // Stop execution if validation fails
        }
        if (Date.isEmpty()) {
            tvDatePicker.error = "Please select a date"
            return
        }
        if (address.isEmpty()) {
            Toast.makeText(this, "Please select an address", Toast.LENGTH_SHORT).show()
            return // Stop execution if validation fails
        }

        val RemID = reff.push().key!!

        val Member = Member(RemID, Name, address, longitude, latitude, Date)

        reff.child(RemID).setValue(Member)
            .addOnCompleteListener {
                Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener { err ->
                Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_SHORT).show()

            }

    }
    // Override onActivityResult to handle the result from MapActivity


    private fun updateLable(myCalendar: Calendar) {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.UK)
        tvDatePicker.setText(sdf.format(myCalendar.time))

    }
}