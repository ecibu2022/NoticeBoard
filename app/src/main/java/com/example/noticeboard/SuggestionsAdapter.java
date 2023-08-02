package com.example.noticeboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.MyViewHolder> {
    private ArrayList<SuggestionsModal> suggestions;
    private Context context;

    public SuggestionsAdapter(Context context, ArrayList<SuggestionsModal> suggestions) {
        this.suggestions = suggestions;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflating the Notice Design Layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.suggestions_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        SuggestionsModal mySuggestions = suggestions.get(position);
        holder.title.setText(mySuggestions.getTitle());
        holder.body.setText(mySuggestions.getBody());
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView title, body;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            body = itemView.findViewById(R.id.body);
        }
    }

}
