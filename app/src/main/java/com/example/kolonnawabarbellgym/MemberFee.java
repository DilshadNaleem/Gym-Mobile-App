package com.example.kolonnawabarbellgym;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kolonnawabarbellgym.Adapter.UserListAdapter;
import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;
import com.example.kolonnawabarbellgym.DTO.UserModel;

import java.util.ArrayList;
import java.util.List;

public class MemberFee extends BaseActivity implements UserListAdapter.OnItemClickListener {

    private EditText etSearch;
    private RecyclerView rvUsers;
    private UserListAdapter adapter;
    private List<UserModel> userList;
    private List<UserModel> filteredList;
    private DatabaseHelperClass databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_member_fee);

        currentNavItemId = R.id.navigation_fee;
        setupBottomNavigation(R.id.navigation_fee);

        initViews();
        setupRecyclerView();
        loadUsers();
        setupSearch();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        rvUsers = findViewById(R.id.rvUsers);
        databaseHelper = new DatabaseHelperClass(this);
        userList = new ArrayList<>();
        filteredList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new UserListAdapter(this, filteredList, this);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);
    }

    private void loadUsers() {
        userList.clear();
        Cursor cursor = databaseHelper.getAllNewUsers();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                UserModel user = new UserModel();
                user.setUniqueId(cursor.getString(cursor.getColumnIndexOrThrow("unique_id")));
                user.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow("firstName")));
                user.setLastName(cursor.getString(cursor.getColumnIndexOrThrow("lastName")));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                user.setPhoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("phoneNumber")));
                user.setNic(cursor.getString(cursor.getColumnIndexOrThrow("nic")));
                user.setMonthlyFee(cursor.getString(cursor.getColumnIndexOrThrow("monthlyFee")));
                user.setCreatedTime(cursor.getString(cursor.getColumnIndexOrThrow("created_time")));

                // Get profile image blob
                byte[] imageBlob = cursor.getBlob(cursor.getColumnIndexOrThrow("profileImage"));
                user.setProfileImage(imageBlob);

                userList.add(user);
            } while (cursor.moveToNext());
            cursor.close();
        }

        filteredList.clear();
        filteredList.addAll(userList);
        adapter.updateList(filteredList);

        if (userList.isEmpty()) {
            Toast.makeText(this, "No users found", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterUsers(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(userList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (UserModel user : userList) {
                if (user.getFirstName().toLowerCase().contains(lowerCaseQuery) ||
                        user.getLastName().toLowerCase().contains(lowerCaseQuery) ||
                        user.getEmail().toLowerCase().contains(lowerCaseQuery) ||
                        user.getPhoneNumber().contains(query)) {
                    filteredList.add(user);
                }
            }
        }

        adapter.updateList(filteredList);
    }

    @Override
    public void onItemClick(UserModel user) {
        // Navigate to fee setting activity
        Intent intent = new Intent(MemberFee.this, SetMonthlyFeeActivity.class);
        intent.putExtra("user_unique_id", user.getUniqueId());
        intent.putExtra("user_name", user.getFullName());
        intent.putExtra("user_email", user.getEmail());
        intent.putExtra("current_fee", user.getMonthlyFee() != null ? user.getMonthlyFee() : "");

        String remail = getIntent().getStringExtra("remail");
        if (remail != null) {
            intent.putExtra("remail", remail);
        }
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning from fee setting activity
        loadUsers();
    }
}