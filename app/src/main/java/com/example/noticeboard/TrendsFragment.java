package com.example.noticeboard;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TrendsFragment extends Fragment {
    private DatabaseReference approvedNoticesRef;
    private List<PostNoticeModal> trendingNoticesList;
    private RecyclerView trendingRecyclerView;
    private TrendsAdapter trendingAdapter;

    public TrendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trends, container, false);

        // Initialize Firebase Realtime Database reference
        approvedNoticesRef = FirebaseDatabase.getInstance().getReference("approved_notices");

        // Initialize the RecyclerView and its adapter
        trendingNoticesList = new ArrayList<>();
        trendingRecyclerView = view.findViewById(R.id.trendsRecyclerView);
        trendingRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        trendingAdapter = new TrendsAdapter(trendingNoticesList, getActivity());
        trendingRecyclerView.setAdapter(trendingAdapter);

        // Load trending notices
        loadTrendingNotices();

        return view;
    }

    private void loadTrendingNotices() {
        approvedNoticesRef.orderByChild("likeCount").startAt(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                trendingNoticesList.clear();
                for (DataSnapshot noticeSnapshot : snapshot.getChildren()) {
                    PostNoticeModal notice = noticeSnapshot.getValue(PostNoticeModal.class);
                    if (notice != null) {
                        trendingNoticesList.add(notice);
                    }
                }
                trendingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database read error
            }
        });
    }

}