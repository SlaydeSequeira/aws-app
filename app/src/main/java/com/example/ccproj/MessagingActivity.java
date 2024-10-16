package com.example.ccproj;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
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
    private static final String BASE_URL = "http://54.145.223.218:3000/";
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
                            showNotification(message);

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
    public void showNotification(String username) {
        // Create a notification channel (required for Android Oreo and later)
        String channelId = "channel_id";
        CharSequence channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }

        // Create an intent to open a web URL when the notification is clicked
        String quizUrl = "https://c884-103-81-240-214.ngrok-free.app/take_quiz/2/";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(quizUrl));

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Create the notification
        Notification notification = new Notification.Builder(this, channelId)
                .setContentTitle("New Message")
                .setContentText(username)
                .setSmallIcon(R.drawable.baseline_email_24)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true) // Notification will be removed when clicked
                .build();

        // Show the notification
        if (notificationManager != null) {
            notificationManager.notify(0, notification);
        }
    }
}
