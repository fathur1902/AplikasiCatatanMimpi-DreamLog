package com.example.dreamlog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
            Log.d("LoginActivity", "Token found, redirecting to WelcomeMessageActivity");
            startActivity(new Intent(LoginActivity.this, WelcomeMessageActivity.class));
            finish();
        }

        btnLogin.setOnClickListener(v -> login());

        tvRegister.setOnClickListener(v -> {
            Log.d("LoginActivity", "Redirecting to RegisterActivity");
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void login() {
        String url = "http://192.168.1.12:8080/api/users/login";

        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", username);
            jsonBody.put("password", password);
            Log.d("LoginActivity", "Sending login data: " + jsonBody.toString());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        try {
                            String token = response.getString("token");
                            String fullName = response.getJSONObject("user").getString("full_name"); // Ambil full_name dari respons
                            SharedPreferences prefs = getSharedPreferences("DreamLogPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("auth_token", token);
                            editor.putString("full_name", fullName);
                            editor.apply();

                            Log.d("LoginActivity", "Login successful, token and full_name saved");
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, WelcomeMessageActivity.class));
                            finish();
                        } catch (Exception e) {
                            Log.e("LoginActivity", "Error parsing response: " + e.getMessage());
                            Toast.makeText(this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        String errorMessage = "Unknown error";
                        if (error.networkResponse != null) {
                            errorMessage = "Status code: " + error.networkResponse.statusCode;
                            try {
                                String responseData = new String(error.networkResponse.data, "UTF-8");
                                errorMessage += ", Response: " + responseData;
                                Log.e("LoginActivity", "Error details: " + errorMessage);
                                JSONObject errorResponse = new JSONObject(responseData);
                                if (errorResponse.has("error")) {
                                    String serverError = errorResponse.getString("error");
                                    if (serverError.equals("Invalid username or password")) {
                                        Toast.makeText(this, "Username or password incorrect", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(this, "Error: " + serverError, Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                Log.e("LoginActivity", "Error parsing error response: " + e.getMessage());
                                Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                            }
                        } else if (error.getMessage() != null) {
                            errorMessage = error.getMessage();
                            Log.e("LoginActivity", "Error details: " + errorMessage);
                            Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error: Unable to connect to server", Toast.LENGTH_SHORT).show();
                        }
                    });
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("LoginActivity", "Error creating JSON: " + e.getMessage());
            Toast.makeText(this, "Error creating JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}