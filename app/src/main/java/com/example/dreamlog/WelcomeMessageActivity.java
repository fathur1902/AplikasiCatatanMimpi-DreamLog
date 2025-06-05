package com.example.dreamlog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeMessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_message);

        // Inisialisasi elemen UI
        ImageView logoImage = findViewById(R.id.logoImage);
        ImageView decorationCircle = findViewById(R.id.decorationCircle);
        TextView welcomeText = findViewById(R.id.welcomeText);
        Button continueButton = findViewById(R.id.continueButton);

        // Ambil full_name dari SharedPreferences
        SharedPreferences prefs = getSharedPreferences("DreamLogPrefs", MODE_PRIVATE);
        String fullName = prefs.getString("full_name", "User");
        String welcomeMessage = "Selamat Datang di DreamLog, " + fullName + "!\nMimpi Apa Hari Ini?";
        welcomeText.setText(welcomeMessage);


        // Tambahkan animasi
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        // Listener untuk memeriksa animasi
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.d("WelcomeMessageActivity", "Animation started for decorationCircle and logoImage");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d("WelcomeMessageActivity", "Animation ended - logoImage alpha: " + logoImage.getAlpha() + ", decorationCircle alpha: " + decorationCircle.getAlpha());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        // Terapkan animasi
        logoImage.startAnimation(fadeIn);
        decorationCircle.startAnimation(slideUp);
        welcomeText.startAnimation(slideUp);
        continueButton.startAnimation(fadeIn);

        // Aksi tombol lanjut
        continueButton.setOnClickListener(v -> {
            Log.d("WelcomeMessageActivity", "Redirecting to MainActivity");
            startActivity(new Intent(WelcomeMessageActivity.this, MainActivity.class));
            finish();
        });
    }
}