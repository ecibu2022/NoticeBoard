package com.example.noticeboard;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class AdminProfileFragment extends Fragment {
    private View formContainer;
    private Button editProfileButton;

    public AdminProfileFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_edit_profile, container, false);

        formContainer = view.findViewById(R.id.formContainer);
        editProfileButton = view.findViewById(R.id.editProfile);

//        Showing Form For Editing Profile When Edit Profile Button Is Clicked
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (formContainer.getVisibility() == View.VISIBLE) {
                    formContainer.setVisibility(View.GONE);
                } else {
                    formContainer.setVisibility(View.VISIBLE);
                }
            }
        });
//        End of the Form

        return view;
    }
}