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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HistoricalListsActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private ListView listViewHistory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historical_lists);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        listViewHistory = findViewById(R.id.listViewHistory);
        Button buttonHome = findViewById(R.id.buttonHome);

        buttonHome.setOnClickListener(v -> {
            Intent intent = new Intent(HistoricalListsActivity.this, HomePageActivity.class);
            startActivity(intent);
        });

        displayHistory();
    }

    private void displayHistory() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            mDatabase.child("users").child(userId).child("fetchHistory")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            List<String> historyList = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Map<String, Object> historyItem = (Map<String, Object>) snapshot.getValue();
                                if (historyItem != null && historyItem.containsKey("timestamp")) {
                                    long timestamp = (long) historyItem.get("timestamp");
                                    String formattedTimestamp = String.valueOf(timestamp);
                                    historyList.add(formattedTimestamp);
                                }
                            }
                            if (!historyList.isEmpty()) {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(HistoricalListsActivity.this,
                                        android.R.layout.simple_list_item_1, historyList);
                                listViewHistory.setAdapter(adapter);
                            } else {
                                Toast.makeText(HistoricalListsActivity.this, "No history available.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(HistoricalListsActivity.this, "Failed to retrieve history: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "User is not signed in.", Toast.LENGTH_SHORT).show();
        }
    }
}
