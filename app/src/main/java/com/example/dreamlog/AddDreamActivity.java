package com.example.dreamlog;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class AddDreamActivity extends AppCompatActivity {
    private EditText editTitle, editDescription, editEmotion;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_dream);

        editTitle = findViewById(R.id.editTitle);
        editDescription = findViewById(R.id.editDescription);
        editEmotion = findViewById(R.id.editEmotion);
        Button btnSave = findViewById(R.id.btnSave);
        queue = Volley.newRequestQueue(this);

        btnSave.setOnClickListener(v -> saveDream());
    }

    private void saveDream() {
        String url = "http://192.168.1.5:8080/api/dreams"; // Untuk emulator
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("title", editTitle.getText().toString());
            jsonBody.put("description", editDescription.getText().toString());
            jsonBody.put("emotion", editEmotion.getText().toString());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        Toast.makeText(this, "Dream saved!", Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    error -> Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show());
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}