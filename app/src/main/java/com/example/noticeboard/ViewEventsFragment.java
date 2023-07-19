package com.example.noticeboard;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewEventsFragment extends Fragment {
    private RecyclerView myEvents;
    private DatabaseReference databaseReference;
    private ArrayList<CreateEventsModal> events;
    private EventsAdapter eventsAdapter;

    public ViewEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_events, container, false);

        myEvents = view.findViewById(R.id.availableEvents);
        myEvents.setHasFixedSize(true);
        myEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        events = new ArrayList<>();

        eventsAdapter = new EventsAdapter(getContext(), events);
        myEvents.setAdapter(eventsAdapter);

        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Loading Events");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        databaseReference = FirebaseDatabase.getInstance().getReference("events");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                events.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    CreateEventsModal myItems = itemSnapshot.getValue(CreateEventsModal.class);
                    events.add(myItems);
                }
                eventsAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Failed to load events.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
