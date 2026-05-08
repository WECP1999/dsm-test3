package com.example.dsm104_test3

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.dsm104_test3.util.SessionManager

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val session = SessionManager(this)
            if (session.isLoggedIn()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2000)
    }
}
