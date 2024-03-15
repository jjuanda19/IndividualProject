package com.example.remainderapplication


import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class SingUpActivity : AppCompatActivity() {
    private lateinit var txtSingUp: TextView
    private lateinit var txtEmail: EditText
    private lateinit var textPassword: EditText
    private lateinit var textRetypePassword: EditText
    private lateinit var buttonSingUp: Button
    private lateinit var txtLinkSingUp: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sing_up)

        auth = FirebaseAuth.getInstance()
        txtSingUp = findViewById(R.id.textViewSingUp)
        txtEmail = findViewById(R.id.editTextEmail)
        textPassword = findViewById(R.id.editTextTextPassword)
        textRetypePassword = findViewById(R.id.editTextTextRetypePassword)
        buttonSingUp = findViewById(R.id.button_sing_in)
        txtLinkSingUp = findViewById(R.id.txtlinkSingUp)


        buttonSingUp.setOnClickListener {
            // Directly call createNewAccount without setting another listener
            createNewAccount()
        }
        val content = SpannableString(txtLinkSingUp.text.toString())
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        txtLinkSingUp.text = content
        txtLinkSingUp.setOnClickListener{
            intent =Intent(this,SingInActivity::class.java)
            startActivity(intent)
            finish()
        }


    }

    private fun createNewAccount() {
        val user: String = txtEmail.text.toString().trim()
        val pass: String = textPassword.text.toString().trim()
        val retypePass: String = textRetypePassword.text.toString().trim()

        // Check if the email field is empty
        if (user.isEmpty()) {
            txtEmail.error = "Email cannot be empty"
            return // Prevent further execution
        }

        // Check if the password field is empty
        if (pass.isEmpty()) {
            textPassword.error = "Password cannot be empty"
            return // Prevent further execution
        }

        // Check if the retyped password field is empty
        if (retypePass.isEmpty()) {
            textRetypePassword.error = "Retype password cannot be empty"
            return // Prevent further execution
        }

        // Check if the passwords match
        if (pass != retypePass) {
            textRetypePassword.error = "Passwords do not match"
            return // Prevent further execution if passwords do not match
        }



        // Proceed with creating user if the passwords match
        auth.createUserWithEmailAndPassword(user, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""

                    // Immediately navigate to SignInActivity upon successful account creation
                    startActivity(Intent(this@SingUpActivity, SingInActivity::class.java))
                    finish()


                        val userInfo = hashMapOf("email" to user)
                            val userRef =
                                FirebaseDatabase.getInstance().getReference("Users").child(userId)
                            userRef.setValue(userInfo).addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {

                                    Toast.makeText(
                                        this@SingUpActivity,
                                        "SignUp Successful",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@SingUpActivity,
                                        "Failed to save user information: ${dbTask.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }




                } else {
                    Toast.makeText(
                        this,
                        "SignUp Failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                }
               }
    }
}


