package com.example.noticeboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

@ExperimentalBadgeUtils public class NoticeDetails extends AppCompatActivity {
    private TextView noticeTitle, noticeBody, fileLinks, postedBy, dateTime;
    private ImageView noticeImage;
    private ImageView likeImageView, shareImageView, commentImageView;
    private DatabaseReference trendsRef;
    private boolean isLiked = false;
    PostNoticeModal notice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_details);

        // Retrieve the notice object from the intent
        notice = getIntent().getParcelableExtra("notice");

        // Set the notice content to the respective views in the layout
        noticeTitle = findViewById(R.id.noticeTitle);
        noticeBody = findViewById(R.id.noticeBody);
        fileLinks = findViewById(R.id.fileLinks);
        postedBy = findViewById(R.id.postedBy);
        dateTime = findViewById(R.id.dateTime);
        likeImageView = findViewById(R.id.like);
        commentImageView = findViewById(R.id.comment);
        shareImageView = findViewById(R.id.share);
        trendsRef = FirebaseDatabase.getInstance().getReference("trends");

        noticeTitle.setText(notice.getTitle());
        noticeBody.setText(notice.getBody());
        postedBy.setText("Posted by " + notice.getSubmittedBy());
        dateTime.setText("On " + notice.getDateTime());

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


        // Check if the notice is liked by the current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            isLikedByUser(notice.getId(), userId, new LikeStatusCallback() {
                @Override
                public void onCallback(boolean isLiked) {
                    // Update the UI with the like status
                    if (isLiked) {
                        // The notice is liked by the user, update the UI accordingly
                        isLiked = true;
                        likeImageView.setImageResource(R.drawable.logo);
                    }
                }
            });
        }

        // Share Notice
//        shareImageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Share the notice with other apps using the Android sharing functionality
//                Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
//                shareIntent.setType("*/*");
//                shareIntent.putExtra(Intent.EXTRA_TEXT, notice.getTitle() + "\n\n" + notice.getBody());
//
//                ArrayList<Uri> fileUris = new ArrayList<>();
//
//                // Add image URIs to the share intent
//                List<String> imageUrls = notice.getImageUrls();
//                if (imageUrls != null && !imageUrls.isEmpty()) {
//                    for (String imageUrl : imageUrls) {
//                        // Convert image URL to Uri
//                        Uri imageUri = Uri.parse(imageUrl);
//                        fileUris.add(imageUri);
//                    }
//                }
//
//                // Add file URIs to the share intent
//                List<String> fileUrls = notice.getFileUrls();
//                if (fileUrls != null && !fileUrls.isEmpty()) {
//                    for (String fileUrl : fileUrls) {
//                        // Convert file URL to Uri
//                        Uri fileUri = Uri.parse(fileUrl);
//                        fileUris.add(fileUri);
//                    }
//                }
//
//                // Set the list of file URIs to the share intent
//                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris);
//
//                startActivity(Intent.createChooser(shareIntent, "Share Notice"));
//            }
//        });


        // Set up the like button click listener
        likeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    final String userId = currentUser.getUid();
                    final String noticeId = notice.getId(); // Store the noticeId separately

                    isLikedByUser(noticeId, userId, new LikeStatusCallback() {
                        @Override
                        public void onCallback(boolean isLiked) {
                            if (!isLiked) {
                                // Perform the action when the "like" image view is clicked
                                // Increment the like count and update the badge on the "like" image view
                                int currentLikeCount = notice.getLikeCount();
                                int newLikeCount = currentLikeCount + 1;
                                notice.setLikeCount(newLikeCount);

                                // Update the UI with the new like count
                                updateLikeCountUI(newLikeCount);

                                // Set the "liked" word on the "like" image view
                                likeImageView.setImageResource(R.drawable.logo);

                                // Submit the updated like count to the database
                                submitLikeToDatabase(noticeId, newLikeCount, userId);
                                submitNoticeToDatabase(notice);

                                // Mark the notice as liked by the current user
                                markNoticeAsLiked(noticeId, userId);
                            }
                        }
                    });
                } else {
                    // Handle the case when the user is not logged in
                    // You can prompt the user to log in or handle it according to your requirements
                }
            }
        });
    }

    private void isLikedByUser(String noticeId, String userId, final LikeStatusCallback callback) {
        if (noticeId != null && userId != null) {
            DatabaseReference likesRef = trendsRef.child(noticeId).child("likes").child(userId);
            likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean isLiked = dataSnapshot.exists();
                    callback.onCallback(isLiked);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    callback.onCallback(false);
                }
            });
        } else {
            callback.onCallback(false);
        }
    }

    interface LikeStatusCallback {
        void onCallback(boolean isLiked);
    }

//    Marking Notice as Liked
    private void markNoticeAsLiked(String noticeId, String userId) {
        // Here, I'm assuming you have a "likes" node under "trends" that stores the users who liked a notice
        if (noticeId != null && userId != null) {
            trendsRef.child(noticeId).child("likes").child(userId).setValue(true);
        }
    }


    // Submit the like to the database
    private void submitLikeToDatabase(String noticeId, int likeCount, String userId) {
        if (noticeId != null) {
            // Submit the updated like count to the "trends" node in the database
            trendsRef.child(noticeId).child("likeCount").setValue(likeCount);

            // Store the user's like in the "likes" node of the notice in the database
            trendsRef.child(noticeId).child("likes").child(userId).setValue(true);
        }
    }

    private void submitNoticeToDatabase(PostNoticeModal notice) {
        // Generate a unique ID for the notice in the "trends" node
        String noticeId = trendsRef.push().getKey();

        // Set the notice details in the "trends" node
        trendsRef.child(noticeId).setValue(notice);
    }

    private void submitLikeCountToDatabase(String noticeId, int likeCount) {
        // Submit the updated like count to the "trends" node in the database
        trendsRef.child(noticeId).child("likeCount").setValue(likeCount);
    }

    private void updateLikeCountUI(int likeCount) {
        // Update the badge on the "like" image view with the new like count
        BadgeDrawable badgeDrawable = BadgeDrawable.create(this);
        badgeDrawable.setNumber(likeCount);
        BadgeUtils.attachBadgeDrawable(badgeDrawable, likeImageView, null);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the back button click
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Finish the activity and go back to the previous activity
        super.onBackPressed();
        finish();
    }

}