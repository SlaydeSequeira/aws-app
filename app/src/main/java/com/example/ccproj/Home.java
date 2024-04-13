package com.example.ccproj;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Home extends AppCompatActivity {

    private static final String TAG = "Home";

    private ListView listView;
    private List<String> usernamesList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Retrieve the value passed via intent
        Intent intent = getIntent();
        String value = intent.getStringExtra("user");
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show();

        // Initialize ListView and adapter
        listView = findViewById(R.id.listView);
        usernamesList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usernamesList);
        listView.setAdapter(adapter);

        // Set item click listener on ListView
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String clickedUsername = usernamesList.get(position);
            openMessagingActivity(clickedUsername,value);
        });

        // Create OkHttp client
        OkHttpClient client = new OkHttpClient();

        // Create request
        Request request = new Request.Builder()
                .url("http://3.232.107.171/users")
                .build();

        // Make asynchronous call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    Log.d(TAG, "JSON Response: " + jsonResponse);

                    // Parse JSON response
                    Gson gson = new Gson();
                    User[] users = gson.fromJson(jsonResponse, User[].class);

                    // Extract usernames and update UI
                    usernamesList.clear();
                    for (User user : users) {
                        usernamesList.add(user.getUsername());
                    }

                    // Update UI on the main thread
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    Log.e(TAG, "HTTP Error: " + response.code());
                }
            }
        });
    }

    // User class to match JSON structure
    private static class User {
        private int id;
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }
    }

    // Method to open MessagingActivity with the clicked username
    private void openMessagingActivity(String username,String myname) {
        Intent intent = new Intent(this, MessagingActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("me",myname);
        startActivity(intent);
    }
}
