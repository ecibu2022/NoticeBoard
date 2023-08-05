package com.example.noticeboard;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class UserHomeFragment extends Fragment {
    private RecyclerView myNotices;
    private DatabaseReference noticesRef, currentUserRef;
    private List<PostNoticeModal> notices;
    private NoticeAdapter noticeAdapter;
    private String userFaculty, userCourse, userYear, noticeID;

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

        String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentUserRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserID);
        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userFaculty = snapshot.child("faculty").getValue(String.class);
                    userCourse = snapshot.child("course").getValue(String.class);
                    userYear = snapshot.child("year").getValue(String.class);

                    noticesRef = FirebaseDatabase.getInstance().getReference("approved_notices");
                    noticeID= noticesRef.getKey();

                    // Filter based on everyone
                    Query everyoneQuery = noticesRef.orderByChild("everyone").equalTo("Everyone");

                    // Filter based on faculty
                    Query facultyQuery = noticesRef.orderByChild("faculty").equalTo(userFaculty);

                    // Filter based on faculty
                    Query courseQuery = noticesRef.orderByChild("course").equalTo(userFaculty+"_"+userCourse);

                    // Filter based on faculty
                    Query yearQuery = noticesRef.orderByChild("year").equalTo(userFaculty+"_"+userCourse+"_"+userYear);

                    everyoneQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            HashMap<String, PostNoticeModal> noticesMap = new HashMap<>();

                            for (DataSnapshot noticeSnapshot : dataSnapshot.getChildren()) {
                                PostNoticeModal notice = noticeSnapshot.getValue(PostNoticeModal.class);
                                noticesMap.put(noticeSnapshot.getKey(), notice);
                            }
                            List<PostNoticeModal> sortedNotices = new ArrayList<>(noticesMap.values());
                            sortNoticesByDateTime(sortedNotices);

                            // Update your adapter with the sorted notices
                            notices.addAll(sortedNotices);
                            noticeAdapter.notifyDataSetChanged();
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            progressDialog.dismiss();
                        }
                    });

                    facultyQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            HashMap<String, PostNoticeModal> noticesMap = new HashMap<>();

                            for (DataSnapshot noticeSnapshot : dataSnapshot.getChildren()) {
                                PostNoticeModal notice = noticeSnapshot.getValue(PostNoticeModal.class);
                                noticesMap.put(noticeSnapshot.getKey(), notice);
                            }
                            List<PostNoticeModal> sortedNotices = new ArrayList<>(noticesMap.values());
                            sortNoticesByDateTime(sortedNotices);

                            // Update your adapter with the sorted notices
                            notices.addAll(sortedNotices);
                            noticeAdapter.notifyDataSetChanged();
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            progressDialog.dismiss();
                        }
                    });

                    courseQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            HashMap<String, PostNoticeModal> noticesMap = new HashMap<>();

                            for (DataSnapshot noticeSnapshot : dataSnapshot.getChildren()) {
                                PostNoticeModal notice = noticeSnapshot.getValue(PostNoticeModal.class);
                                noticesMap.put(noticeSnapshot.getKey(), notice);
                            }
                            List<PostNoticeModal> sortedNotices = new ArrayList<>(noticesMap.values());
                            sortNoticesByDateTime(sortedNotices);

                            // Update your adapter with the sorted notices
                            notices.addAll(sortedNotices);
                            noticeAdapter.notifyDataSetChanged();
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            progressDialog.dismiss();
                        }
                    });

                    yearQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            HashMap<String, PostNoticeModal> noticesMap = new HashMap<>();

                            for (DataSnapshot noticeSnapshot : dataSnapshot.getChildren()) {
                                PostNoticeModal notice = noticeSnapshot.getValue(PostNoticeModal.class);
                                noticesMap.put(noticeSnapshot.getKey(), notice);
                            }
                            List<PostNoticeModal> sortedNotices = new ArrayList<>(noticesMap.values());
                            sortNoticesByDateTime(sortedNotices);

                            // Update your adapter with the sorted notices
                            notices.addAll(sortedNotices);
                            noticeAdapter.notifyDataSetChanged();
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            progressDialog.dismiss();
                        }
                    });

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
                // Filter notices based on the search query
                Log.d("Search", "Query: " + newText);
                List<PostNoticeModal> filteredNotices = filterNoticesByTitle(notices, newText);
                noticeAdapter.setFilteredList(filteredNotices);
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private List<PostNoticeModal> filterNoticesByTitle(List<PostNoticeModal> notices, String query) {
        Log.d("Search", "Filtering: " + query);
        query = query.toLowerCase(Locale.getDefault());
        List<PostNoticeModal> filteredNotices = new ArrayList<>();

        for (PostNoticeModal notice : notices) {
            if (notice.getTitle().toLowerCase(Locale.getDefault()).contains(query)) {
                filteredNotices.add(notice);
            }
        }

        return filteredNotices;
    }


}

