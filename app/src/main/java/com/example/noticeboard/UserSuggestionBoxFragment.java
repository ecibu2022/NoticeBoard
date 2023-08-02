package com.example.noticeboard;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserSuggestionBoxFragment extends Fragment {
    private EditText title, body;
    private Button suggest;
    private DatabaseReference reference, usersRef;
    private String userID;

    public UserSuggestionBoxFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_user_suggestion_box, container, false);
        title=view.findViewById(R.id.suggestion_title);
        body=view.findViewById(R.id.suggestion_body);
        suggest=view.findViewById(R.id.suggest);
        reference= FirebaseDatabase.getInstance().getReference("suggestions");
        usersRef=FirebaseDatabase.getInstance().getReference("users");

        suggest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Title=title.getText().toString();
                String Body=body.getText().toString();
                String ID=reference.push().getKey();

                usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        userID=snapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getValue(String.class);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });

                SuggestionsModal suggestionsModal=new SuggestionsModal(ID, Title, Body, userID);

                reference.child(ID).setValue(suggestionsModal).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getContext(), "Suggestion Submitted Successfully", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getContext(), "Failed to Submit Suggestion", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Failed to Submit Suggestion", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        return view;
    }
}