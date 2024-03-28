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

// Defines a class for the sign-in activity in an Android application. It extends AppCompatActivity to inherit common activity behavior.
class SignInActivity : AppCompatActivity() {

    // Declaration of UI components as lateinit variables, indicating they will be initialized later.
    private lateinit var txtSignIn : TextView
    private lateinit var txtEmail : EditText
    private lateinit var txtPassword: EditText
    private lateinit var txtLink: TextView
    private lateinit var buttonSignIn : Button
    // Declaration of the FirebaseAuth instance to handle authentication operations.
    private lateinit var auth: FirebaseAuth

    // The onCreate method is called when the activity is starting.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the user interface layout for this Activity.
        setContentView(R.layout.activity_sing_in)

        // Initializing UI components by finding them in the layout.
        txtSignIn = findViewById(R.id.textViewSingIn)
        txtEmail = findViewById(R.id.editTextSingInEmail)
        txtPassword = findViewById(R.id.editTextSingInPassword)
        txtLink = findViewById(R.id.txtlinkSingUp)
        buttonSignIn = findViewById(R.id.button_sing_in)
        // Getting an instance of FirebaseAuth.
        auth = FirebaseAuth.getInstance()

        // Creating a SpannableString to underline the 'Sign Up' link text.
        val content = SpannableString(txtLink.text.toString())
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        txtLink.text = content
        // Setting an onClickListener on the 'Sign Up' TextView to navigate to the SignUpActivity.
        txtLink.setOnClickListener {
            intent = Intent(this, SingUpActivity::class.java)
            startActivity(intent)
        }
        // Setting an onClickListener on the sign-in button to call the signIn method when clicked.
        buttonSignIn.setOnClickListener {
            signIn()
        }
    }

    // A private method to handle the sign-in operation.
    private fun signIn() {
        // Extracting text from the EditText fields and trimming to remove any leading or trailing whitespace.
        val email: String = txtEmail.text.toString().trim()
        val pass: String = txtPassword.text.toString().trim()

        // Checking if the email field is empty and displaying an error if it is.
        if (email.isEmpty()) {
            txtEmail.error = "Email cannot be empty"
            return // Exiting the method to prevent further execution.
        }
        // Checking if the password field is empty and displaying an error if it is.
        if (pass.isEmpty()) {
            txtPassword.error = "Password cannot be empty"
            return // Exiting the method to prevent further execution.
        }
        // Using FirebaseAuth to sign in with email and password.
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
            if (it.isSuccessful) {
                // If sign in is successful, navigate to the MainActivity and clear the activity stack.
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // If sign in fails, display the exception message as a toast.
                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
