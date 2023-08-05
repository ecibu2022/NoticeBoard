package com.example.noticeboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {
    private RelativeLayout relative_layout;
    private TextView textView;
    private Animation layoutAnimation, textViewAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hideStatusBar();

        relative_layout=findViewById(R.id.relative_layout);
        textView=findViewById(R.id.textView);
        layoutAnimation= AnimationUtils.loadAnimation(MainActivity.this, R.anim.fall_down);
        textViewAnimation= AnimationUtils.loadAnimation(MainActivity.this, R.anim.bottom_to_top);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                relative_layout.setVisibility(View.VISIBLE);;
                relative_layout.setAnimation(layoutAnimation);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textView.setVisibility(View.VISIBLE);;
                        textView.setAnimation(textViewAnimation);
                    }
                }, 100);

            }
        }, 500);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, Login.class));
                finish();
            }
        }, 6000);

    }

    private void hideStatusBar() {
        // Set the activity to full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide the status bar on Android versions >= 16 (API level 16)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            View decorView = getWindow().getDecorView();
            int systemUiVisibilityFlags = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(systemUiVisibilityFlags);
        }
    }

}