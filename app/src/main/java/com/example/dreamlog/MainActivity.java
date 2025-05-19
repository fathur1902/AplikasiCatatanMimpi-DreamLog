package com.example.dreamlog;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.plattysoft.leonids.ParticleSystem;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DreamAdapter adapter;
    private List<Dream> dreamList = new ArrayList<>();
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DreamAdapter(dreamList);
        recyclerView.setAdapter(adapter);
        recyclerView.setAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));

        queue = Volley.newRequestQueue(this);
        loadDreams();

//        new ParticleSystem(this, 100, android.R.drawable.star_big_on, 8000)
//                .setSpeedRange(0.01f, 0.05f)
//                .setScaleRange(0.5f, 1.5f)
//                .setRotationSpeed(30)
//                .emit(findViewById(R.id.particleContainer), 10);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddDreamActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDreams();
    }

    private void loadDreams() {
        String url = "http://192.168.1.5:8080/api/dreams"; // Untuk emulator
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    dreamList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            Dream dream = new Dream(
                                    obj.getString("title"),
                                    obj.getString("description"),
                                    obj.getString("emotion")
                            );
                            dreamList.add(dream);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> error.printStackTrace());
        queue.add(request);
    }
}