package com.example.noticeboard;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class EventsFragment extends Fragment {
    private AutoCompleteTextView selectDate, selectStartTime, selectEndTime;
    private EditText eventTitle, eventDescription, eventLocation;
    private ToggleButton toggle_reminder;
    private Button events;
    private DatabaseReference eventRef;

    public EventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        eventTitle = view.findViewById(R.id.title);
        eventDescription = view.findViewById(R.id.description);
        eventLocation = view.findViewById(R.id.location);
        selectDate = view.findViewById(R.id.selectDate);
        selectStartTime = view.findViewById(R.id.selectStartTime);
        selectEndTime = view.findViewById(R.id.selectEndTime);
        toggle_reminder = view.findViewById(R.id.toggle_reminder);
        events = view.findViewById(R.id.events);

        eventRef = FirebaseDatabase.getInstance().getReference("events");

        // Date Picker
        MaterialDatePicker.Builder<Long> materialDateBuilder = MaterialDatePicker.Builder.datePicker();
        materialDateBuilder.setTitleText("SELECT EVENT DATE");
        final MaterialDatePicker<Long> materialDatePicker = materialDateBuilder.build();

        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDatePicker.show(getParentFragmentManager(), "MATERIAL_DATE_PICKER");
            }
        });

        materialDatePicker.addOnPositiveButtonClickListener(
                new MaterialPickerOnPositiveButtonClickListener<Long>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onPositiveButtonClick(Long selection) {
                        // Set the selected date to the AutoCompleteTextView
                        selectDate.setText(materialDatePicker.getHeaderText());
                    }
                });

        // Start Time Picker
        selectStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();

                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                selectStartTime.setText(hourOfDay + ":" + minute);
                            }
                        }, hour, minute, false);
                timePickerDialog.show();
            }
        });

        // End Time Picker
        selectEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();

                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                selectEndTime.setText(hourOfDay + ":" + minute);
                            }
                        }, hour, minute, false);
                timePickerDialog.show();
            }
        });

        // Create Events Button
        events.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateFields();
            }
        });

        return view;
    }

    public void validateFields() {
        String eventID = eventRef.push().getKey();
        String Title = eventTitle.getText().toString().trim();
        String Description = eventDescription.getText().toString().trim();
        String Date = selectDate.getText().toString().trim();
        String startTime = selectStartTime.getText().toString().trim();
        String endTime = selectEndTime.getText().toString().trim();
        String Location = eventLocation.getText().toString().trim();

        if (Title.isEmpty()) {
            eventTitle.setError("Title is required");
            eventTitle.requestFocus();
            return;
        } else if (Description.isEmpty()) {
            eventDescription.setError("Description is required");
            eventDescription.requestFocus();
            return;
        } else if (Date.isEmpty()) {
            selectDate.setError("Date is required");
            selectDate.requestFocus();
            return;
        } else if (startTime.isEmpty()) {
            selectStartTime.setError("Start time is required");
            selectStartTime.requestFocus();
            return;
        } else if (endTime.isEmpty()) {
            selectEndTime.setError("End time is required");
            selectEndTime.requestFocus();
            return;
        } else if (Location.isEmpty()) {
            eventLocation.setError("Location is required");
            eventLocation.requestFocus();
            return;
        }

        ProgressDialog progressDialog=new ProgressDialog(getContext());
        progressDialog.setTitle("Creating Events In Progress");
        progressDialog.setMessage("Please wait....");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        CreateEventsModal createEventsModal = new CreateEventsModal(eventID, Title, Description, Date, startTime, endTime, Location);
        eventRef.child(eventID).setValue(createEventsModal).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Event Created Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to create Event", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Failed to create Event", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
