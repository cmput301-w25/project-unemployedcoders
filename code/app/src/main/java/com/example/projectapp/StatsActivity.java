package com.example.projectapp;

import android.animation.ObjectAnimator;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.DatePicker;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;  // Added import for Random

public class StatsActivity extends AppCompatActivity {

    private PieChart pieChart;
    private Button backButton, dailyButton, monthlyButton, customRangeButton, spinButton;
    private List<MoodEvent> moodEvents; // loaded from Firebase via FirebaseSync
    private Date customStartDate, customEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        pieChart = findViewById(R.id.pieChart);
        backButton = findViewById(R.id.button_back);
        dailyButton = findViewById(R.id.button_daily);
        monthlyButton = findViewById(R.id.button_monthly);
        customRangeButton = findViewById(R.id.button_custom_range);
        spinButton = findViewById(R.id.button_spin);

        pieChart.setRotationEnabled(true);

        backButton.setOnClickListener(v -> {
            Snackbar.make(findViewById(R.id.stats_root), "Returning...", Snackbar.LENGTH_SHORT).show();
            finish();
        });

        loadMoodEventsFromFirebase();

        dailyButton.setOnClickListener(v -> {
            List<MoodEvent> dailyEvents = filterEventsForToday(moodEvents);
            generatePieChart(dailyEvents, "Moods (Today)");
        });

        monthlyButton.setOnClickListener(v -> {
            List<MoodEvent> monthEvents = filterEventsForPastMonth(moodEvents);
            generatePieChart(monthEvents, "Moods (Past Month)");
        });

        customRangeButton.setOnClickListener(v -> showDateRangeDialog());

        spinButton.setOnClickListener(v -> spinTheChart());
    }

    private void loadMoodEventsFromFirebase() {
        FirebaseSync fb = FirebaseSync.getInstance();
        fb.fetchUserProfileObject(new UserProfileCallback() {
            @Override
            public void onUserProfileLoaded(UserProfile userProfile) {
                if (userProfile != null && userProfile.getHistory() != null) {
                    moodEvents = userProfile.getHistory().getEvents();
                    if (moodEvents == null || moodEvents.isEmpty()) {
                        Snackbar.make(findViewById(R.id.stats_root), "No mood events found", Snackbar.LENGTH_SHORT).show();
                    } else {
                        dailyButton.performClick();
                    }
                } else {
                    moodEvents = new ArrayList<>();
                    Snackbar.make(findViewById(R.id.stats_root), "No mood events found", Snackbar.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Exception e) {
                moodEvents = new ArrayList<>();
                Snackbar.make(findViewById(R.id.stats_root), "Error loading data: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void generatePieChart(List<MoodEvent> events, String centerText) {
        if (events == null || events.isEmpty()) {
            pieChart.clear();
            Snackbar.make(findViewById(R.id.stats_root), "No data for this range", Snackbar.LENGTH_SHORT).show();
            return;
        }

        Map<String, Integer> moodCounts = new HashMap<>();
        Map<String, Integer> moodColorMap = new HashMap<>();

        for (MoodEvent event : events) {
            String emoticon = getString(event.getEmoticonResource());
            String moodLabel = emoticon + " " + event.getEmotionalState();
            int count = moodCounts.getOrDefault(moodLabel, 0);
            moodCounts.put(moodLabel, count + 1);
            if (!moodColorMap.containsKey(moodLabel)) {
                int color = ContextCompat.getColor(this, event.getColorResource());
                moodColorMap.put(moodLabel, color);
            }
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> sliceColors = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : moodCounts.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
            sliceColors.add(moodColorMap.get(entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Mood Distribution");
        dataSet.setColors(sliceColors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(14f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText(centerText);
        pieChart.setCenterTextSize(18f);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.animateXY(1000, 1000, Easing.EaseInOutQuad, Easing.EaseInOutQuad);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setTextSize(12f);
        legend.setWordWrapEnabled(true);

        pieChart.invalidate();
    }

    private void spinTheChart() {
        pieChart.setRotationEnabled(true);
        float startAngle = pieChart.getRotationAngle();
        float endAngle = startAngle + 360f * 3 + new Random().nextInt(360);
        ObjectAnimator animator = ObjectAnimator.ofFloat(pieChart, "rotationAngle", startAngle, endAngle);
        animator.setDuration(2000);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (pieChart.getData() != null && pieChart.getData().getDataSetCount() > 0) {
                    int entryCount = pieChart.getData().getDataSet().getEntryCount();
                    int randomIndex = new Random().nextInt(entryCount);
                    pieChart.highlightValue(randomIndex, 0);
                    Snackbar.make(findViewById(R.id.stats_root), "Winning Mood: " +
                                    pieChart.getData().getDataSet().getEntryForIndex(randomIndex).getLabel(),
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        animator.start();
    }

    private void showDateRangeDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog startPicker = new DatePickerDialog(this,
                (DatePicker view, int year1, int month1, int dayOfMonth) -> {
                    calendar.set(year1, month1, dayOfMonth, 0, 0, 0);
                    customStartDate = calendar.getTime();
                    showEndDateDialog();
                },
                year, month, day);
        startPicker.setTitle("Select Start Date");
        startPicker.show();
    }

    private void showEndDateDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog endPicker = new DatePickerDialog(this,
                (DatePicker view, int year2, int month2, int dayOfMonth) -> {
                    calendar.set(year2, month2, dayOfMonth, 23, 59, 59);
                    customEndDate = calendar.getTime();
                    applyCustomRange();
                },
                year, month, day);
        endPicker.setTitle("Select End Date");
        endPicker.show();
    }

    private void applyCustomRange() {
        if (customStartDate == null || customEndDate == null) {
            Snackbar.make(findViewById(R.id.stats_root), "Date range not selected properly", Snackbar.LENGTH_SHORT).show();
            return;
        }
        List<MoodEvent> filtered = filterEventsForRange(moodEvents, customStartDate, customEndDate);
        generatePieChart(filtered, "Moods (Custom Range)");
    }

    private List<MoodEvent> filterEventsForRange(List<MoodEvent> events, Date startDate, Date endDate) {
        List<MoodEvent> result = new ArrayList<>();
        if (events == null) return result;
        for (MoodEvent e : events) {
            Date d = e.getDate();
            if (d != null && !d.before(startDate) && !d.after(endDate)) {
                result.add(e);
            }
        }
        return result;
    }

    private List<MoodEvent> filterEventsForToday(List<MoodEvent> events) {
        List<MoodEvent> result = new ArrayList<>();
        if (events == null) return result;
        Calendar startOfDay = Calendar.getInstance();
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);
        Date todayStart = startOfDay.getTime();
        for (MoodEvent e : events) {
            if (e.getDate() != null && e.getDate().after(todayStart)) {
                result.add(e);
            }
        }
        return result;
    }

    private List<MoodEvent> filterEventsForPastMonth(List<MoodEvent> events) {
        List<MoodEvent> result = new ArrayList<>();
        if (events == null) return result;
        Calendar oneMonthAgo = Calendar.getInstance();
        oneMonthAgo.add(Calendar.MONTH, -1);
        Date cutoff = oneMonthAgo.getTime();
        for (MoodEvent e : events) {
            if (e.getDate() != null && e.getDate().after(cutoff)) {
                result.add(e);
            }
        }
        return result;
    }
}
