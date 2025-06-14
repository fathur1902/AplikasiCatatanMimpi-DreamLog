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
            Log.w("MainActivity", "No auth token found, redirecting to WelcomeActivity");
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
                Log.d("MainActivity", "Logged out, redirecting to WelcomeActivity");
                startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
                finish();
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && (requestCode == REQUEST_CODE_ADD_DREAM || requestCode == REQUEST_CODE_EDIT_DREAM)) {
            Log.d("MainActivity", "Dream added/edited, reloading dreams");
            loadDreams();
        }
    }

    private void loadDreams() {
        String url = "https://dreamlog-backend-production.up.railway.app/api/dreams";
        SharedPreferences prefs = getSharedPreferences("DreamLogPrefs", MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);

        if (token == null) {
            Log.e("MainActivity", "Token is null, cannot fetch dreams");
            startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
            finish();
            return;
        }

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
                    if (error.networkResponse != null) {
                        String errorMessage = "Status code: " + error.networkResponse.statusCode;
                        try {
                            String responseData = new String(error.networkResponse.data, "UTF-8");
                            errorMessage += ", Response: " + responseData;
                            Log.e("MainActivity", "Error fetching dreams: " + errorMessage);
                            if (error.networkResponse.statusCode == 401 || error.networkResponse.statusCode == 403) {
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.remove("auth_token");
                                editor.apply();
                                Log.w("MainActivity", "Unauthorized, redirecting to WelcomeActivity");
                                startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
                                finish();
                            }
                        } catch (Exception e) {
                            Log.e("MainActivity", "Error parsing error response: " + e.getMessage());
                        }
                    } else if (error.getMessage() != null) {
                        Log.e("MainActivity", "Error fetching dreams: " + error.getMessage());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                Log.d("MainActivity", "Sending request with token: " + token);
                return headers;
            }
        };
        queue.add(request);
    }

    @Override
    public void onEditDream(Dream dream) {
        Log.d("MainActivity", "Editing dream: " + dream.getId());
        Intent intent = new Intent(MainActivity.this, AddDreamActivity.class);
        intent.putExtra("dream_id", dream.getId());
        intent.putExtra("dream_title", dream.getTitle());
        intent.putExtra("dream_description", dream.getDescription());
        startActivityForResult(intent, REQUEST_CODE_EDIT_DREAM);
    }

    @Override
    public void onDeleteDream(Dream dream) {
        String url = "https://dreamlog-backend-production.up.railway.app/api/dreams/" + dream.getId();
        SharedPreferences prefs = getSharedPreferences("DreamLogPrefs", MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);

        if (token == null) {
            Log.e("MainActivity", "Token is null, cannot delete dream");
            startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
            finish();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                response -> {
                    Log.d("MainActivity", "Dream deleted: " + dream.getId());
                    loadDreams();
                },
                error -> {
                    if (error.networkResponse != null) {
                        String errorMessage = "Status code: " + error.networkResponse.statusCode;
                        try {
                            String responseData = new String(error.networkResponse.data, "UTF-8");
                            errorMessage += ", Response: " + responseData;
                            Log.e("MainActivity", "Error deleting dream: " + errorMessage);
                            if (error.networkResponse.statusCode == 401 || error.networkResponse.statusCode == 403) {
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.remove("auth_token");
                                editor.apply();
                                Log.w("MainActivity", "Unauthorized, redirecting to WelcomeActivity");
                                startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
                                finish();
                            }
                        } catch (Exception e) {
                            Log.e("MainActivity", "Error parsing error response: " + e.getMessage());
                        }
                    } else if (error.getMessage() != null) {
                        Log.e("MainActivity", "Error deleting dream: " + error.getMessage());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                Log.d("MainActivity", "Sending DELETE request with token: " + token);
                return headers;
            }
        };
        queue.add(request);
    }
}