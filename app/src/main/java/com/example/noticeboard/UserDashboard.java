package com.example.noticeboard;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
    private ImageView userImageView;
    private View headerView;
    private TextView userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        mAuth=FirebaseAuth.getInstance();

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the title for the action bar
        getSupportActionBar().setTitle("Home");

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

//        Retrieving Users Data in nav view
        headerView = navigationView.getHeaderView(0);
        userImageView = headerView.findViewById(R.id.circle_image_view);
        userName = headerView.findViewById(R.id.full_name);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference usersRef = database.getReference("users");
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the user's image URL and name
                    String imageUrl = dataSnapshot.child("profileImage").getValue(String.class);
                    String fullName = dataSnapshot.child("fullName").getValue(String.class);

                    // Setting the user's image and name in the navigation drawer layout
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(UserDashboard.this).load(imageUrl).into(userImageView);
                    }
                    userName.setText(fullName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occurred while retrieving the user's data
            }
        });

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {
                usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Retrieve the user's image URL and name
                            String imageUrl = dataSnapshot.child("profileImage").getValue(String.class);
                            String fullName = dataSnapshot.child("fullName").getValue(String.class);

                            // Setting the user's image and name in the navigation drawer layout
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(UserDashboard.this).load(imageUrl).into(userImageView);
                            }
                            userName.setText(fullName);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle any errors that occurred while retrieving the user's data
                    }
                });

            }
        });

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.home:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new UserHomeFragment())
                        .commit();
                getSupportActionBar().setTitle("Home");
                break;

            case R.id.edit:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new EditProfileFragment()).addToBackStack(String.valueOf(R.id.home))
                        .commit();
                getSupportActionBar().setTitle("User Profile");
                break;

            case R.id.post:
                getSupportFragmentManager().beginTransaction().addToBackStack(String.valueOf(R.id.home))
                        .replace(R.id.fragment_container, new PostNoticeFragment())
                        .commit();
                getSupportActionBar().setTitle("Post Notice");
                break;

            case R.id.events:
                showEventsOptionsDialog();
                break;

            case R.id.trends:
                getSupportFragmentManager().beginTransaction().addToBackStack(String.valueOf(R.id.home))
                        .replace(R.id.fragment_container, new TrendsFragment())
                        .commit();
                getSupportActionBar().setTitle("Trends");
                break;

            case R.id.suggestions:
                getSupportFragmentManager().beginTransaction().addToBackStack(String.valueOf(R.id.home))
                        .replace(R.id.fragment_container, new UserSuggestionBoxFragment())
                        .commit();
                getSupportActionBar().setTitle("Suggestion Box");
                break;

            case R.id.faqs:
                getSupportFragmentManager().beginTransaction().addToBackStack(String.valueOf(R.id.home))
                        .replace(R.id.fragment_container, new FAQsFragment())
                        .commit();
                getSupportActionBar().setTitle("FAQs");
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
    
//    Events Options Dialog
private void showEventsOptionsDialog() {
    String[] options = {"View", "Create"};

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("****EVENTS****");
    builder.setItems(options, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == 0) {
                getSupportFragmentManager().beginTransaction().addToBackStack(String.valueOf(R.id.home))
                        .replace(R.id.fragment_container, new ViewEventsFragment())
                        .commit();
                getSupportActionBar().setTitle("Events");
            } else if (which == 1) {
                getSupportFragmentManager().beginTransaction().addToBackStack(String.valueOf(R.id.home))
                        .replace(R.id.fragment_container, new EventsFragment())
                        .commit();
                getSupportActionBar().setTitle("Create Event");
            }
        }
    });
    builder.show();
}

//Going back to Login
@Override
public void onBackPressed() {
    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
        drawerLayout.closeDrawer(GravityCompat.START);
    } else {
        // Check if the current fragment is the HomeFragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof UserHomeFragment) {
            // Navigate to the LoginActivity
            navigateToLogin();
        } else {
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