package com.example.noticeboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class TrendsAdapter extends RecyclerView.Adapter<TrendsAdapter.TrendsViewHolder> {

    private List<PostNoticeModal> trendingNoticesList;
    private Context context;

    public TrendsAdapter(List<PostNoticeModal> trendingNoticesList, Context context) {
        this.trendingNoticesList = trendingNoticesList;
        this.context = context;
    }

    @NonNull
    @Override
    public TrendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trends_layout, parent, false);
        return new TrendsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrendsViewHolder holder, int position) {
        PostNoticeModal notice = trendingNoticesList.get(position);
        holder.noticeTitle.setText(notice.getTitle());
        holder.noticeBody.setText(notice.getBody());
        holder.postedBy.setText("Posted by "+notice.getSubmittedBy());
        holder.dateTime.setText("On "+notice.getDateTime());

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
    }

    @Override
    public int getItemCount() {
        return trendingNoticesList.size();
    }

    public static class TrendsViewHolder extends RecyclerView.ViewHolder {
        private ImageView noticeImage;
        private TextView noticeTitle, noticeBody, fileLinks, postedBy, dateTime;

        public TrendsViewHolder(@NonNull View itemView) {
            super(itemView);
            noticeImage=itemView.findViewById(R.id.noticeImage);
            noticeTitle = itemView.findViewById(R.id.noticeTitle);
            noticeBody = itemView.findViewById(R.id.noticeBody);
            fileLinks=itemView.findViewById(R.id.fileLinks);
            postedBy=itemView.findViewById(R.id.postedBy);
            dateTime=itemView.findViewById(R.id.dateTime);
        }

    }
}
