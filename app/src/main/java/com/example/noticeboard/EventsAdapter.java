package com.example.noticeboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.MyViewHolder> {
    private ArrayList<CreateEventsModal> events;
    private Context context;

    public EventsAdapter(Context context, ArrayList<CreateEventsModal> events) {
        this.events = events;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflating the Notice Design Layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.events_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        CreateEventsModal event = events.get(position);
        holder.eventTitle.setText(event.getTitle());
        holder.eventDescription.setText(event.getDescription());
        holder.eventLocation.setText(event.getLocation());
        holder.eventDate.setText("Date: "+event.getDate());
        holder.eventTime.setText("Start: "+event.getStartTime()+" End: "+event.getEndTime());
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView eventTitle, eventDescription, eventLocation, eventDate, eventTime;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDescription = itemView.findViewById(R.id.eventDescription);
            eventLocation = itemView.findViewById(R.id.eventLocation);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventTime = itemView.findViewById(R.id.eventTime);
        }
    }
}
