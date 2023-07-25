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
                    String facultyName = snapshot.child("faculty").getValue(String.class);
                    String course = snapshot.child("course").getValue(String.class);
                    String year = snapshot.child("year").getValue(String.class);

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
                            filterNoticesByFaculty(noticesRef, facultyName, course, year);
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

    private void filterNoticesByFaculty(DatabaseReference noticesRef, String facultyName, String course, String year) {
        if (facultyName == null || facultyName.isEmpty()) {
            // If "Everyone" is selected, set Faculty, Course, and Year to null
            filterNoticesByCourse(noticesRef, null, null, null);
            return;
        }

        // Query faculty-specific notices
        Query facultyNoticesQuery = noticesRef.orderByChild("faculty").equalTo(facultyName);
        facultyNoticesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    if (!noticeIds.contains(itemSnapshot.getKey())) {
                        PostNoticeModal notice = itemSnapshot.getValue(PostNoticeModal.class);
                        if (notice != null && course.equals(notice.getCourse())) {
                            // Check if the notice is meant for the user's course
                            notices.add(notice);
                            noticeIds.add(itemSnapshot.getKey());
                        }
                    }
                }

                if (year == null || year.isEmpty()) {
                    // If "Post for Faculty and Course" is selected, both Faculty and Course are set, and Year is null
                    noticeAdapter.notifyDataSetChanged();
                } else {
                    filterNoticesByYear(noticesRef, facultyName, course, year);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to retrieve faculty-specific notices", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void filterNoticesByCourse(DatabaseReference noticesRef, String facultyName, String course, String year) {
        if (course == null || course.isEmpty()) {
            // If "Post for Course in a Faculty" is selected, both Faculty and Course are set, and Year is null
            noticeAdapter.notifyDataSetChanged();
            return;
        }

        // Query course-specific notices
        Query courseNoticesQuery = noticesRef.orderByChild("course").equalTo(course);
        courseNoticesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    if (!noticeIds.contains(itemSnapshot.getKey())) {
                        PostNoticeModal notice = itemSnapshot.getValue(PostNoticeModal.class);
                        if (notice != null && notice.getFaculty().equals(facultyName)) {
                            // Check if the notice is meant for the user's faculty
                            notices.add(notice);
                            noticeIds.add(itemSnapshot.getKey());
                        }
                    }
                }

                if (year == null || year.isEmpty()) {
                    // If "Post for Course in a Faculty" is selected, both Faculty and Course are set, and Year is null
                    noticeAdapter.notifyDataSetChanged();
                } else {
                    filterNoticesByYear(noticesRef, facultyName, course, year);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to retrieve course-specific notices", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterNoticesByYear(DatabaseReference noticesRef, String facultyName, String course, String year) {
        if (year == null || year.isEmpty()) {
            // If "Post for Faculty, Course, and Year" is selected, all three values are set
            noticeAdapter.notifyDataSetChanged();
            return;
        }

        // Query year-specific notices
        Query yearNoticesQuery = noticesRef.orderByChild("year").equalTo(year);
        yearNoticesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    if (!noticeIds.contains(itemSnapshot.getKey())) {
                        PostNoticeModal notice = itemSnapshot.getValue(PostNoticeModal.class);
                        if (notice != null && notice.getFaculty().equals(facultyName) && notice.getCourse().equals(course)) {
                            // Check if the notice is meant for the user's faculty and course
                            notices.add(notice);
                            noticeIds.add(itemSnapshot.getKey());
                        }
                    }
                }
                // Sort the notices list based on date and time (newest on top)
                sortNoticesByDateTime(notices);
                noticeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to retrieve year-specific notices", Toast.LENGTH_SHORT).show();
            }
        });
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
