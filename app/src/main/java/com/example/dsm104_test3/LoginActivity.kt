package com.example.dsm104_test3

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dsm104_test3.network.RetrofitClient
import com.example.dsm104_test3.util.SessionManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var tvGoRegister: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        session = SessionManager(this)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvGoRegister = findViewById(R.id.tvGoRegister)
        progressBar = findViewById(R.id.progressBar)

        btnLogin.setOnClickListener { doLogin() }
        tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun doLogin() {
        val email = etEmail.text?.toString()?.trim() ?: ""
        val password = etPassword.text?.toString() ?: ""

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fields_required), Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getUsers()
                if (response.isSuccessful) {
                    val users = response.body() ?: emptyList()
                    val user = users.find { it.email.equals(email, ignoreCase = true) }
                    if (user == null) {
                        Toast.makeText(this@LoginActivity, getString(R.string.error_email_not_found), Toast.LENGTH_SHORT).show()
                    } else if (user.password != password) {
                        Toast.makeText(this@LoginActivity, getString(R.string.error_wrong_password), Toast.LENGTH_SHORT).show()
                    } else {
                        session.saveSession(user.id, user.username, user.email, user.role)
                        Toast.makeText(this@LoginActivity, getString(R.string.success_login), Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, getString(R.string.error_network), Toast.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !loading
    }
}
