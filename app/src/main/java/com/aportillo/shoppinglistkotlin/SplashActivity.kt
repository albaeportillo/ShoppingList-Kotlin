package com.aportillo.shoppinglistkotlin

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.aportillo.shoppinglistkotlin.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseUser


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val animation: Animation = AnimationUtils.loadAnimation(this, R.anim.blink)
        animation.interpolator = LinearInterpolator()
        animation.repeatCount = Animation.INFINITE
        animation.duration = 3*800
        /////

        var img =  findViewById<ImageView>(R.id.ivSplashScreen)
        var linearLayout = findViewById<LinearLayout>(R.id.linearLayoutV)
       img.startAnimation(animation)


        var txt = findViewById<TextView>(R.id.textSplashScreebrokn)
        txt.startAnimation(animation)

        val secondsDelayed = 3

        /////////


        startActivityOnDelay(secondsDelayed)
    }

    private fun startActivityOnDelay(secondsDelayed: Int) {
        Handler().postDelayed(Runnable {
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish()
        }, secondsDelayed.toLong() * 2000)
    }

}