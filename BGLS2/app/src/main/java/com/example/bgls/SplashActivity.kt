package com.example.bgls

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val tvB = findViewById<TextView>(R.id.tvB)
        val tvL = findViewById<TextView>(R.id.tvL)
        val tvM = findViewById<TextView>(R.id.tvM)
        val tvS = findViewById<TextView>(R.id.tvS)
        val tvSubtitle = findViewById<TextView>(R.id.tvSubtitle)
        val ivLogo = findViewById<ImageView>(R.id.ivLogo)

        // Initial setup - hide elements
        tvB.alpha = 0f
        tvL.alpha = 0f
        tvM.alpha = 0f
        tvS.alpha = 0f
        tvSubtitle.alpha = 0f
        ivLogo.alpha = 0f

        // Initial positions for sliding in
        tvB.translationX = -300f
        tvB.translationY = -300f
        
        tvL.translationY = -400f
        
        tvM.translationY = 400f
        
        tvS.translationX = 300f
        tvS.translationY = 300f

        // Create Animators
        val overshoot = OvershootInterpolator(1.5f)
        val duration = 1000L

        // B Animation
        val animBAlpha = ObjectAnimator.ofFloat(tvB, View.ALPHA, 0f, 1f)
        val animBX = ObjectAnimator.ofFloat(tvB, View.TRANSLATION_X, 0f)
        val animBY = ObjectAnimator.ofFloat(tvB, View.TRANSLATION_Y, 0f)
        val animBSet = AnimatorSet().apply {
            playTogether(animBAlpha, animBX, animBY)
            interpolator = overshoot
            this.duration = duration
        }

        // L Animation
        val animLAlpha = ObjectAnimator.ofFloat(tvL, View.ALPHA, 0f, 1f)
        val animLY = ObjectAnimator.ofFloat(tvL, View.TRANSLATION_Y, 0f)
        val animLSet = AnimatorSet().apply {
            playTogether(animLAlpha, animLY)
            interpolator = overshoot
            this.duration = duration
            startDelay = 200
        }

        // M Animation
        val animMAlpha = ObjectAnimator.ofFloat(tvM, View.ALPHA, 0f, 1f)
        val animMY = ObjectAnimator.ofFloat(tvM, View.TRANSLATION_Y, 0f)
        val animMSet = AnimatorSet().apply {
            playTogether(animMAlpha, animMY)
            interpolator = overshoot
            this.duration = duration
            startDelay = 400
        }

        // S Animation
        val animSAlpha = ObjectAnimator.ofFloat(tvS, View.ALPHA, 0f, 1f)
        val animSX = ObjectAnimator.ofFloat(tvS, View.TRANSLATION_X, 0f)
        val animSY = ObjectAnimator.ofFloat(tvS, View.TRANSLATION_Y, 0f)
        val animSSet = AnimatorSet().apply {
            playTogether(animSAlpha, animSX, animSY)
            interpolator = overshoot
            this.duration = duration
            startDelay = 600
        }

        // Subtitle and Logo fade in
        val animSubtitle = ObjectAnimator.ofFloat(tvSubtitle, View.ALPHA, 0f, 1f).apply {
            this.duration = 800
            interpolator = AccelerateDecelerateInterpolator()
        }
        val animSubtitleY = ObjectAnimator.ofFloat(tvSubtitle, View.TRANSLATION_Y, 50f, 0f).apply {
            this.duration = 800
            interpolator = OvershootInterpolator()
        }

        val animLogo = ObjectAnimator.ofFloat(ivLogo, View.ALPHA, 0f, 1f).apply {
            this.duration = 1000
        }
        
        val scaleLogoX = ObjectAnimator.ofFloat(ivLogo, View.SCALE_X, 0.5f, 1f)
        val scaleLogoY = ObjectAnimator.ofFloat(ivLogo, View.SCALE_Y, 0.5f, 1f)
        val logoScaleSet = AnimatorSet().apply {
            playTogether(animLogo, scaleLogoX, scaleLogoY)
            this.duration = 1000
            interpolator = OvershootInterpolator()
        }

        // Master Animator Set
        val masterSet = AnimatorSet()
        masterSet.playTogether(animBSet, animLSet, animMSet, animSSet)
        
        val finalSet = AnimatorSet()
        finalSet.playSequentially(masterSet, AnimatorSet().apply { playTogether(animSubtitle, animSubtitleY, logoScaleSet) })
        
        finalSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Add a small delay before transitioning
                ivLogo.postDelayed({
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }, 1500) // 1.5 seconds delay to admire the animation
            }
        })

        // Start animation
        finalSet.start()
    }
}
