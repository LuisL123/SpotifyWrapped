package com.example.wrappedspotify;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomePageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private OkHttpClient mOkHttpClient = new OkHttpClient();
    private Button fetchTopSongsButton, requestTokenButton, viewHistoryButton;
    private String mAccessToken;

    private static final String REDIRECT_URI = "com.example.wrappedspotify://auth";
    private static final String CLIENT_ID = "045795b20f1745d5960bbbef296de6a1";
    private static final int REQUEST_CODE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        fetchTopSongsButton = findViewById(R.id.buttonFetchTopSongs);
        fetchTopSongsButton.setOnClickListener(v -> fetchSpotifyAccessToken());

        requestTokenButton = findViewById(R.id.buttonRequestNewToken);
        requestTokenButton.setOnClickListener(v -> authenticateSpotify());

        viewHistoryButton = findViewById(R.id.buttonViewHistory);
        viewHistoryButton.setOnClickListener(v -> viewHistory());

        Button logoutButton = findViewById(R.id.buttonLogout);
        logoutButton.setOnClickListener(v -> logoutUser());

        Button editAccountButton = findViewById(R.id.buttonEditAccount);
        editAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, EditAccountActivity.class);
            startActivity(intent);
        });
    }

    private void fetchSpotifyAccessToken() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            mDatabase.child("users").child(userId).child("spotifyToken")
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            mAccessToken = task.getResult().getValue(String.class);
                            if (mAccessToken != null && !mAccessToken.isEmpty()) {
                                fetchTopDataFromSpotify();
                            } else {
                                Toast.makeText(HomePageActivity.this, "Access token not found.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(HomePageActivity.this, "Failed to fetch access token.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "User is not signed in.", Toast.LENGTH_SHORT).show();
        }

    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(HomePageActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    private void fetchTopDataFromSpotify() {
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/tracks?limit=3")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("SpotifyAPI", "Network error while fetching data", e);
                runOnUiThread(() -> Toast.makeText(HomePageActivity.this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseData = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e("SpotifyAPI", "Failed to fetch data: " + responseData);
                    runOnUiThread(() -> Toast.makeText(HomePageActivity.this, "Fetch failed: " + response.message(), Toast.LENGTH_LONG).show());
                    return;
                }
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    JSONArray items = jsonObject.getJSONArray("items");
                    List<String> topTracks = new ArrayList<>();
                    List<String> topArtists = new ArrayList<>();
                    List<String> artistIds = new ArrayList<>();
                    List<String> trackURIs = new ArrayList<>();

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject track = items.getJSONObject(i);
                        topTracks.add(track.getString("name"));
                        trackURIs.add(track.getString("uri")); // Store the track URI
                        JSONObject artist = track.getJSONArray("artists").getJSONObject(0);
                        topArtists.add(artist.getString("name"));
                        artistIds.add(artist.getString("id"));
                    }

                    fetchArtistGenres(artistIds, topTracks, topArtists, trackURIs);
                } catch (Exception e) {
                    Log.e("SpotifyAPI", "Error parsing Spotify data", e);
                    runOnUiThread(() -> Toast.makeText(HomePageActivity.this, "Error parsing Spotify data: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void authenticateSpotify() {
        AuthorizationRequest request = new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
                .setShowDialog(false)
                .setScopes(new String[]{"user-read-email", "user-top-read"})
                .build();
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    private void viewHistory() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            mDatabase.child("users").child(userId).child("historicalDataList")
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            ArrayList<ArrayList<String>> historicalDataList = new ArrayList<>();
                            for (Object entry : ((HashMap<?, ?>) task.getResult().getValue()).values()) {
                                historicalDataList.add((ArrayList<String>) entry);
                            }
                            Intent intent = new Intent(HomePageActivity.this, HistoricalListsActivity.class);
                            intent.putExtra("historicalDataList", historicalDataList);
                            startActivity(intent);
                        } else {
                            Toast.makeText(HomePageActivity.this, "No historical data found.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "User is not signed in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchArtistGenres(List<String> artistIds, List<String> topTracks, List<String> topArtists, List<String> trackURIs) {
        List<String> topGenres = new ArrayList<>();
        AtomicInteger callsCompleted = new AtomicInteger(0);
        for (String id : artistIds) {
            Request artistRequest = new Request.Builder()
                    .url("https://api.spotify.com/v1/artists/" + id)
                    .addHeader("Authorization", "Bearer " + mAccessToken)
                    .build();

            mOkHttpClient.newCall(artistRequest).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("SpotifyAPI", "Error fetching artist details", e);
                    if (callsCompleted.incrementAndGet() == artistIds.size()) {
                        storeDataInFirebase(topTracks, topArtists, topGenres, trackURIs);  // Proceed even if some requests fail
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        String responseData = response.body().string();
                        JSONObject artistObject = new JSONObject(responseData);
                        JSONArray genres = artistObject.getJSONArray("genres");
                        if (genres.length() > 0) {
                            synchronized (topGenres) {
                                topGenres.add(genres.getString(0)); // Add the primary genre
                            }
                        }

                        if (callsCompleted.incrementAndGet() == artistIds.size()) {
                            storeDataInFirebase(topTracks, topArtists, topGenres, trackURIs);
                        }
                    } catch (Exception e) {
                        Log.e("SpotifyAPI", "Error parsing artist details", e);
                    }
                }
            });
        }
    }

    private void storeDataInFirebase(List<String> topTracks, List<String> topArtists, List<String> topGenres, List<String> trackURIs) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            Map<String, Object> spotifyData = new HashMap<>();
            spotifyData.put("topTracks", topTracks);
            spotifyData.put("topArtists", topArtists);
            spotifyData.put("topGenres", topGenres);
            spotifyData.put("trackURIs", trackURIs); // Adding track URIs to be stored in Firebase

            mDatabase.child("users").child(userId).child("historicalDataList").push().setValue(topTracks)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(HomePageActivity.this, "Historical data saved successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(HomePageActivity.this, "Failed to save historical data.", Toast.LENGTH_SHORT).show();
                        }
                    });

            mDatabase.child("users").child(userId).child("spotifyData").setValue(spotifyData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(HomePageActivity.this, "Spotify data saved successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(HomePageActivity.this, SpotifyWrappedActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(HomePageActivity.this, "Failed to save Spotify data.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "User is not signed in.", Toast.LENGTH_SHORT).show();
        }
    }
}
