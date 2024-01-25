package com.example.remainderapplication


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HubActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hub)

       val buttonMap=findViewById<Button>(R.id.buttonLocation)
        buttonMap.setOnClickListener{
            val intent=Intent(this,MapActivity::class.java)
        startActivity(intent)
        }
    }
}