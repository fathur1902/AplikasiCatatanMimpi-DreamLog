package com.example.dreamlog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
        }

        btnSave.setOnClickListener(v -> saveDream());
    }

    private void saveDream() {
        String url = "http://192.168.1.2:8080/api/dreams";
        if (dreamId != -1) {
            url += "/" + dreamId;
        }

        SharedPreferences prefs = getSharedPreferences("DreamLogPrefs", MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("title", editTitle.getText().toString());
            jsonBody.put("description", editDescription.getText().toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    dreamId == -1 ? Request.Method.POST : Request.Method.PUT, url, jsonBody,
                    response -> {
                        Toast.makeText(this, dreamId == -1 ? "Dream saved!" : "Dream updated!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    },
                    error -> {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        if (error.networkResponse != null && (error.networkResponse.statusCode == 401 || error.networkResponse.statusCode == 403)) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.remove("auth_token");
                            editor.apply();
                            startActivity(new Intent(AddDreamActivity.this, WelcomeActivity.class));
                            finish();
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}