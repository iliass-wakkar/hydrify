package com.rocmine.wip.hydrify.ui.welcomPage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.rocmine.wip.hydrify.R;
import com.rocmine.wip.hydrify.ui.UserInfoActivity.UserInfoActivity;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private OnboardingAdapter adapter;
    private List<OnboardingItem> onboardingItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);

        onboardingItems = new ArrayList<>();
        onboardingItems.add(new OnboardingItem(
                R.drawable.bottle_of_water, // replace with your actual drawable
                "Track your daily water intake with Us.",
                "Achieve your hydration goals with a simple tap!"));
        onboardingItems.add(new OnboardingItem(
                R.drawable.world_water_day, // replace with your actual drawable
                "Smart Reminders\nTailored to You",
                "Quick and easy to set your hydration goal & then track your daily water intake progress."));
        onboardingItems.add(new OnboardingItem(
                R.drawable.bottle_of_water2, // replace with your actual drawable
                "Easy to Use – Drink, Tap, Repeat",
                "Staying hydrated every day is easy with Drops Water Tracker."));

        adapter = new OnboardingAdapter(onboardingItems, viewPager);
        viewPager.setAdapter(adapter);
    }

    private static class OnboardingItem {
        int imageResId;
        String title;
        String description;

        OnboardingItem(int imageResId, String title, String description) {
            this.imageResId = imageResId;
            this.title = title;
            this.description = description;
        }
    }

    private static class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {
        private final List<OnboardingItem> onboardingItems;
        private final ViewPager2 viewPager;

        OnboardingAdapter(List<OnboardingItem> onboardingItems, ViewPager2 viewPager) {
            this.onboardingItems = onboardingItems;
            this.viewPager = viewPager;
        }

        @NonNull
        @Override
        public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_onboarding_info_page, parent, false);
            return new OnboardingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
            OnboardingItem item = onboardingItems.get(position);
            holder.imageView.setImageResource(item.imageResId);
            holder.titleTextView.setText(item.title);
            holder.descriptionTextView.setText(item.description);

            // Set up progress dots
            holder.progressDots.removeAllViews();
            for (int i = 0; i < onboardingItems.size(); i++) {
                View dot = new View(holder.progressDots.getContext());
                int size = (int) (holder.progressDots.getContext().getResources().getDisplayMetrics().density * 8);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                params.setMargins(size / 2, 0, size / 2, 0);
                dot.setLayoutParams(params);
                if (i == position) {
                    dot.setBackgroundResource(R.drawable.dot_active);
                } else {
                    dot.setBackgroundResource(R.drawable.dot_inactive);
                }
                holder.progressDots.addView(dot);
            }

            // Set up button text
            if (position == onboardingItems.size() - 1) {
                holder.nextButton.setText("GET STARTED");
            } else {
                holder.nextButton.setText("NEXT");
            }

            holder.nextButton.setOnClickListener(v -> {
                if (position < onboardingItems.size() - 1) {
                    viewPager.setCurrentItem(position + 1);
                } else {
                    // Start UserInfoActivity
                    Context context = v.getContext();
                    Intent intent = new Intent(context, UserInfoActivity.class);
                    context.startActivity(intent);
                    if (context instanceof AppCompatActivity) {
                        ((AppCompatActivity) context).finish();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return onboardingItems.size();
        }

        static class OnboardingViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView titleTextView;
            TextView descriptionTextView;
            LinearLayout progressDots;
            Button nextButton;

            OnboardingViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.illustrationImageView);
                titleTextView = itemView.findViewById(R.id.titleTextView);
                descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
                progressDots = itemView.findViewById(R.id.progressDots);
                nextButton = itemView.findViewById(R.id.nextButton);
            }
        }
    }
}