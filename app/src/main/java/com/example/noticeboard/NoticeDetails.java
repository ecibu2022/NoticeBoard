package com.example.noticeboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NoticeDetails extends AppCompatActivity {
    private TextView noticeTitle, noticeBody, fileLinks, submitedBy, dateTime, num_of_likes;
    private ImageView noticeImage, likeImageView, shareImageView, commentImageView;
    private DatabaseReference likesRef, noticeRef;
    private FirebaseUser currentUser;
    PostNoticeModal notice;
    private boolean isLikedByCurrentUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_details);

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
        submitedBy=findViewById(R.id.postedBy);
        dateTime = findViewById(R.id.dateTime);
        likeImageView = findViewById(R.id.like);
        commentImageView = findViewById(R.id.comment);
        shareImageView = findViewById(R.id.share);
        num_of_likes=findViewById(R.id.num_of_likes);
        currentUser=FirebaseAuth.getInstance().getCurrentUser();
        likesRef = FirebaseDatabase.getInstance().getReference("Likes").child(notice.getId());
        noticeRef = FirebaseDatabase.getInstance().getReference("approved_notices").child(notice.getId());

        noticeTitle.setText(notice.getTitle());
        noticeBody.setText(notice.getBody());
        submitedBy.setText("Posted by "+ notice.getSubmittedBy());
        dateTime.setText("On " + notice.getDateTime());

        // Set file URL to the fileLinks TextView
        String fileUrl = notice.getFileUrl();
        if (fileUrl != null && !fileUrl.isEmpty()) {
            fileLinks.setText(notice.getFileUrl());
            fileLinks.setVisibility(View.VISIBLE);
            setFileLinksClickListener(fileLinks, fileUrl);
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


        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get the total number of likes
                long likesCount = snapshot.getChildrenCount();
                // Update the like count TextView
                num_of_likes.setText(String.valueOf(likesCount)+" Likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        likeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentUser == null) {
                    // User not logged in, handle login or show a message
                    return;
                }

                if (isLikedByCurrentUser) {
                    // User already liked the notice, perform unlike action
                    likesRef.child(currentUser.getUid()).removeValue();
                    decrementLikeCount();
                } else {
                    // User has not liked the notice, perform like action
                    likesRef.child(currentUser.getUid()).setValue(true);
                    incrementLikeCount();
                }

                // Update the UI to reflect the change in like status
                isLikedByCurrentUser = !isLikedByCurrentUser;
                updateLikeStatus();
            }
        });

//        Share Notice
        shareImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareNotice();
            }
        });
    }


    // Function to update the like button UI based on user's like status
    private void updateLikeStatus() {
        if (isLikedByCurrentUser) {
            likeImageView.setImageResource(R.drawable.liked);
        } else {
            likeImageView.setImageResource(R.drawable.like);
        }
    }

    // Function to increment the likeCount in the database
    private void incrementLikeCount() {
        // Increment the likeCount in the database
        notice.setLikeCount(notice.getLikeCount() + 1);

        // Update the likeCount value in the "Notices" node
        noticeRef.child("likeCount").setValue(notice.getLikeCount())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Like count successfully incremented in the database
                        Log.d("NoticeDetails", "New like count: " + notice.getLikeCount());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to update like count in the database
                        Toast.makeText(NoticeDetails.this, "Failed to update like count", Toast.LENGTH_SHORT).show();
                        // Revert the local change since the database update failed
                        notice.setLikeCount(notice.getLikeCount() - 1);
                        updateLikeStatus(); // Update the UI to reflect the reverted change
                    }
                });
    }

    // Function to decrement the likeCount in the database
    private void decrementLikeCount() {
        // Decrement the likeCount in the database
        notice.setLikeCount(notice.getLikeCount() - 1);

        // Update the likeCount value in the "Notices" node
        noticeRef.child("likeCount").setValue(notice.getLikeCount())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Like count successfully decremented in the database
                        Log.d("NoticeDetails", "New like count: " + notice.getLikeCount());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to update like count in the database
                        Toast.makeText(NoticeDetails.this, "Failed to update like count", Toast.LENGTH_SHORT).show();
                        // Revert the local change since the database update failed
                        notice.setLikeCount(notice.getLikeCount() + 1);
//                        updateLikeStatus(); // Update the UI to reflect the reverted change
                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Add an AuthStateListener to handle user login/logout events
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove the AuthStateListener to avoid memory leaks
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    }

    // AuthStateListener to handle user login/logout events
    private FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            currentUser = firebaseAuth.getCurrentUser();
            // Check if the current user has already liked the notice after login/logout
            if (currentUser != null) {
                likesRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isLikedByCurrentUser = snapshot.exists();
                        updateLikeStatus();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
    };

    // Function to share the notice details along with the image and file link (if available)
    private void shareNotice() {
        // Check if the notice image is available
        if (noticeImage.getVisibility() == View.VISIBLE) {
            // Get the notice image drawable from the ImageView
            Drawable noticeImageDrawable = noticeImage.getDrawable();
            if (noticeImageDrawable != null) {
                // Convert the drawable to a Bitmap
                Bitmap noticeImageBitmap = Utils.drawableToBitmap(noticeImageDrawable);

                // Create an Intent to share the image
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                // Put the Bitmap image as an extra in the Intent
                shareIntent.putExtra(Intent.EXTRA_STREAM, Utils.getBitmapUri(NoticeDetails.this, noticeImageBitmap));

                // Add the title and body text to the shared content
                String sharedText = "Notice Title: " + notice.getTitle() + "\n\n"
                        + "Notice Body: " + notice.getBody() + "\n\n";

                // Check if file link is available
                String fileUrl = notice.getFileUrl();
                if (fileUrl != null && !fileUrl.isEmpty()) {
                    sharedText += "File Link: " + fileUrl;
                }

                // Add the shared text as an extra in the Intent
                shareIntent.putExtra(Intent.EXTRA_TEXT, sharedText);

                // Add a subject for the shared content
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Notice Details");
                // Start the Intent
                startActivity(Intent.createChooser(shareIntent, "Share Notice Details"));
            } else {
                // The notice image is not available
                Toast.makeText(this, "Notice image is not available for sharing.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // The notice image is not available
            Toast.makeText(this, "Notice image is not available for sharing.", Toast.LENGTH_SHORT).show();
        }
    }


    private void setFileLinksClickListener(TextView fileLinksTextView, String fileUrl) {
        fileLinksTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                viewIntent.setData(Uri.parse(fileUrl));

                if (viewIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(viewIntent);
                } else {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
                    request.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0");

                    DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    downloadManager.enqueue(request);
                }
            }
        });
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