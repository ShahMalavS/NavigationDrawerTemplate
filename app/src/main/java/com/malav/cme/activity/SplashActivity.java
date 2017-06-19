package com.malav.cme.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.malav.cme.R;

public class SplashActivity extends AppCompatActivity implements Animation.AnimationListener {

    Animation animFadeOut;
    ImageView splash_logo;
    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splash_logo = (ImageView) findViewById(R.id.imageView1);

        new Handler().postDelayed(new Runnable() {

	            /*sd
	             * Showing splash screen with a timer. This will be useful when you
	             * want to show case your app logo / company
	             */

            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        // Take any action after completing the animation

        // check for fade out animation
        if (animation == animFadeOut) {
            //	Toast.makeText(getApplicationContext(), "Animation Stopped",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
