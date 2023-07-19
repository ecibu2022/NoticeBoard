package com.example.noticeboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

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
            holder.fileLinks.setText(fileLinksBuilder.toString());
            holder.fileLinks.setVisibility(View.VISIBLE);
        } else {
            holder.fileLinks.setVisibility(View.GONE);
        }

        if (notice.getImageUrl() != null && !notice.getImageUrl().isEmpty()) {
//            Glide.with(context).load(notice.getImageUrl()).into();
        } else {
//            holder.noticeImagesRecyclerView.setVisibility(View.GONE);
        }

        // Working on approve notice button
        holder.approveNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                                                // Send a notification to the receiver
                                                // sendNotificationToReceiver(notice);

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

    }

    @Override
    public int getItemCount() {
        return notices.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView noticeTitle, noticeBody, postedBy, dateTime, fileLinks;
        private MaterialButton approveNotice;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            noticeTitle = itemView.findViewById(R.id.noticeTitle);
            noticeBody = itemView.findViewById(R.id.noticeBody);
            postedBy = itemView.findViewById(R.id.postedBy);
            dateTime = itemView.findViewById(R.id.dateTime);
            fileLinks = itemView.findViewById(R.id.fileLinks);
            approveNotice = itemView.findViewById(R.id.approveNotice);
        }
    }

    private void deleteNoticeImages(String noticeId, List<String> imageUrls) {
        if (imageUrls != null) {
            for (String imageUrl : imageUrls) {
                // Delete the image from storage
                StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                imageRef.delete()
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Failed to delete notice image", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    private void deleteNoticeFiles(String noticeId, List<String> fileUrls) {
        if (fileUrls != null) {
            for (String fileUrl : fileUrls) {
                // Delete the file from storage
                StorageReference fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl);
                fileRef.delete()
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Failed to delete notice file", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    public void removeItem(int position) {
        if (position >= 0 && position < notices.size()) {
            notices.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, notices.size());
        }
    }


}
