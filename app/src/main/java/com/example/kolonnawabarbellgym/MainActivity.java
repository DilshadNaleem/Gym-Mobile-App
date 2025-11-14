package com.example.kolonnawabarbellgym;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private static final int SPLASH_DELAY = 5000;
    private DatabaseHelperClass dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.pbmain);

        dbHelper = new DatabaseHelperClass(this);
        //deleteAllUserData();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_DELAY);
    }

//    //private void deleteAllUserData()
//    {
//        try
//        {
//            SQLiteDatabase db = dbHelper.getWritableDatabase();
//            db.delete("users", null, null);
//
//            db.close();
//            Toast.makeText(this, "All user data deleted", Toast.LENGTH_SHORT).show();
//        }
//
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            Toast.makeText(this, "Error deleting data", Toast.LENGTH_SHORT).show();
//        }
//    }
}