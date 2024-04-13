package com.example.ccproj;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import okhttp3.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class MessagingActivity extends AppCompatActivity {
    EditText et;
    TextView t;
    private static final String BASE_URL = "http://3.232.107.171:80/";
    private RecyclerView recyclerView;
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        Intent intent = getIntent();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        String username = intent.getStringExtra("username");
        String me = intent.getStringExtra("me");
        OkHttpClient client = new OkHttpClient();
        et = findViewById(R.id.text_send);
        t= findViewById(R.id.username);
        t.setText(username);
        ImageButton imageButton  = findViewById(R.id.btn_send);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(et.getText().toString(), me, username);
            }
        });
        // Construct the URL with parameters
        String url = BASE_URL + "get-messages/" + username + "/" + me;

        // Create a request
        Request request = new Request.Builder()
                .url(url)
                .build();

        // Enqueue the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle failure
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONArray jsonArray = new JSONArray(responseData);

                        // Initialize arrays to store messages and user indicators
                        String[] messages = new String[jsonArray.length()];
                        int[] userIndicators = new int[jsonArray.length()];

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String message = jsonObject.getString("message");
                            String fromUsername = jsonObject.getString("from_username");
                            String toUsername = jsonObject.getString("to_username");

                            // Store the message
                            messages[i] = message;

                            // Check conditions and store corresponding value in userIndicators
                            if (fromUsername.equals(username) && toUsername.equals(me)) {
                                userIndicators[i] = 0;
                            } else if (fromUsername.equals(me) && toUsername.equals(username)) {
                                userIndicators[i] = 1;
                            }
                            Log.d(TAG, "onResponse: "+messages[i]+" "+userIndicators[i]);
                        }

                        // Now you have two arrays: messages and userIndicators
                        // messages array contains the messages
                        // userIndicators array contains 0 or 1 based on the conditions you provided

                        // Initialize and set adapter for RecyclerView
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter = new MessageAdapter(messages, userIndicators);
                                recyclerView.setAdapter(adapter);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Handle unsuccessful response
                }
            }

        });
    }

    private void sendMessage(String message, String fromUsername, String toUsername) {
        try {
            JSONObject json = new JSONObject();
            json.put("fromUsername", fromUsername);
            json.put("toUsername", toUsername);
            json.put("message", message);

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json.toString());

            Request request = new Request.Builder()
                    .url(BASE_URL + "send-message")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // Handle failure
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        // Handle successful response
                        String responseData = response.body().string();
                        Log.d(TAG, "onResponse: " + responseData);
                        et.setText("");
                        // Process your JSON response here
                    } else {
                        // Handle unsuccessful response
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
