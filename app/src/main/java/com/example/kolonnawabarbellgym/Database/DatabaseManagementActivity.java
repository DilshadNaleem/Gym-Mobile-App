package com.example.kolonnawabarbellgym.Database;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.kolonnawabarbellgym.R;
import com.example.kolonnawabarbellgym.Utils.FileUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseManagementActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int PICK_DB_FILE_REQUEST = 101;
    private static final int MANAGE_STORAGE_REQUEST_CODE = 102;

    private DatabaseHelperClass databaseHelper;
    private Button btnBackup, btnImportMerge;
    private TextView tvTotalMembers, tvDbSize, tvLastBackup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_management);

        databaseHelper = new DatabaseHelperClass(this);

        initializeViews();
        setupClickListeners();
        updateStatus();
    }

    private void initializeViews() {
        btnBackup = findViewById(R.id.btnBackup);
        btnImportMerge = findViewById(R.id.btnImportMerge);
        tvTotalMembers = findViewById(R.id.tvTotalMembers);
        tvDbSize = findViewById(R.id.tvDbSize);
        tvLastBackup = findViewById(R.id.tvLastBackup);
    }

    private void setupClickListeners() {
        btnBackup.setOnClickListener(v -> createBackup());
        btnImportMerge.setOnClickListener(v -> importAndMerge());
    }

    private void updateStatus() {
        new StatusUpdateTask(this).execute();
    }

    private void createBackup() {
        if (checkPermissions()) {
            new BackupTask(this).execute();
        } else {
            requestPermissions();
        }
    }

    private void importAndMerge() {
        if (checkPermissions()) {
            openFilePicker();
        } else {
            requestPermissions();
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_DB_FILE_REQUEST);
    }

    // Updated permission checking
    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ - Check for MANAGE_EXTERNAL_STORAGE
            return Environment.isExternalStorageManager();
        } else {
            // Android 10 and below - Check for WRITE_EXTERNAL_STORAGE
            return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ - Request MANAGE_EXTERNAL_STORAGE
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getPackageName())));
                startActivityForResult(intent, MANAGE_STORAGE_REQUEST_CODE);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, MANAGE_STORAGE_REQUEST_CODE);
            }
        } else {
            // Android 10 and below - Request WRITE_EXTERNAL_STORAGE
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_DB_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            // Take persistable URI permission
            if (uri != null) {
                getContentResolver().takePersistableUriPermission(uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            String filePath = FileUtils.getPath(this, uri);
            if (filePath != null) {
                showMergeConfirmationDialog(filePath);
            } else {
                // Try using URI directly if path is null
                showMergeConfirmationDialog(uri.toString());
            }
        } else if (requestCode == MANAGE_STORAGE_REQUEST_CODE) {
            // Check if permission was granted after MANAGE_EXTERNAL_STORAGE request
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "Storage permission granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Storage permission denied!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showMergeConfirmationDialog(String filePath) {
        new AlertDialog.Builder(this)
                .setTitle("Merge Databases")
                .setMessage("This will:\n\n1. Create backup of your current database\n2. Merge with imported database\n3. Keep all unique members from both databases")
                .setPositiveButton("Continue", (dialog, which) -> {
                    new MergeTask(this).execute(filePath);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Updated Backup Task to use app-specific directory
    private static class BackupTask extends AsyncTask<Void, Void, String> {
        private WeakReference<DatabaseManagementActivity> activityReference;
        private ProgressDialog progressDialog;

        BackupTask(DatabaseManagementActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            DatabaseManagementActivity activity = activityReference.get();
            if (activity != null) {
                progressDialog = new ProgressDialog(activity);
                progressDialog.setMessage("Creating backup...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            DatabaseManagementActivity activity = activityReference.get();
            if (activity == null) return null;

            try {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String fileName = "gym_backup_" + timeStamp + ".db";

                // Use app-specific directory (no permissions needed)
                File exportDir = new File(activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "GymBackups");
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }

                File backupFile = new File(exportDir, fileName);
                boolean success = activity.databaseHelper.exportDatabase(backupFile.getAbsolutePath());

                return success ? backupFile.getAbsolutePath() : null;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            DatabaseManagementActivity activity = activityReference.get();
            if (activity != null) {
                if (result != null) {
                    activity.shareBackupFile(result);
                    activity.updateStatus();
                    Toast.makeText(activity, "Backup created successfully!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(activity, "Backup failed!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Updated Merge Task
    private static class MergeTask extends AsyncTask<String, Void, Boolean> {
        private WeakReference<DatabaseManagementActivity> activityReference;
        private ProgressDialog progressDialog;

        MergeTask(DatabaseManagementActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            DatabaseManagementActivity activity = activityReference.get();
            if (activity != null) {
                progressDialog = new ProgressDialog(activity);
                progressDialog.setMessage("Merging databases...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        }

        @Override
        protected Boolean doInBackground(String... paths) {
            DatabaseManagementActivity activity = activityReference.get();
            if (activity == null) return false;

            try {
                // First create backup
                String backupPath = activity.createSafetyBackup();
                if (backupPath == null) {
                    return false; // Backup failed
                }

                // Then perform merge
                boolean mergeSuccess = activity.databaseHelper.mergeDatabases(paths[0]);

                if (!mergeSuccess) {
                    // Restore from backup if merge fails
                    activity.databaseHelper.importDatabase(backupPath);
                }

                return mergeSuccess;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            DatabaseManagementActivity activity = activityReference.get();
            if (activity != null) {
                if (success) {
                    activity.updateStatus();
                    Toast.makeText(activity, "Databases merged successfully!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(activity, "Merge failed! Data restored from backup.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    // Status Update Task (unchanged)
    private static class StatusUpdateTask extends AsyncTask<Void, Void, DatabaseHelperClass.StatusData> {
        private WeakReference<DatabaseManagementActivity> activityReference;

        StatusUpdateTask(DatabaseManagementActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected DatabaseHelperClass.StatusData doInBackground(Void... voids) {
            DatabaseManagementActivity activity = activityReference.get();
            if (activity == null) return null;

            return activity.databaseHelper.getStatusData();
        }

        @Override
        protected void onPostExecute(DatabaseHelperClass.StatusData statusData) {
            DatabaseManagementActivity activity = activityReference.get();
            if (activity != null && statusData != null) {
                activity.tvTotalMembers.setText(String.valueOf(statusData.totalMembers));
                activity.tvDbSize.setText(statusData.dbSize);
                activity.tvLastBackup.setText(statusData.lastBackup);
            }
        }
    }

    private String createSafetyBackup() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "safety_backup_" + timeStamp + ".db";

            // Use app-specific directory
            File backupDir = new File(getExternalFilesDir(null), "safety_backups");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            File backupFile = new File(backupDir, fileName);
            return databaseHelper.exportDatabase(backupFile.getAbsolutePath()) ? backupFile.getAbsolutePath() : null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void shareBackupFile(String filePath) {
        File file = new File(filePath);
        Uri fileUri = FileProvider.getUriForFile(this,
                getApplicationContext().getPackageName() + ".provider",
                file);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.setType("application/octet-stream");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "Share Database Backup"));
    }
}