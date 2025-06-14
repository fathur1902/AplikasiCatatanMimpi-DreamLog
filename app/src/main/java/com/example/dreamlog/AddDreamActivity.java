package com.example.dreamlog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class AddDreamActivity extends AppCompatActivity {
    private EditText editTitle, editDescription;
    private RequestQueue queue;
    private int dreamId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_dream);

        editTitle = findViewById(R.id.editTitle);
        editDescription = findViewById(R.id.editDescription);
        Button btnSave = findViewById(R.id.btnSave);
        queue = Volley.newRequestQueue(this);

        Intent intent = getIntent();
        dreamId = intent.getIntExtra("dream_id", -1);
        if (dreamId != -1) {
            editTitle.setText(intent.getStringExtra("dream_title"));
            editDescription.setText(intent.getStringExtra("dream_description"));
            btnSave.setText("Perbarui");
            Log.d("AddDreamActivity", "Editing dream with ID: " + dreamId);
        } else {
            Log.d("AddDreamActivity", "Adding new dream");
        }

        btnSave.setOnClickListener(v -> saveDream());
    }

    private void saveDream() {
        String url = "https://dreamlog-backend-production.up.railway.app/api/dreams";
        if (dreamId != -1) {
            url += "/" + dreamId;
        }

        SharedPreferences prefs = getSharedPreferences("DreamLogPrefs", MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);

        if (token == null) {
            Log.e("AddDreamActivity", "Token is null, redirecting to WelcomeActivity");
            Toast.makeText(this, "Session expired, please login again", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddDreamActivity.this, WelcomeActivity.class));
            finish();
            return;
        }

        String title = editTitle.getText().toString().trim();
        String description = editDescription.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Title and description are required", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("title", title);
            jsonBody.put("description", description);
            Log.d("AddDreamActivity", "Sending data: " + jsonBody.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    dreamId == -1 ? Request.Method.POST : Request.Method.PUT, url, jsonBody,
                    response -> {
                        Log.d("AddDreamActivity", "Response: " + response.toString());
                        Toast.makeText(this, dreamId == -1 ? "Dream saved!" : "Dream updated!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    },
                    error -> {
                        String errorMessage = "Unknown error";
                        if (error.networkResponse != null) {
                            errorMessage = "Status code: " + error.networkResponse.statusCode;
                            try {
                                String responseData = new String(error.networkResponse.data, "UTF-8");
                                errorMessage += ", Response: " + responseData;
                                Log.e("AddDreamActivity", "Error: " + errorMessage);
                                JSONObject errorResponse = new JSONObject(responseData);
                                if (errorResponse.has("error")) {
                                    Toast.makeText(this, "Error: " + errorResponse.getString("error"), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                                }
                                if (error.networkResponse.statusCode == 401 || error.networkResponse.statusCode == 403) {
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.remove("auth_token");
                                    editor.apply();
                                    Log.w("AddDreamActivity", "Unauthorized, redirecting to WelcomeActivity");
                                    Toast.makeText(this, "Session expired, please login again", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(AddDreamActivity.this, WelcomeActivity.class));
                                    finish();
                                }
                            } catch (Exception e) {
                                Log.e("AddDreamActivity", "Error parsing response: " + e.getMessage());
                                Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                            }
                        } else if (error.getMessage() != null) {
                            errorMessage = error.getMessage();
                            Log.e("AddDreamActivity", "Error: " + errorMessage);
                            Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    Log.d("AddDreamActivity", "Sending request with token: " + token);
                    return headers;
                }
            };
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("AddDreamActivity", "Error creating JSON: " + e.getMessage());
            Toast.makeText(this, "Error creating JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}