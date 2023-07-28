package com.example.noticeboard;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class UserHomeFragment extends Fragment {
    private RecyclerView myNotices;
    private DatabaseReference databaseReference;
    private List<PostNoticeModal> notices;
    private NoticeAdapter noticeAdapter;
    private Set<String> noticeIds;
    private String facultyName, course,year;

    public UserHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Enable menu in the fragment
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_home, container, false);

        setHasOptionsMenu(true);

        myNotices = view.findViewById(R.id.myNotices);
        myNotices.setHasFixedSize(true);
        myNotices.setLayoutManager(new LinearLayoutManager(getContext()));

        notices = new ArrayList<>();
        noticeAdapter = new NoticeAdapter(getContext(), (ArrayList<PostNoticeModal>) notices);
        myNotices.setAdapter(noticeAdapter);

        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Loading Notices");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        noticeIds = new HashSet<>();

        String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserID);
        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    facultyName = snapshot.child("faculty").getValue(String.class);
                    course = snapshot.child("course").getValue(String.class);
                    year = snapshot.child("year").getValue(String.class);

                    DatabaseReference noticesRef = FirebaseDatabase.getInstance().getReference("approved_notices");

                    // Query notices accessible to everyone
                    Query everyoneNoticesQuery = noticesRef.orderByChild("everyone").equalTo("Everyone");
                    everyoneNoticesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                                PostNoticeModal notice = itemSnapshot.getValue(PostNoticeModal.class);
                                if (notice != null && !noticeIds.contains(itemSnapshot.getKey())) {
                                    notices.add(notice);
                                    noticeIds.add(itemSnapshot.getKey());
                                }
                            }
                            filterNoticesByFaculty(noticesRef, facultyName, course, year); // Pass course and year variables
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Failed to retrieve notices for everyone", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });

                } else {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Error retrieving user data", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    // Modify the method filterNoticesByFaculty as follows:
    private void filterNoticesByFaculty(DatabaseReference noticesRef, String facultyName, String course, String year) {
        // Create a new list to collect the filtered notices
        List<PostNoticeModal> filteredNotices = new ArrayList<>();

        // Query notices accessible to everyone
        Query everyoneNoticesQuery = noticesRef.child("Everyone").orderByChild("everyone").equalTo("Everyone");
        everyoneNoticesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    PostNoticeModal notice = itemSnapshot.getValue(PostNoticeModal.class);
                    if (notice != null && !noticeIds.contains(itemSnapshot.getKey())) {
                        // Check if the notice is accessible to everyone
                        filteredNotices.add(notice);
                        noticeIds.add(itemSnapshot.getKey());
                    }
                }

                // If facultyName is not null or empty, then filter faculty-specific notices
                if (facultyName != null && !facultyName.isEmpty()) {
                    // Add the notices accessible to everyone to the filteredNotices list before filtering faculty-specific notices
                    filteredNotices.addAll(notices); // Add all existing notices to the filteredNotices list
                    filterFacultySpecificNotices(noticesRef, facultyName, filteredNotices);
                } else {
                    // If facultyName is null or empty, display all filtered notices
                    displayFilteredNotices(filteredNotices);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to retrieve notices for everyone", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Add a new method to filter faculty-specific notices:
    private void filterFacultySpecificNotices(DatabaseReference noticesRef, String facultyName, List<PostNoticeModal> filteredNotices) {
        // Query faculty-specific notices
        Query facultyNoticesQuery = noticesRef.orderByChild("faculty").equalTo(facultyName);
        facultyNoticesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    PostNoticeModal notice = itemSnapshot.getValue(PostNoticeModal.class);
                    if (notice != null && !noticeIds.contains(itemSnapshot.getKey())) {
                        // Check if the notice is meant for the user's faculty
                        filteredNotices.add(notice);
                        noticeIds.add(itemSnapshot.getKey());
                    }
                }

                // If course is not null or empty, then filter course-specific notices
                if (course != null && !course.isEmpty()) {
                    filterCourseSpecificNotices(noticesRef, facultyName, course, filteredNotices);
                } else {
                    // If course is null or empty, display all filtered notices
                    displayFilteredNotices(filteredNotices);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to retrieve faculty-specific notices", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Add a new method to filter course-specific notices:
    private void filterCourseSpecificNotices(DatabaseReference noticesRef, String facultyName, String course, List<PostNoticeModal> filteredNotices) {
        // Query course-specific notices
        Query courseNoticesQuery = noticesRef.child("Course").orderByChild("course").equalTo(course);
        courseNoticesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    PostNoticeModal notice = itemSnapshot.getValue(PostNoticeModal.class);
                    if (notice != null && !noticeIds.contains(itemSnapshot.getKey()) && notice.getFaculty().equals(facultyName)) {
                        // Check if the notice is meant for the user's faculty and course
                        filteredNotices.add(notice);
                        noticeIds.add(itemSnapshot.getKey());
                    }
                }

                // If year is not null or empty, then filter year-specific notices
                if (year != null && !year.isEmpty()) {
                    filterYearSpecificNotices(noticesRef, facultyName, course, year, filteredNotices);
                } else {
                    // If year is null or empty, display all filtered notices
                    displayFilteredNotices(filteredNotices);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to retrieve course-specific notices", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Add a new method to filter year-specific notices:
    private void filterYearSpecificNotices(DatabaseReference noticesRef, String facultyName, String course, String year, List<PostNoticeModal> filteredNotices) {
        // Query year-specific notices
        Query yearNoticesQuery = noticesRef.child("Year").orderByChild("year").equalTo(year);
        yearNoticesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    PostNoticeModal notice = itemSnapshot.getValue(PostNoticeModal.class);
                    if (notice != null && !noticeIds.contains(itemSnapshot.getKey()) && notice.getFaculty().equals(facultyName) && notice.getCourse().equals(course)) {
                        // Check if the notice is meant for the user's faculty, course, and year
                        filteredNotices.add(notice);
                        noticeIds.add(itemSnapshot.getKey());
                    }
                }
                // Display all filtered notices
                displayFilteredNotices(filteredNotices);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to retrieve year-specific notices", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Add a new method to display the filtered notices:
    private void displayFilteredNotices(List<PostNoticeModal> filteredNotices) {
        // Clear the existing notices list and add the filtered notices
        notices.clear();
        notices.addAll(filteredNotices);
        // Sort the notices list based on date and time (newest on top)
        sortNoticesByDateTime(notices);
        noticeAdapter.notifyDataSetChanged();
    }

    //Retrieving the notices based on newest first
    private void sortNoticesByDateTime(List<PostNoticeModal> notices) {
        // Sort the notices in descending order based on date and time (newest to oldest)
        Collections.sort(notices, new Comparator<PostNoticeModal>() {
            @Override
            public int compare(PostNoticeModal notice1, PostNoticeModal notice2) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                try {
                    Date date1 = sdf.parse(notice1.getDateTime());
                    Date date2 = sdf.parse(notice2.getDateTime());
                    // Sorting in descending order (newest to oldest)
                    return date2.compareTo(date1);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Inflate menu with items using MenuInflater
        inflater.inflate(R.menu.search_bar_menu, menu);

        MenuItem searchViewItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) searchViewItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle query submission
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle query text change
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }
}