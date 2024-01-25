package com.example.remainderapplication


import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.remainderapplication.R.id.textAddress
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HubActivity : AppCompatActivity() {
    private lateinit var  tvDatePicker: TextView
    private lateinit var  btDatePicker:  Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hub)

        tvDatePicker=findViewById(R.id.tvDate)
        btDatePicker=findViewById(R.id.button_date)

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


        //Get data from another activity
       val textAddress = findViewById<TextView>(textAddress)
        val receivedAddress:String = intent.extras?.getString("EXTRA_LATITUDE").orEmpty()
        textAddress.text = receivedAddress

       val buttonMap=findViewById<Button>(R.id.buttonLocation)
        buttonMap.setOnClickListener{
            val intent=Intent(this,MapActivity::class.java)
        startActivity(intent)
        }



    }

    private fun updateLable(myCalendar: Calendar) {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.UK)
        tvDatePicker.setText(sdf.format(myCalendar.time))

    }
}