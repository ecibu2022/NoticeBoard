package com.example.noticeboard;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
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
import java.util.Calendar;

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
                // Schedule alarms after updating the events list
                scheduleEventAlarms();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Failed to load events.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }


    private boolean isEventNotificationsEnabled() {
        // Load event notifications checkbox state from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MySettings", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("eventNotificationsEnabled", true);
    }

    private void scheduleEventAlarms() {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

        for (CreateEventsModal event : events) {
            // Check if event notifications are enabled in the settings
            if (isEventNotificationsEnabled()) {
                String eventTime = event.getStartTime();
                long eventTimeInMillis = convertTimeToMillis(eventTime);

                // Creating an Intent for the AlarmReceiver class that will handle the alarm
                Intent alarmIntent = new Intent(requireContext(), AlarmReceiver.class);
                alarmIntent.putExtra("event_title", event.getTitle()); // Pass event title to the AlarmReceiver

                int uniqueId = generateUniqueId();
                PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), uniqueId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                // Use setExactAndAllowWhileIdle for API 23 and above, or setExact for API 19 and above
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, eventTimeInMillis, pendingIntent);
                } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, eventTimeInMillis, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, eventTimeInMillis, pendingIntent);
                }
            }
        }
    }


    private long convertTimeToMillis(String time) {
        String[] timeParts = time.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        // Get the current date
        Calendar calendar = Calendar.getInstance();
        long currentTimeInMillis = calendar.getTimeInMillis();

        // Set the time from "HH:mm" format
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long eventTimeInMillis = calendar.getTimeInMillis();

        // Check if the event time is in the past, if so, add one day to the event time
        if (eventTimeInMillis < currentTimeInMillis) {
            eventTimeInMillis += AlarmManager.INTERVAL_DAY;
        }

        return eventTimeInMillis;
    }


    private int generateUniqueId() {
        return (int) System.currentTimeMillis();
    }

}
