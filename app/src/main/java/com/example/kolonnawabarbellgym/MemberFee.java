package com.example.kolonnawabarbellgym;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.kolonnawabarbellgym.Adapter.SwipeToDeleteCallback;
import com.example.kolonnawabarbellgym.Adapter.UserListAdapter;
import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;
import com.example.kolonnawabarbellgym.DTO.UserModel;

import java.util.ArrayList;
import java.util.List;

public class MemberFee extends BaseActivity implements
        UserListAdapter.OnItemClickListener,
        UserListAdapter.OnItemDeleteListener {

    private EditText etSearch;
    private RecyclerView rvUsers;
    private UserListAdapter adapter;
    private List<UserModel> userList;
    private List<UserModel> filteredList;
    private DatabaseHelperClass databaseHelper;
    private LottieAnimationView feeAnimationView;
    private LinearLayout emptyState;
    private TextView tvMemberCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_fee);

        currentNavItemId = R.id.navigation_fee;
        setupBottomNavigation(R.id.navigation_fee);

        initViews();
        setupRecyclerView();
        loadUsers();
        setupSearch();
        setupSwipeToDelete();

    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        rvUsers = findViewById(R.id.rvUsers);

        emptyState = findViewById(R.id.emptyState);
        tvMemberCount = findViewById(R.id.tvMemberCount);

        databaseHelper = new DatabaseHelperClass(this);
        userList = new ArrayList<>();
        filteredList = new ArrayList<>();
    }



    private void setupRecyclerView() {
        adapter = new UserListAdapter(this, filteredList, this);
        adapter.setOnItemDeleteListener(this);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);

        // Add item animation
        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        rvUsers.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down));
    }

    private void setupSwipeToDelete() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(adapter, this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(rvUsers);
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

        updateEmptyState();
        updateMemberCount();

        if (userList.isEmpty()) {
            showEmptyStateAnimation();
        }
    }

    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvUsers.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvUsers.setVisibility(View.VISIBLE);
        }
    }

    private void updateMemberCount() {
        String countText = filteredList.size() + " member" + (filteredList.size() != 1 ? "s" : "");
        tvMemberCount.setText(countText);
    }

    private void showEmptyStateAnimation() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        emptyState.startAnimation(fadeIn);
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
        updateEmptyState();
        updateMemberCount();

        // Add animation when filtering
        if (!filteredList.isEmpty()) {
            rvUsers.scheduleLayoutAnimation();
        }
    }

    @Override
    public void onItemClick(UserModel user) {
        // Add click animation
        Animation scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);

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

        // Start activity with animation
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onItemDelete(UserModel user) {
        showDeleteConfirmationDialog(user);
    }

    private void showDeleteConfirmationDialog(UserModel user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Member");
        builder.setMessage("Are you sure you want to delete " + user.getFullName() + "? This action cannot be undone and all member data will be permanently deleted.");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUser(user);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Refresh the adapter to reset the swipe
                adapter.updateList(filteredList);
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteUser(UserModel user) {
        // Add delete animation
        Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);

        // Find the position of the user in the filtered list
        int position = -1;
        for (int i = 0; i < filteredList.size(); i++) {
            if (filteredList.get(i).getUniqueId().equals(user.getUniqueId())) {
                position = i;
                break;
            }
        }

        if (position != -1) {
            // Store the position in a final variable for use in inner class
            final int finalPosition = position;

            // Apply fade out animation
            RecyclerView.ViewHolder viewHolder = rvUsers.findViewHolderForAdapterPosition(finalPosition);
            if (viewHolder != null) {
                viewHolder.itemView.startAnimation(fadeOut);
            }

            // Remove from database
            boolean deleted = deleteUserFromDatabase(user.getUniqueId());

            if (deleted) {
                // Remove from lists
                filteredList.remove(finalPosition);

                // Also remove from main list
                for (int i = 0; i < userList.size(); i++) {
                    if (userList.get(i).getUniqueId().equals(user.getUniqueId())) {
                        userList.remove(i);
                        break;
                    }
                }

                // Notify adapter with delay to show animation
                rvUsers.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyItemRemoved(finalPosition);
                        updateEmptyState();
                        updateMemberCount();
                        Toast.makeText(MemberFee.this, "Member deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                }, 300);

            } else {
                Toast.makeText(this, "Failed to delete member", Toast.LENGTH_SHORT).show();
                adapter.updateList(filteredList); // Refresh on failure
            }
        }
    }

    private boolean deleteUserFromDatabase(String uniqueId) {
        try {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            // First delete related payment records
            db.delete("payment", "unique_id = ?", new String[]{uniqueId});

            // Then delete the user
            int result = db.delete("new_users", "unique_id = ?", new String[]{uniqueId});

            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning from fee setting activity
        loadUsers();

        // Restart animation
        if (feeAnimationView != null) {
            feeAnimationView.resumeAnimation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause animation to save resources
        if (feeAnimationView != null) {
            feeAnimationView.pauseAnimation();
        }
    }
}