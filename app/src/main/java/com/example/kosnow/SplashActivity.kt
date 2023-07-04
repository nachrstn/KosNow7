package com.example.kosnow

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val ivLogo = findViewById<ImageView>(R.id.imageView)
        Glide.with(this).load(R.drawable.ic_logo).into(ivLogo)
        Handler().postDelayed({
            val homeIntent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(homeIntent)
            finish()
        }, SPLASH_TIME_OUT.toLong())
    }

    companion object {
        private const val SPLASH_TIME_OUT = 4000
    }
}