package com.example.noticeboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class OfficialDashboard extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{
    BottomNavigationView bottomNavigationView;
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_official_dashboard);

//        Hiding Status bar
        hideStatusBar();

        bottomNavigationView=findViewById(R.id.bottomNavigationView);
        drawerLayout=findViewById(R.id.drawer_layout);

        //        Makes Home active when back button is clicked either in settings or profile it goes back to home
        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, homeFragment).commit();

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.home);
    }

    OfficialsHomeFragment homeFragment=new OfficialsHomeFragment();
    OfficialsPostNoticeFragment postNoticeFragment=new OfficialsPostNoticeFragment();
    SuggestionsFragment suggestionsFragment=new SuggestionsFragment();
    OfficialsProfileFragment accountFragment=new OfficialsProfileFragment();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.home:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, homeFragment)
                        .addToBackStack(null)
                        .commit();
                return true;

            case R.id.post:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, postNoticeFragment)
                        .addToBackStack(null)
                        .commit();
                return true;

            case R.id.suggestions:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, suggestionsFragment)
                        .addToBackStack(null)
                        .commit();
                return true;

            case R.id.profile:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, accountFragment)
                        .addToBackStack(null)
                        .commit();
                return true;
        }

        return false;
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

    //Going back to Login
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Check if the current fragment is the AdminHomeFragment
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.flFragment);
            if (currentFragment instanceof OfficialsHomeFragment) {
                // If it is, navigate to the LoginActivity
                navigateToLogin();
            } else {
                // If it's not, perform the default back button behavior
                super.onBackPressed();
            }
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
