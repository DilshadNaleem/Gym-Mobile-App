package com.example.kolonnawabarbellgym;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainDashboard extends BaseActivity {

    private String userEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_dashboard);

        userEmail = getIntent().getStringExtra("remail");
        Log.d("MainDashboard", "User email: " + userEmail);

        currentNavItemId = R.id.navigation_dashboard;
        setupBottomNavigation(R.id.navigation_dashboard);
    }

}