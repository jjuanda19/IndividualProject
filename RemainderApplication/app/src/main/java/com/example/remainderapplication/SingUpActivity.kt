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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


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
        buttonSingUp = findViewById(R.id.button_sing_up)
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

                    // Reference to the node where we keep the last used userID
                    val lastUserIdRef = FirebaseDatabase.getInstance().getReference("lastUserID")
                    lastUserIdRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var lastId = snapshot.getValue(Int::class.java) ?: 0
                            val nextId = lastId + 1

                            // Formatting the ID to be four digits (e.g., 0001)
                            val formattedId = String.format("%04d", nextId)

                            // Now, save the new user's info along with the formattedId
                            val userInfo = hashMapOf(
                                "userId" to formattedId,
                                "email" to user
                            )

                            val userRef =
                                FirebaseDatabase.getInstance().getReference("Users").child(userId)
                            userRef.setValue(userInfo).addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    // Update the last used ID in the database
                                    lastUserIdRef.setValue(nextId)

                                    // Success handling

                                    Toast.makeText(
                                        this@SingUpActivity,
                                        "SignUp Successful, ID: $formattedId",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(
                                        Intent(
                                            this@SingUpActivity,
                                            SingInActivity::class.java
                                        )
                                    )
                                    finish()
                                } else {

                                    Toast.makeText(
                                        this@SingUpActivity,
                                        "Failed to save user information: ${dbTask.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                            Toast.makeText(
                                this@SingUpActivity,
                                "Database Error: ${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
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


