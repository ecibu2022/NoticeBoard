package com.example.noticeboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    private TextView noticeTitle, noticeBody, fileLinks, postedBy, dateTime, num_of_likes;
    private ImageView noticeImage, likeImageView, shareImageView, commentImageView;
    private DatabaseReference trendsRef;
    private FirebaseUser currentUser;
    PostNoticeModal notice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_details);

        // Retrieve the notice object from the intent
        notice = getIntent().getParcelableExtra("notice");

        // Set the notice content to the respective views in the layout
        noticeImage=findViewById(R.id.noticeImage);
        noticeTitle = findViewById(R.id.noticeTitle);
        noticeBody = findViewById(R.id.noticeBody);
        fileLinks = findViewById(R.id.fileLinks);
        postedBy = findViewById(R.id.postedBy);
        dateTime = findViewById(R.id.dateTime);
        likeImageView = findViewById(R.id.like);
        commentImageView = findViewById(R.id.comment);
        shareImageView = findViewById(R.id.share);
        num_of_likes=findViewById(R.id.num_of_likes);
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

//        Like Image View
        isLikes(notice.getId(), likeImageView);
        numberOfLikes(likeImageView, notice.getId());

        likeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(likeImageView.getTag().equals("Like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(notice.getId())
                            .child(currentUser.getUid()).setValue(true);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(notice.getId())
                            .child(currentUser.getUid()).removeValue();
                }
            }
        });

        // Share Notice
        shareImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Share the notice with other apps using the Android sharing functionality
                Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                shareIntent.setType("*/*");

                // Add notice title and body to the share intent as text
                shareIntent.putExtra(Intent.EXTRA_TEXT, notice.getTitle() + "\n\n" + notice.getBody());

                ArrayList<Uri> fileUris = new ArrayList<>();

                // Add image URI to the fileUris list
                if (notice.getImageUrl() != null && !notice.getImageUrl().isEmpty()) {
                    Uri imageUri = Uri.parse(notice.getImageUrl());
                    fileUris.add(imageUri);
                }

                // Add file URI to the fileUris list
                String fileUrl = notice.getFileUrl();
                if (fileUrl != null && !fileUrl.isEmpty()) {
                    Uri fileUri = Uri.parse(fileUrl);
                    fileUris.add(fileUri);
                }

                // Set the list of file URIs to the share intent
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris);

                startActivity(Intent.createChooser(shareIntent, "Share Notice"));
            }
        });

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

//    Like and Unlike method
public void isLikes(String noticeId, ImageView likeImage) {
    currentUser = FirebaseAuth.getInstance().getCurrentUser();

    if (currentUser != null && noticeId != null) {
        DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        likeRef.child(notice.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.child(currentUser.getUid()).exists()) {
                    likeImageView.setImageResource(R.drawable.liked);
                    likeImageView.setTag("Liked");
                } else {
                    likeImageView.setImageResource(R.drawable.like);
                    likeImageView.setTag("Like");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle the onCancelled event, if needed
            }
        });
    } else {
        // Handle the case when the user is not authenticated
        // You can set a default state for the likeImageView here
        likeImageView.setImageResource(R.drawable.like);
        likeImageView.setTag("Like");
    }
}

    public void numberOfLikes(ImageView textView, String noticeId) {
        if (noticeId != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes")
                    .child(noticeId);

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    num_of_likes.setText(String.valueOf(snapshot.getChildrenCount()));
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Handle the onCancelled event, if needed
                }
            });
        } else {
            // Handle the case when noticeId is null
            num_of_likes.setText("0");
        }
    }

}