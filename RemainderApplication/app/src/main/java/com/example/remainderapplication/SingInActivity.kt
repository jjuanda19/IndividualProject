package com.example.remainderapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class SingInActivity : AppCompatActivity() {
    private lateinit var txtSingIn : TextView
    private lateinit var txtemail : EditText
    private lateinit var txtpassword: EditText
    private lateinit var txtlink: TextView
    private lateinit var buttonSingIn : Button
    private lateinit var auth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sing_in)

        txtSingIn=findViewById(R.id.textViewSingIn)
        txtemail= findViewById(R.id.editTextSingInEmail)
        txtpassword=findViewById(R.id.editTextSingInPassword)
        txtlink=findViewById(R.id.txtlinkSingUp)
        buttonSingIn=findViewById(R.id.button_sing_up)
        auth = FirebaseAuth.getInstance()

        val content = SpannableString(txtlink.text.toString())
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        txtlink.text = content
        txtlink.setOnClickListener{
            intent= Intent(this,SingUpActivity::class.java)
            startActivity(intent)
        }
        buttonSingIn.setOnClickListener{
            singIn()


        }
    }

    private fun singIn(){
        val email: String = txtemail.text.toString().trim()
        val pass: String = txtpassword.text.toString().trim()

        // Check if the email field is empty
        if (email.isEmpty()) {
            txtemail.error = "Email cannot be empty"
            return // Prevent further execution
        }
        if (pass.isEmpty()) {
            txtpassword.error = "Password cannot be empty"
            return // Prevent further execution
        }
        auth.signInWithEmailAndPassword(email,pass).addOnCompleteListener {
            if(it.isSuccessful){
                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
                finish()
            }else
            Toast.makeText(this,it.exception.toString(),Toast.LENGTH_SHORT).show()
        }
    }

}