package com.example.wrappedspotify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class HistoricalListsActivity extends AppCompatActivity {

    private ListView listViewHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historical_lists);

        listViewHistory = findViewById(R.id.listViewHistory);
        loadFetchHistory();
        Button homeButton = findViewById(R.id.buttonHome);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(HistoricalListsActivity.this, HomePageActivity.class);
            startActivity(intent);
        });
    }

    private void loadFetchHistory() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference fetchHistoryRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("fetchHistory");
            fetchHistoryRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ArrayList<String> fetchHistoryList = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        HashMap<String, Object> fetch = (HashMap<String, Object>) snapshot.getValue();
                        Long fetchDate = (Long) fetch.get("fetchDate");
                        String dateString = formatDate(fetchDate);
                        fetchHistoryList.add("Fetch Date: " + dateString);
                    }
                    updateListView(fetchHistoryList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(HistoricalListsActivity.this, "Failed to load history: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User is not signed in.", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatDate(Long milliseconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        return formatter.format(new Date(milliseconds));
    }

    private void updateListView(ArrayList<String> data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
        listViewHistory.setAdapter(adapter);
    }
}

