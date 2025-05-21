package com.example.dreamlog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private EditText editUsername, editPassword;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);
        queue = Volley.newRequestQueue(this);

        SharedPreferences prefs = getSharedPreferences("DreamLogPrefs", MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);
        if (token != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        btnLogin.setOnClickListener(v -> login());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void login() {
        String url = "http://192.168.1.2:8080/api/users/login";
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", editUsername.getText().toString());
            jsonBody.put("password", editPassword.getText().toString());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        try {
                            String token = response.getString("token");
                            SharedPreferences prefs = getSharedPreferences("DreamLogPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("auth_token", token);
                            editor.apply();

                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } catch (Exception e) {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show());
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}