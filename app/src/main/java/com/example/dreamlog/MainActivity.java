package com.example.dreamlog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements DreamAdapter.OnDreamActionListener {
    private RecyclerView recyclerView;
    private DreamAdapter adapter;
    private List<Dream> dreamList = new ArrayList<>();
    private RequestQueue queue;
    private static final int REQUEST_CODE_ADD_DREAM = 1;
    private static final int REQUEST_CODE_EDIT_DREAM = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DreamAdapter(dreamList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));

        queue = Volley.newRequestQueue(this);

        SharedPreferences prefs = getSharedPreferences("DreamLogPrefs", MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);
        if (token == null) {
            startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
            finish();
            return;
        }

        loadDreams();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddDreamActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_DREAM);
        });

        FloatingActionButton fabLogout = findViewById(R.id.fab_logout);
        if (fabLogout != null) {
            fabLogout.setOnClickListener(v -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove("auth_token");
                editor.apply();
                startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
                finish();
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && (requestCode == REQUEST_CODE_ADD_DREAM || requestCode == REQUEST_CODE_EDIT_DREAM)) {
            loadDreams();
        }
    }

    private void loadDreams() {
        String url = "http://192.168.1.2:8080/api/dreams";
        SharedPreferences prefs = getSharedPreferences("DreamLogPrefs", MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("MainActivity", "Response received: " + response.toString());
                    dreamList.clear();
                    if (response.length() == 0) {
                        Log.w("MainActivity", "No dreams received from server");
                    }
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            int id = obj.optInt("id", -1);
                            String title = obj.optString("title", "No Title");
                            String description = obj.optString("description", "No Description");
                            Dream dream = new Dream(id, title, description);
                            dreamList.add(dream);
                            Log.d("MainActivity", "Dream added: " + dream.getTitle() + ", Emotion: " + dream.getEmotion());
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("MainActivity", "Error parsing dream: " + e.getMessage());
                        }
                    }
                    Log.d("MainActivity", "Total dreams: " + dreamList.size());
                    adapter.notifyDataSetChanged();
                },
                error -> {
                    error.printStackTrace();
                    Log.e("MainActivity", "Error fetching dreams: " + error.getMessage());
                    if (error.networkResponse != null && (error.networkResponse.statusCode == 401 || error.networkResponse.statusCode == 403)) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.remove("auth_token");
                        editor.apply();
                        startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
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
    }

    @Override
    public void onEditDream(Dream dream) {
        Intent intent = new Intent(MainActivity.this, AddDreamActivity.class);
        intent.putExtra("dream_id", dream.getId());
        intent.putExtra("dream_title", dream.getTitle());
        intent.putExtra("dream_description", dream.getDescription());
        startActivityForResult(intent, REQUEST_CODE_EDIT_DREAM);
    }

    @Override
    public void onDeleteDream(Dream dream) {
        String url = "http://192.168.1.2:8080/api/dreams/" + dream.getId();
        SharedPreferences prefs = getSharedPreferences("DreamLogPrefs", MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                response -> {
                    Log.d("MainActivity", "Dream deleted: " + dream.getId());
                    loadDreams();
                },
                error -> {
                    error.printStackTrace();
                    Log.e("MainActivity", "Error deleting dream: " + error.getMessage());
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        queue.add(request);
    }
}