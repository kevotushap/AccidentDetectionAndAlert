package com.example.accidentdetectionandalert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private ImageView profileImageView;
    private TextView nameTextView;
    private RadioGroup themeRadioGroup;
    private Switch notificationsSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        // Initialize views
        profileImageView = findViewById(R.id.profile_image);
        nameTextView = findViewById(R.id.name_text_view);
        themeRadioGroup = findViewById(R.id.radio_group_theme);
        notificationsSwitch = findViewById(R.id.switch_notifications);
        Button signOutButton = findViewById(R.id.btn_sign_out);

        // Load and display user's profile image and name
        String profileImageUriString = sharedPreferences.getString("profileImageUri", null);
        Uri profileImageUri = (profileImageUriString != null) ? Uri.parse(profileImageUriString) : null;
        profileImageView.setImageURI(profileImageUri);
        nameTextView.setText(sharedPreferences.getString("username", ""));

        // Handle theme selection
        themeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_light) {
                    // Set light theme
                    setTheme(R.style.AppTheme_Light);
                } else if (checkedId == R.id.radio_dark) {
                    // Set dark theme
                    setTheme(R.style.AppTheme_Dark);
                }
            }
        });

    }