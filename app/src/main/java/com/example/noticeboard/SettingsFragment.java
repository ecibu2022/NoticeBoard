package com.example.noticeboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;

public class SettingsFragment extends Fragment {
    private CheckBox checkBoxMessageNotifications, checkBoxEventNotifications, checkBoxVibration;
    private Button buttonStartTime, buttonEndTime, buttonSave, buttonSelectSound;
    private TextView startTime, endTime, sound;
    private int selectedStartHour, selectedStartMinute;
    private int selectedEndHour, selectedEndMinute;
    private Uri selectedSoundUri;
    private static final int REQUEST_SOUND_PICKER = 1;
    private OnSettingsSavedListener settingsSavedListener;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        checkBoxMessageNotifications = view.findViewById(R.id.checkBoxMessageNotifications);
        checkBoxEventNotifications = view.findViewById(R.id.checkBoxEventNotifications);
        checkBoxVibration = view.findViewById(R.id.checkBoxVibration);
        buttonStartTime = view.findViewById(R.id.buttonStartTime);
        buttonEndTime = view.findViewById(R.id.buttonEndTime);
        buttonSelectSound = view.findViewById(R.id.select);
        startTime = view.findViewById(R.id.startTime);
        endTime = view.findViewById(R.id.endTime);
        sound=view.findViewById(R.id.sound);
        buttonSave = view.findViewById(R.id.buttonSave);

        buttonStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerForStartTime();
            }
        });

        buttonEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerForEndTime();
            }
        });

        // Set the button text to the saved sound name
        updateSelectedSoundText();

        buttonSelectSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSoundPicker();
            }
        });

        // Load previously saved settings
        loadSettings();

        return view;
    }

    private void showTimePickerForStartTime() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getActivity(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        selectedStartHour = hourOfDay;
                        selectedStartMinute = minute;

                        String time = String.format("%02d:%02d", selectedStartHour, selectedStartMinute);
                        startTime.setText("Start Time: " + time);
                    }
                },
                selectedStartHour,
                selectedStartMinute,
                false
        );

        // Show the time picker dialog
        timePickerDialog.show();
    }

    private void showTimePickerForEndTime() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getActivity(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        selectedEndHour = hourOfDay;
                        selectedEndMinute = minute;

                        String time = String.format("%02d:%02d", selectedEndHour, selectedEndMinute);
                        endTime.setText("End Time: " + time);
                    }
                },
                selectedEndHour,
                selectedEndMinute,
                false
        );

        // Show the time picker dialog
        timePickerDialog.show();
    }

    private void openSoundPicker() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Select Notification Sound");

        // Get the list of notification sounds from RingtoneManager
        final RingtoneManager manager = new RingtoneManager(getActivity());
        manager.setType(RingtoneManager.TYPE_NOTIFICATION);
        final Cursor cursor = manager.getCursor();

        int selectedItem = getSoundPosition(selectedSoundUri);

        String[] soundNames = new String[cursor.getCount()];
        if (cursor.moveToFirst()) {
            do {
                int position = cursor.getPosition();
                Uri uri = manager.getRingtoneUri(position);
                Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), uri);
                soundNames[position] = ringtone.getTitle(getActivity());
            } while (cursor.moveToNext());
        }

        alertDialog.setSingleChoiceItems(soundNames, selectedItem, (dialogInterface, i) -> {
            cursor.moveToPosition(i);
            selectedSoundUri = manager.getRingtoneUri(cursor.getPosition());
            updateSelectedSoundText();

            // Save the selected sound
            saveSettings();

            dialogInterface.dismiss();
        });

        alertDialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        alertDialog.show();
    }

    // Method to retrieve the sound URI based on the selected position in the spinner
    @SuppressLint("Range")
    private Uri getSoundUri(int position) {
        if (position == 0) {
            // Return the default sound URI if the first position is selected
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else {
            // Return the URI of the selected sound from the RingtoneManager
            Cursor cursor = null;
            try {
                cursor = getActivity().getContentResolver().query(
                        RingtoneManager.getActualDefaultRingtoneUri(getActivity(), RingtoneManager.TYPE_NOTIFICATION),
                        new String[]{RingtoneManager.EXTRA_RINGTONE_PICKED_URI},
                        null,
                        null,
                        null
                );

                if (cursor != null && cursor.moveToFirst()) {
                    return Uri.parse(cursor.getString(cursor.getColumnIndex(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return null;
    }

    // Method to retrieve the position of the sound URI in the spinner
    private int getSoundPosition(Uri soundUri) {
        // Get the list of notification sounds from RingtoneManager
        RingtoneManager manager = new RingtoneManager(getActivity());
        manager.setType(RingtoneManager.TYPE_NOTIFICATION);
        Cursor cursor = manager.getCursor();

        // Iterate through the list to find the position of the sound URI
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int currentPosition = cursor.getPosition();
                Uri uri = manager.getRingtoneUri(currentPosition);
                if (uri != null && uri.equals(soundUri)) {
                    return currentPosition;
                }
            } while (cursor.moveToNext());
        }

        return -1;
    }

    // Update the button text with the selected sound name
    private void updateSelectedSoundText() {
        if (selectedSoundUri != null) {
            Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), selectedSoundUri);
            String title = ringtone.getTitle(getActivity());
            sound.setText(title);
        } else {
            sound.setText("Select Notification Sound");
        }
    }

    private void saveSettings() {
        // Retrieve user-selected settings (e.g., selectedStartHour, selectedStartMinute, selectedSoundUri)
        int startHour = selectedStartHour;
        int startMinute = selectedStartMinute;
        boolean eventNotificationsEnabled = checkBoxEventNotifications.isChecked();
        String soundUriString = selectedSoundUri != null ? selectedSoundUri.toString() : null;

        // Save settings to SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MySettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("startHour", startHour);
        editor.putInt("startMinute", startMinute);
        editor.putBoolean("eventNotificationsEnabled", eventNotificationsEnabled);
        editor.putString("soundUri", soundUriString);
        editor.apply();

        // Notifying the parent activity/fragment if you implemented the OnSettingsSavedListener interface
        if (settingsSavedListener != null) {
            settingsSavedListener.onSettingsSaved();
        }
    }

    public interface OnSettingsSavedListener {
        void onSettingsSaved();
    }

    private void loadSettings() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MySettings", Context.MODE_PRIVATE);

        // Load previously saved settings (e.g., selectedStartHour, selectedStartMinute, selectedSoundUri)
        int startHour = sharedPreferences.getInt("startHour", 0);
        int startMinute = sharedPreferences.getInt("startMinute", 0);
        boolean eventNotificationsEnabled = sharedPreferences.getBoolean("eventNotificationsEnabled", true);
        String soundUriString = sharedPreferences.getString("soundUri", null);
        selectedStartHour = startHour;
        selectedStartMinute = startMinute;
        selectedSoundUri = soundUriString != null ? Uri.parse(soundUriString) : null;

        // Set the event notifications checkbox state
        checkBoxEventNotifications.setChecked(eventNotificationsEnabled);
    }

    public void setOnSettingsSavedListener(OnSettingsSavedListener listener) {
        this.settingsSavedListener = listener;
    }

}
