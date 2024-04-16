package com.example.wrappedspotify;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;

public class SpotifyWrappedActivity extends AppCompatActivity {

    private ListView listViewData;
    private Button nextButton;
    private TextView categoryHeader;
    private DatabaseReference mDatabase;

    private List<String> topTracks;
    private List<String> topArtists;
    private List<String> topGenres;
//COMMENT COMMIT TEST
    private int currentDisplay = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_wrapped);

        listViewData = findViewById(R.id.listViewData);
        nextButton = findViewById(R.id.nextButton);
        categoryHeader = findViewById(R.id.categoryHeader);


        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            mDatabase.child("users").child(userId).child("spotifyData").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                        topTracks = dataSnapshot.child("topTracks").getValue(t);
                        topArtists = dataSnapshot.child("topArtists").getValue(t);
                        topGenres = dataSnapshot.child("topGenres").getValue(t);
                        updateDisplay();
                    } else {
                        Toast.makeText(SpotifyWrappedActivity.this, "No Spotify data found.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(SpotifyWrappedActivity.this, "Failed to fetch Spotify data.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User is not signed in.", Toast.LENGTH_SHORT).show();
        }

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDisplay = (currentDisplay + 1) % 3;
                updateDisplay();
            }
        });
        Button homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SpotifyWrappedActivity.this, HomePageActivity.class);
                startActivity(intent);
            }
        });

    }

    private void updateDisplay() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.textViewItem) {

            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(R.id.textViewItem);
                textView.setText((position + 1) + ". " + getItem(position));
                return view;
            }
        };

        switch (currentDisplay) {
            case 0:
                categoryHeader.setText("Top Tracks");
                adapter.addAll(topTracks);

                break;
            case 1:
                categoryHeader.setText("Top Artists");
                adapter.addAll(topArtists);
                break;
            case 2:
                categoryHeader.setText("Top Genres");
                adapter.addAll(topGenres);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + currentDisplay);
        }

        listViewData.setAdapter(adapter);
    }




    public static void playClip(String songMp3File){
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(songMp3File);
            // below line is use to prepare
            // and start our media player.
            mediaPlayer.prepare();
            mediaPlayer.start();
            //ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            //executorService.schedule(MainActivity::stopClip(mediaPlayer),10, TimeUnit.SECONDS);
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            stopClip(mediaPlayer);
                        }
                    },
                    10000
            );


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stopClip(MediaPlayer mediaPlayer){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
    }





}
