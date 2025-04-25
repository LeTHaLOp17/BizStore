package com.service.bizstore

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.service.bizstore.databinding.ActivityLoginBinding
import com.service.bizstore.Model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private var userName: String? = null
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialization of Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        // login with email and password
        binding.loginButton.setOnClickListener {
            // get data from text field
            email = binding.emailAddress.text.toString().trim()
            password = binding.password.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please enter all the details 😒", Toast.LENGTH_SHORT).show()
            } else {
                createUser()
                Toast.makeText(this, "Login successFull 😁", Toast.LENGTH_SHORT).show()
            }
        }

        // navigate to Sign Up
        binding.donthavebutton.setOnClickListener {
            val intent = Intent(this, SignActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createUser() {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                updateUi(user)
            } else {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        saveUserdata()
                        val user = auth.currentUser
                        updateUi(user)
                    } else {
                        Toast.makeText(this, "Sign-in failed 😒", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun saveUserdata() {
        email = binding.emailAddress.text.toString().trim()
        password = binding.password.text.toString().trim()

        val user = UserModel(userName, email, password)
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        database.child("user").child(userId).setValue(user)
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun updateUi(user: FirebaseUser?) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
