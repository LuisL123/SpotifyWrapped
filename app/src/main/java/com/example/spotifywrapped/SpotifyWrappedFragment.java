package com.example.spotifywrapped;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class SpotifyWrappedFragment extends Fragment {

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
    }
}