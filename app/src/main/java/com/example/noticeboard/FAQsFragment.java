package com.example.noticeboard;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FAQsFragment extends Fragment {
    private RecyclerView recyclerView;
    private FAQAdapter faqAdapter;
    private List<FAQItem> faqItemList;

    public FAQsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_f_a_qs, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the list of FAQ items
        faqItemList = new ArrayList<>();
        faqItemList.add(new FAQItem("How do I log in to the online notice board?", "To log in, click on the \"Login\" button and enter your credentials provided during registration."));
        faqItemList.add(new FAQItem("Can I post notices and events on the online notice board?", "Yes, as a registered user, you can post both notices and events to the notice board."));
        faqItemList.add(new FAQItem("How do I view notices and events on the notice board?", "Click on the \"View Notices\" or \"View Events\" tab to see the latest updates and information."));
        faqItemList.add(new FAQItem("Can I like, comment, and share notices and events on the platform?", "Absolutely! You can engage with the content by liking, commenting, and sharing it with others."));
        faqItemList.add(new FAQItem("How can I view trends and popular posts on the notice board?", "The \"View Trends\" section allows you to see the most popular and trending notices and events."));
        faqItemList.add(new FAQItem("What should I do if I forget my login credentials?", "Click on the \"Forgot Password\" link on the login page and follow the instructions to reset your password."));
        faqItemList.add(new FAQItem("Can I view and update my profile information on the platform?", "Yes, you can manage your profile by clicking on the \"Profile\" tab and making the necessary changes."));
        faqItemList.add(new FAQItem("How can I search for specific notices or events on the notice board?", "Use the search bar to enter keywords related to the information you are looking for."));
        faqItemList.add(new FAQItem("Is the notice board accessible on mobile devices?", "Yes, the platform is mobile-friendly and can be accessed on smartphones and tablets."));
        faqItemList.add(new FAQItem("How can I suggest improvements or report issues with the notice board?", "You can use the \"Make a Suggestion\" section to provide feedback and report any problems you encounter."));
        faqItemList.add(new FAQItem("Can I view frequently asked questions (FAQs) on the notice board?", "Yes, there is a dedicated section where you can find answers to common questions."));
        faqItemList.add(new FAQItem("Are there specific guidelines for posting notices and events?", "Yes, ensure you follow the guidelines provided on the platform to maintain a consistent format."));
        faqItemList.add(new FAQItem("Can I see who has viewed or interacted with my posted notices and events?", "Yes, you can view the number of views and interactions on your posts."));
        faqItemList.add(new FAQItem("Is there a notification system for new notices and events?", "Yes, you will receive notifications for new posts and important updates."));
        faqItemList.add(new FAQItem("Can I filter notices and events by category or department?", "Yes, the platform allows you to filter content based on specific categories or departments."));

        // Add more questions and answers as needed

        faqAdapter = new FAQAdapter(faqItemList);
        recyclerView.setAdapter(faqAdapter);

        return view;
    }

    // FAQItem class representing a single FAQ question-answer pair
    private static class FAQItem {
        String question;
        String answer;

        FAQItem(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
    }

    // RecyclerView adapter to handle FAQ items
    private class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.FAQViewHolder> {

        private List<FAQItem> itemList;

        FAQAdapter(List<FAQItem> itemList) {
            this.itemList = itemList;
        }

        @Override
        public FAQViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.faq_questions, parent, false);
            return new FAQViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FAQViewHolder holder, int position) {
            FAQItem item = itemList.get(position);
            holder.questionTextView.setText(item.question);
            holder.answerTextView.setText(item.answer);

            // Set click listener to expand/collapse the answer when clicked on the question
            holder.questionTextView.setOnClickListener(v -> {
                boolean isAnswerVisible = holder.answerTextView.getVisibility() == View.VISIBLE;
                holder.answerTextView.setVisibility(isAnswerVisible ? View.GONE : View.VISIBLE);
            });
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        class FAQViewHolder extends RecyclerView.ViewHolder {
            TextView questionTextView;
            TextView answerTextView;

            FAQViewHolder(@NonNull View itemView) {
                super(itemView);
                questionTextView = itemView.findViewById(R.id.questionTextView);
                answerTextView = itemView.findViewById(R.id.answerTextView);
            }
        }
    }

}