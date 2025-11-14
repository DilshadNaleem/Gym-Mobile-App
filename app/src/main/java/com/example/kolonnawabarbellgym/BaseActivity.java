package com.example.kolonnawabarbellgym;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;
    protected int currentNavItemId = -1; // Track current navigation item

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Don't set content view here - let child activities do that
    }

    protected void setupBottomNavigation(int selectedItemId) {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(selectedItemId);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                // Don't do anything if already on the selected screen
                if (itemId == currentNavItemId) {
                    return true;
                }

                // Get the current user email from intent
                String userEmail = getIntent().getStringExtra("remail");

                if (itemId == R.id.navigation_dashboard) {
                    Intent intent = new Intent(BaseActivity.this, MainDashboard.class);
                    // Add the email to intent
                    if (userEmail != null) {
                        intent.putExtra("remail", userEmail);
                    }
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_fee) {
                    Intent intent = new Intent(BaseActivity.this, MemberFee.class);
                    // Add the email to intent
                    if (userEmail != null) {
                        intent.putExtra("remail", userEmail);
                    }
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    Intent intent = new Intent(BaseActivity.this, ProfileActivity.class);
                    // Add the email to intent
                    if (userEmail != null) {
                        intent.putExtra("remail", userEmail);
                    }
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_newuser) {
                    Intent intent = new Intent(BaseActivity.this, AddUser.class);
                    if (userEmail != null)
                    {
                        intent.putExtra("remail", userEmail);
                    }
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    finish();
                    return true;
                }
                return false;
            }
        });
    }
}