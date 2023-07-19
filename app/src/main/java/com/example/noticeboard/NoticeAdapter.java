package com.example.noticeboard;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import java.util.ArrayList;
import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.MyViewHolder> {
    private ArrayList<PostNoticeModal> notices;
    private Context context;

    public NoticeAdapter(Context context, ArrayList<PostNoticeModal> notices) {
        this.notices = notices;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflating the Notice Design Layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notice_summary_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        PostNoticeModal notice = notices.get(position);
        holder.noticeTitle.setText(notice.getTitle());
        holder.noticeBody.setText(notice.getBody());
        holder.postedBy.setText("Posted by " + notice.getSubmittedBy());
        holder.dateTime.setText("On " + notice.getDateTime());

        // Set file URL to the fileLinks TextView
        String fileUrl = notice.getFileUrl();
        if (fileUrl != null && !fileUrl.isEmpty()) {
            holder.fileLinks.setText(notice.getFileUrl());
            holder.fileLinks.setVisibility(View.VISIBLE);
            setFileLinksClickListener(holder.fileLinks, fileUrl);
        } else {
            holder.fileLinks.setVisibility(View.GONE);
        }

//        Set image
        if (notice.getImageUrl() != null && !notice.getImageUrl().isEmpty()) {
            holder.noticeImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(notice.getImageUrl()).into(holder.noticeImage);
        } else {
            holder.noticeImage.setVisibility(View.GONE);
        }

        // Set click listener for the item view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open a new activity for details
                @SuppressLint("UnsafeOptInUsageError")
                Intent intent = new Intent(context, NoticeDetails.class);
                intent.putExtra("notice", notice);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notices.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView noticeTitle, noticeBody, postedBy, dateTime, fileLinks;
        private ImageView noticeImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            noticeTitle = itemView.findViewById(R.id.noticeTitle);
            noticeBody = itemView.findViewById(R.id.noticeBody);
            postedBy = itemView.findViewById(R.id.postedBy);
            dateTime = itemView.findViewById(R.id.dateTime);
            fileLinks = itemView.findViewById(R.id.fileLinks);
            noticeImage = itemView.findViewById(R.id.noticeImage);
        }
    }

    private void setFileLinksClickListener(TextView fileLinksTextView, String fileUrl) {
        fileLinksTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to view the file
                Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                viewIntent.setData(Uri.parse(fileUrl));

                // Check if there is an app available to handle the view intent
                if (viewIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(viewIntent);
                } else {
                    // If no suitable app is found, initiate download using DownloadManager
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
                    request.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0");

                    DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    downloadManager.enqueue(request);
                }
            }
        });
    }

}
