package com.example.noticeboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OfficialsNoticeDetails extends AppCompatActivity {

    private TextView noticeTitle, noticeBody, fileLinks, commentTxt;
    private ImageView noticeImage;
    private DatabaseReference noticeRef;
    private PostNoticeModal notice;
    private RecyclerView commentsRecyclerView;
    private List<Comment> comments;
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officials_notice_details);

        // Retrieve the notice object from the intent
        notice = getIntent().getParcelableExtra("notice");

        if (notice == null) {
            // Handle the case when notice is null
            Toast.makeText(this, "Error: Notice is null.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set the notice content to the respective views in the layout
        noticeImage=findViewById(R.id.noticeImage);
        noticeTitle = findViewById(R.id.noticeTitle);
        noticeBody = findViewById(R.id.noticeBody);
        fileLinks = findViewById(R.id.fileLinks);
        commentTxt=findViewById(R.id.commentTxt);
        commentsRecyclerView=findViewById(R.id.commentsRecyclerView);
        commentsRecyclerView.setHasFixedSize(true);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        comments=new ArrayList<>();
        commentAdapter=new CommentAdapter(OfficialsNoticeDetails.this, (ArrayList<Comment>) comments);
        commentsRecyclerView.setAdapter(commentAdapter);

        noticeRef = FirebaseDatabase.getInstance().getReference("approved_notices").child(notice.getId());

        noticeTitle.setText(notice.getTitle());
        noticeBody.setText(notice.getBody());

//        Retrieving Comments
        // Replace "your_notice_id" with the actual notice ID for which you want to retrieve comments
        String noticeId = notice.getId();

// Get a reference to the specific notice node in the "approved_notices" database
        DatabaseReference noticeRef = FirebaseDatabase.getInstance().getReference("approved_notices").child(noticeId);

// Add a ValueEventListener to retrieve the notice details, including the comments
        noticeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // Get the PostNoticeModal object from the database
                PostNoticeModal notice = snapshot.getValue(PostNoticeModal.class);

                if (notice != null) {
                    // Check if the comments field is not null
                    HashMap<String, Comment> commentsMap = notice.getComments();
                    if (commentsMap != null) {
                        // The comments field is not null, so retrieve the comments
                        comments.clear();
                        for (String commentId : commentsMap.keySet()) {
                            Comment comment = commentsMap.get(commentId);
                            comments.add(comment);
                        }

                        // Notify the adapter that the data has changed
                        commentAdapter.notifyDataSetChanged();
                    }else{
                        commentsRecyclerView.setVisibility(View.GONE);
                        commentTxt.setText(null);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle database read error
            }
        });


        // Set file URL to the fileLinks TextView
        String fileUrl = notice.getFileUrl();
        if (fileUrl != null && !fileUrl.isEmpty()) {
            fileLinks.setText(notice.getFileUrl());
            fileLinks.setVisibility(View.VISIBLE);
        } else {
            fileLinks.setVisibility(View.GONE);
        }

//        Set image
        if (notice.getImageUrl() != null && !notice.getImageUrl().isEmpty()) {
            noticeImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(notice.getImageUrl()).into(noticeImage);
        } else {
            noticeImage.setVisibility(View.GONE);
        }

    }
}