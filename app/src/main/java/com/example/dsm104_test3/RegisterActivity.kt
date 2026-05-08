package com.example.dsm104_test3

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dsm104_test3.model.Usuario
import com.example.dsm104_test3.network.RetrofitClient
import com.example.dsm104_test3.util.PasswordValidator
import com.example.dsm104_test3.util.SessionManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var rgRole: RadioGroup
    private lateinit var rbEstudiante: RadioButton
    private lateinit var rbDocente: RadioButton
    private lateinit var btnRegister: Button
    private lateinit var tvGoLogin: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        rgRole = findViewById(R.id.rgRole)
        rbEstudiante = findViewById(R.id.rbEstudiante)
        rbDocente = findViewById(R.id.rbDocente)
        btnRegister = findViewById(R.id.btnRegister)
        tvGoLogin = findViewById(R.id.tvGoLogin)
        progressBar = findViewById(R.id.progressBar)

        btnRegister.setOnClickListener { doRegister() }
        tvGoLogin.setOnClickListener { finish() }
    }

    private fun doRegister() {
        val username = etUsername.text?.toString()?.trim() ?: ""
        val email = etEmail.text?.toString()?.trim() ?: ""
        val password = etPassword.text?.toString() ?: ""
        val role = if (rbDocente.isChecked) SessionManager.ROLE_DOCENTE else SessionManager.ROLE_ESTUDIANTE

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fields_required), Toast.LENGTH_SHORT).show()
            return
        }

        val validation = PasswordValidator.validate(password)
        if (validation is PasswordValidator.ValidationResult.Error) {
            val msg = when (validation.code) {
                "error_length" -> getString(R.string.error_password_length)
                "error_uppercase" -> getString(R.string.error_password_uppercase)
                "error_lowercase" -> getString(R.string.error_password_lowercase)
                "error_number" -> getString(R.string.error_password_number)
                "error_special" -> getString(R.string.error_password_special)
                else -> getString(R.string.error_generic)
            }
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val usersResponse = RetrofitClient.apiService.getUsers()
                if (usersResponse.isSuccessful) {
                    val exists = usersResponse.body()?.any { it.email.equals(email, ignoreCase = true) } == true
                    if (exists) {
                        Toast.makeText(this@RegisterActivity, getString(R.string.error_email_exists), Toast.LENGTH_SHORT).show()
                        setLoading(false)
                        return@launch
                    }
                }

                val newUser = Usuario(
                    username = username,
                    email = email,
                    password = password,
                    role = role
                )
                val response = RetrofitClient.apiService.createUser(newUser)
                if (response.isSuccessful) {
                    val created = response.body()!!
                    SessionManager(this@RegisterActivity).saveSession(created.id, created.username, created.email, created.role)
                    Toast.makeText(this@RegisterActivity, getString(R.string.success_register), Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                } else {
                    Toast.makeText(this@RegisterActivity, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, getString(R.string.error_network), Toast.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnRegister.isEnabled = !loading
    }
}
