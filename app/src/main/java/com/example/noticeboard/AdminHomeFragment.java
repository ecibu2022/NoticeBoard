package com.example.noticeboard;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminHomeFragment extends Fragment {
    private RecyclerView myNotices;
    private DatabaseReference databaseReference;
    private List<PostNoticeModal> notices;
    private ApproveNoticeAdapter noticeAdapter;
    private FirebaseAuth mAuth;

    public AdminHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_admin_home, container, false);
        myNotices = view.findViewById(R.id.notices);
        mAuth=FirebaseAuth.getInstance();
        myNotices.setHasFixedSize(true);
        myNotices.setLayoutManager(new LinearLayoutManager(getContext()));

        notices = new ArrayList<>();
        noticeAdapter = new ApproveNoticeAdapter(getContext(), (ArrayList<PostNoticeModal>) notices);
        myNotices.setAdapter(noticeAdapter);

        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Loading Notices");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        databaseReference = FirebaseDatabase.getInstance().getReference("Notices");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                notices.clear();
                for (DataSnapshot itemSnapshot: snapshot.getChildren()){
                    PostNoticeModal myNotices = itemSnapshot.getValue(PostNoticeModal.class);
                    notices.add(myNotices);
                }
                // Sort the notices list based on date and time (newest on top)
                sortNoticesByDateTime(notices);
                noticeAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressDialog.dismiss();
            }

        });

        return  view;
    }

    public void logoutDialog(){
        AlertDialog.Builder logout=new AlertDialog.Builder(getContext());
        logout.setTitle("Logging Out?");
        logout.setMessage("Please Confirm!");
        logout.setCancelable(false);
        logout.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAuth.signOut();
                startActivity(new Intent(getContext(), Login.class));
            }
        });
        logout.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getContext(), "Logout Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
        logout.show(); // Show the AlertDialog
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

}
