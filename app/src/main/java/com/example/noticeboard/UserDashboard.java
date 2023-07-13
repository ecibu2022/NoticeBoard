package com.example.noticeboard;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserDashboard extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        mAuth=FirebaseAuth.getInstance();

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the title for the action bar
        getSupportActionBar().setTitle("User Dashboard");

        drawerLayout=findViewById(R.id.drawer_layout);
        NavigationView navigationView=findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new UserHomeFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.home);
        }


    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.home:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new UserHomeFragment())
                        .commit();
                break;

            case R.id.edit:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new EditProfileFragment()).addToBackStack(String.valueOf(R.id.home))
                        .commit();
                break;

            case R.id.settings:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SettingsFragment()).addToBackStack(String.valueOf(R.id.home))
                        .commit();
                break;

            case R.id.post:
                getSupportFragmentManager().beginTransaction().addToBackStack(String.valueOf(R.id.home))
                        .replace(R.id.fragment_container, new PostNoticeFragment())
                        .commit();
                break;

            case R.id.events:
                getSupportFragmentManager().beginTransaction().addToBackStack(String.valueOf(R.id.home))
                        .replace(R.id.fragment_container, new EventsFragment())
                        .commit();
                break;

            case R.id.logout:
                logoutDialog();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logoutDialog(){
        AlertDialog.Builder logout=new AlertDialog.Builder(this);
        logout.setTitle("Logging Out?");
        logout.setMessage("Please Confirm!");
        logout.setCancelable(false);
        logout.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAuth.signOut();
                startActivity(new Intent(UserDashboard.this, Login.class));
                finish();
            }
        });
        logout.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(UserDashboard.this, "Logout Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
        logout.show(); // Show the AlertDialog
    }

}