package com.example.ccproj;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;


import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameEditText = findViewById(R.id.edittext1);
        passwordEditText = findViewById(R.id.edittext2);
        loginButton = findViewById(R.id.button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (!username.isEmpty() && !password.isEmpty()) {
                    new LoginTask().execute(username, password);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class LoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String response = "";
            try {
                URL url = new URL("http://3.232.107.171:80/login");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                String requestBody = "{\"username\": \"" + params[0] + "\", \"password\": \"" + params[1] + "\"}";

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(requestBody.getBytes());
                outputStream.flush();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        stringBuilder.append(inputLine);
                    }
                    in.close();
                    response = stringBuilder.toString();
                } else {
                    response = "Error: " + responseCode;
                }

                connection.disconnect();
            } catch (IOException e) {
                Log.e(TAG, "Error: " + e.getMessage());
                response = "Error: " + e.getMessage();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            // Process the result here
            Log.d(TAG, "Response: " + result);

            try {
                JSONObject jsonResponse = new JSONObject(result);
                if (jsonResponse.has("message") && jsonResponse.getString("message").equals("Login successful")) {
                    // Navigate to HomePageActivity
                    Intent intent = new Intent(MainActivity.this, Home.class);
                    startActivity(intent);
                    finish(); // Prevent going back to MainActivity when pressing back button from HomePageActivity
                } else {
                    Toast.makeText(MainActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSON parsing error: " + e.getMessage());
                Toast.makeText(MainActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

