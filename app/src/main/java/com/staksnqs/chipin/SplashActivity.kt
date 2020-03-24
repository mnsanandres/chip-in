package com.staksnqs.chipin

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

//    private val SPLASH_TIME_OUT:Long = 2000
    private val SPLASH_TIME_OUT:Long = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        titleView.typeface = Typeface.createFromAsset(assets, "fonts/bungee.ttf")

        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, SPLASH_TIME_OUT)
    }
}
