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

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

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

        // Handle notifications switch
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Handle notification settings
            // Code for enabling/disabling notifications
        });

        // Handle sign out
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");
    }

    private void logout() {
        // Clear user-related data from SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("username");
        editor.remove("profileImageUri");
        editor.apply();

        // Navigate to the login screen or home screen
        Intent intent = new Intent(this, LoginActivity.class); // Change LoginActivity with your login activity class
        startActivity(intent);
        finish(); // Close the current activity
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
