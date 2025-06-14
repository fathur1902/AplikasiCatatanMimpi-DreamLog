package com.example.dreamlog;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);

        // Inisialisasi elemen UI
        Button btnSignIn = findViewById(R.id.btnSignIn);
        Button btnSignUp = findViewById(R.id.btnSignUp);

        // Tambahkan animasi fade-in untuk tombol
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        btnSignIn.startAnimation(fadeIn);
        btnSignUp.startAnimation(fadeIn);

        // Aksi klik untuk tombol Sign In
        btnSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Aksi klik untuk tombol Sign Up
        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }
}