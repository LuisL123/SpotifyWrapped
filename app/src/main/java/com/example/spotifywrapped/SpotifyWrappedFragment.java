package com.example.spotifywrapped;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class SpotifyWrappedFragment extends Fragment {

    public static final String CLIENT_ID = "df4f62a1fbaa453a82df5932ec64368b";
    public static final String REDIRECT_URI = "com.example.rawspotify://auth";

    public static final int AUTH_TOKEN_REQUEST_CODE = 0;
    public static final int AUTH_CODE_REQUEST_CODE = 1;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String mAccessToken, mAccessCode;
    private Call mCall;

    Button spotifyWrappedBtn;
    private ListView lvHomePage;
    private TextView tokenTextView, codeTextView, profileTextView, topArtistTextView,
            topSongTextView, topGenreTextView, topSongsIDTV;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_spotify_wrapped, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the ImageView by its ID
        ImageView iv_background = view.findViewById(R.id.iv_background);

        // Assuming iv_background has been assigned an AnimationDrawable as background
        // either through XML or programmatically before this point
        if (iv_background.getDrawable() instanceof AnimationDrawable) {
            AnimationDrawable animationDrawable = (AnimationDrawable) iv_background.getDrawable();
            animationDrawable.start();
        }
        String[] list = new String[]{getTopArtists()};
        lvHomePage = view.findViewById(R.id.listView);
        lvHomePage.setAdapter(new ArrayAdapter<String>(view.getContext(), android.
                R.layout.simple_list_item_1 , list));
    }

    public String getTopArtists() {
        final String[] list = {new String("empty")};
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/artists")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();
        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final JSONObject jsonObject = new JSONObject(response.body().string());
                    String artist = jsonObject.toString();
                    //setTextAsync(jsonObject.toString(3), topArtistTextView);
                    String[] topArtists = new String[5];
                    String one = artist.substring(artist.indexOf("\"name\":")  + 8,
                            artist.indexOf("\",\"popularity\":"));
                    topArtists[0] = one;

                    int i = artist.indexOf("\"name\":") + 1;
                    int j = artist.indexOf("\",\"popularity\":") + 1;
                    for(int index = 1; index < 5; index++){
                        topArtists[index] = artist.substring(artist.indexOf("\"name\"", i) + 8,
                                artist.indexOf("\",\"popularity\":", j));
                        i = artist.indexOf("\"name\"", i) + 1;
                        j = artist.indexOf("\",\"popularity\":", j) + 1;
                    }
                    list[0] = "Top artists: " + topArtists[0] + ", " + topArtists[1] + ", "
                            + topArtists[2] + ", " + topArtists[3] + ", " + topArtists[4];
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);

                }
            }
        });

        return list[0];
    }


    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }


}