package com.example.noticeboard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApproveNoticeAdapter extends RecyclerView.Adapter<ApproveNoticeAdapter.MyViewHolder> {
    private ArrayList<PostNoticeModal> notices;
    private Context context;
    String key = "";

    public ApproveNoticeAdapter(Context context, ArrayList<PostNoticeModal> notices) {
        this.notices = notices;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflating the Notice Design Layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_approve_notice, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        PostNoticeModal notice = notices.get(position);
        holder.noticeTitle.setText(notice.getTitle());
        holder.noticeBody.setText(notice.getBody());
        holder.postedBy.setText("Posted by " + notice.getSubmittedBy());
        holder.dateTime.setText("On " + notice.getDateTime());

        // Set file URLs to the fileLinks TextView
        String fileUrl = notice.getFileUrl();
        if (fileUrl != null) {
            StringBuilder fileLinksBuilder = new StringBuilder();
            // Append the fileUrl to the fileLinksBuilder
            fileLinksBuilder.append(fileUrl).append("\n");
            holder.fileLinks.setText(fileLinksBuilder.toString());
            holder.fileLinks.setVisibility(View.VISIBLE);
        } else {
            holder.fileLinks.setVisibility(View.GONE);
        }

        if (notice.getImageUrl() != null && !notice.getImageUrl().isEmpty()) {
            Glide.with(context).load(notice.getImageUrl()).into(holder.noticeImage);
            holder.noticeImage.setVisibility(View.VISIBLE);
        } else {
            holder.noticeImage.setVisibility(View.GONE);
        }

        // Working on approve notice button
        holder.approveNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder approve=new AlertDialog.Builder(context);
                approve.setTitle("Are you sure you want to approve this notice!");
                approve.setMessage("#####Please Confirm?#####");
                approve.setCancelable(false);
                approve.setPositiveButton("Approve", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Check if the notices list is empty or not
                        if (notices.isEmpty()) {
                            Toast.makeText(context, "No notices to approve", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        final PostNoticeModal notice = notices.get(position);
                        final int clickedPosition = position;
                        key = notice.getId();

                        // Submit notice to the "approved_notices" node with the noticeId as the key
                        DatabaseReference approvedNoticeRef = FirebaseDatabase.getInstance().getReference("approved_notices")
                                .child(key); // Use the noticeId as the key for the approved notice
                        approvedNoticeRef.setValue(notice)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Delete the notice from the admin home
                                        DatabaseReference adminHomeRef = FirebaseDatabase.getInstance().getReference("Notices");
                                        // Delete the notice from the admin home
                                        adminHomeRef.child(key).removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // Sending a notification to the target audience
                                                        sendNotificationToTargetAudience(key, notice.getTitle());

                                                        // Remove the notice from the list using its noticeId
                                                        for (int i = 0; i < notices.size(); i++) {
                                                            if (notices.get(i).getId().equals(key)) {
                                                                notices.remove(i);
                                                                break;
                                                            }
                                                        }

                                                        notifyDataSetChanged(); // Refresh the RecyclerView
                                                        Toast.makeText(context, "Notice Approved", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(context, "Failed to delete notice from admin home", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Failed to submit notice to approved notices", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
                approve.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(context, "Notice NOT Approved", Toast.LENGTH_SHORT).show();
                    }
                });
                approve.show();

            }
        });

    }

    @Override
    public int getItemCount() {
        return notices.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView noticeTitle, noticeBody, postedBy, dateTime, fileLinks;
        private MaterialButton approveNotice;
        private ImageView noticeImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            noticeTitle = itemView.findViewById(R.id.noticeTitle);
            noticeBody = itemView.findViewById(R.id.noticeBody);
            postedBy = itemView.findViewById(R.id.postedBy);
            dateTime = itemView.findViewById(R.id.dateTime);
            fileLinks = itemView.findViewById(R.id.fileLinks);
            noticeImage=itemView.findViewById(R.id.noticeImage);
            approveNotice = itemView.findViewById(R.id.approveNotice);
        }
    }

//    Send Notification to Target Audience
private void sendNotificationToTargetAudience(String noticeId, String title) {
    DatabaseReference noticeRef = FirebaseDatabase.getInstance().getReference("Notices").child(noticeId);
    noticeRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            // Get the target audience from the notice
            String targetAudience = dataSnapshot.child("everyone").getValue(String.class);

            // Query the users with the specified target audience from the database
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
            // If the target audience is everyone, query all users with the user role
            queryUsersByRole("user");

            if ("everyone".equals(targetAudience)) {
                queryAllUsers();
            } else {
                // If the target audience is not everyone, query based on faculty, course, and year
                String faculty = dataSnapshot.child("faculty").getValue(String.class);
                String course = dataSnapshot.child("course").getValue(String.class);
                String year = dataSnapshot.child("year").getValue(String.class);

                if (faculty != null && !faculty.isEmpty() && course != null && !course.isEmpty() && year != null && !year.isEmpty()) {
                    // Query based on faculty, course, and year
                    queryUsersByFacultyCourseYear(faculty, course, year);
                } else if (faculty != null && !faculty.isEmpty() && course != null && !course.isEmpty()) {
                    // Query based on faculty and course
                    queryUsersByFacultyCourse(faculty, course);
                } else {
                    // Query based on faculty only
                    queryUsersByFaculty(faculty);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            // Handle any database error that occurred while fetching the data
        }
    });
}

    // Query users based on their role
    private void queryUsersByRole(String role) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        Query roleQuery = usersRef.orderByChild("role").equalTo(role);

        roleQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Send notifications to the retrieved users
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userToken = userSnapshot.child("deviceToken").getValue(String.class);
                    if (userToken != null) {
                        // Send the notification using FCM
                        sendFCMNotification(userToken, "New Notice Posted", "A new notice has been posted");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any database error that occurred while fetching the data
            }
        });
    }

    // Query all users
    private void queryAllUsers() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Send notifications to all users
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userToken = userSnapshot.child("deviceToken").getValue(String.class);
                    if (userToken != null) {
                        // Send the notification using FCM
                        sendFCMNotification(userToken, "New Notice Posted", "A new notice is available");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Query users based on faculty, course, and year
    private void queryUsersByFacultyCourseYear(String faculty, String course, String year) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        Query facultyCourseYearQuery = usersRef.orderByChild("faculty").equalTo(faculty)
                .orderByChild("course").equalTo(course)
                .orderByChild("year").equalTo(year);

        facultyCourseYearQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Send notifications to the retrieved users
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userToken = userSnapshot.child("deviceToken").getValue(String.class);
                    if (userToken != null) {
                        // Send the notification using FCM
                        sendFCMNotification(userToken, "New Notice Posted", "A new notice is available");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any database error that occurred while fetching the data
            }
        });
    }

    // Query users based on faculty and course
    private void queryUsersByFacultyCourse(String faculty, String course) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        Query facultyCourseQuery = usersRef.orderByChild("faculty").equalTo(faculty)
                .orderByChild("course").equalTo(course);

        facultyCourseQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Send notifications to the retrieved users
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userToken = userSnapshot.child("deviceToken").getValue(String.class);
                    if (userToken != null) {
                        // Send the notification using FCM
                        sendFCMNotification(userToken, "New Notice Posted", "A new notice is available");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any database error that occurred while fetching the data
            }
        });
    }

    // Query users based on faculty only
    private void queryUsersByFaculty(String faculty) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        Query facultyQuery = usersRef.orderByChild("faculty").equalTo(faculty);

        facultyQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Send notifications to the retrieved users
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userToken = userSnapshot.child("deviceToken").getValue(String.class);
                    if (userToken != null) {
                        // Send the notification using FCM
                        sendFCMNotification(userToken, "New Notice Posted", "A new notice is available");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any database error that occurred while fetching the data
            }
        });
    }

private void sendFCMNotification(String userToken, String title, String body) {
        // Set the FCM server key from Firebase Console
        String serverKey = "AAAASxz6AZI:APA91bELTl9eqIThc_9kJ3eTYWUYoLtVr1H9MS3AQHHKtSQOPa237wk6VNoRKZMeZqEFy9gh_xxS0zw_CekNpcw-NuAlLohCB_etwwC5GNw_il-Hz39L9sv5IuCHoEdiLvKcICxtli5_";

        // Create the FCM message data payload (customize as needed)
        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("body", body);

        // Create the FCM message body
        Map<String, Object> message = new HashMap<>();
        message.put("to", userToken);
        message.put("data", data);

        // Send the FCM message using OkHttp
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody requestBody = RequestBody.create(mediaType, new Gson().toJson(message));
        Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .addHeader("Authorization", "key=" + serverKey)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("FCM", "Failed to send notification to user", e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("FCM", "Notification sent to user");
                } else {
                    Log.e("FCM", "Failed to send notification to user");
                }
                response.close();
            }
        });
    }

}
