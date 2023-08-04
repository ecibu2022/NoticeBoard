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

public class OfficialsNoticeAdapter extends RecyclerView.Adapter<OfficialsNoticeAdapter.MyViewHolder> {
    private ArrayList<PostNoticeModal> notices;
    private Context context;

    public OfficialsNoticeAdapter(Context context, ArrayList<PostNoticeModal> notices) {
        this.notices = notices;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflating the Notice Design Layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.officials_notice_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        PostNoticeModal notice = notices.get(position);
        holder.noticeTitle.setText(notice.getTitle());
        holder.noticeBody.setText(notice.getBody());

        // Set file URL to the fileLinks TextView
        String fileUrl = notice.getFileUrl();
        if (fileUrl != null && !fileUrl.isEmpty()) {
            holder.fileLinks.setVisibility(View.GONE);
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
                Intent intent = new Intent(context, OfficialsNoticeDetails.class);
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
        private TextView noticeTitle, noticeBody, fileLinks;
        private ImageView noticeImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            noticeTitle = itemView.findViewById(R.id.noticeTitle);
            noticeBody = itemView.findViewById(R.id.noticeBody);
            fileLinks = itemView.findViewById(R.id.fileLinks);
            noticeImage = itemView.findViewById(R.id.noticeImage);
        }
    }

}
