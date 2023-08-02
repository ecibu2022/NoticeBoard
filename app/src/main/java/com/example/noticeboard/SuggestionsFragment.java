package com.example.noticeboard;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SuggestionsFragment extends Fragment {
    private RecyclerView mySuggestions;
    private DatabaseReference databaseReference;
    private List<SuggestionsModal> suggestions;
    private SuggestionsAdapter suggestionsAdapter;

    public SuggestionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_suggestions, container, false);
        mySuggestions = view.findViewById(R.id.suggestions);
        mySuggestions.setHasFixedSize(true);
        mySuggestions.setLayoutManager(new LinearLayoutManager(getContext()));

        suggestions = new ArrayList<>();
        suggestionsAdapter = new SuggestionsAdapter(getContext(), (ArrayList<SuggestionsModal>) suggestions);
        mySuggestions.setAdapter(suggestionsAdapter);

        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Loading Suggestions");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        databaseReference = FirebaseDatabase.getInstance().getReference("suggestions");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                suggestions.clear();
                for (DataSnapshot itemSnapshot: snapshot.getChildren()){
                    SuggestionsModal suggestionsModal = itemSnapshot.getValue(SuggestionsModal.class);
                    suggestions.add(suggestionsModal);
                }

                suggestionsAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressDialog.dismiss();
            }

        });

        return view;
    }
}