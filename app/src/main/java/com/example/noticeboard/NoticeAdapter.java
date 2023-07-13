package com.example.noticeboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

        // Set file URLs to the fileLinks TextView
        List<String> fileUrls = notice.getFileUrls();
        if (fileUrls != null) {
            StringBuilder fileLinksBuilder = new StringBuilder();
            for (String fileUrl : fileUrls) {
                fileLinksBuilder.append(fileUrl).append("\n");
            }
            holder.fileLinks.setText(fileLinksBuilder.toString());
            holder.fileLinks.setVisibility(View.VISIBLE);
        } else {
            holder.fileLinks.setVisibility(View.GONE);
        }

        if (notice.getImageUrls() != null && !notice.getImageUrls().isEmpty()) {
            holder.noticeImagesRecyclerView.setVisibility(View.VISIBLE);
            holder.noticeImagesRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            NoticeImageAdapter imageAdapter = new NoticeImageAdapter(context, notice.getImageUrls());
            holder.noticeImagesRecyclerView.setAdapter(imageAdapter);
        } else {
            holder.noticeImagesRecyclerView.setVisibility(View.GONE);
        }
    }



    @Override
    public int getItemCount() {
        return notices.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView noticeTitle, noticeBody, postedBy, dateTime, fileLinks;
        private RecyclerView noticeImagesRecyclerView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            noticeTitle = itemView.findViewById(R.id.noticeTitle);
            noticeBody = itemView.findViewById(R.id.noticeBody);
            postedBy = itemView.findViewById(R.id.postedBy);
            dateTime = itemView.findViewById(R.id.dateTime);
            fileLinks=itemView.findViewById(R.id.fileLinks);
            noticeImagesRecyclerView = itemView.findViewById(R.id.noticeRecyclerView);
        }
    }
}
