package com.example.cpsc362dayplanner;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class calendarViewActivity extends AppCompatActivity {
    private LinearLayout linearLayoutNotesContainer;
    private List<Note> notes;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
    private String currentSelectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_view);

        CalendarView calendarView = findViewById(R.id.calendarView);
        linearLayoutNotesContainer = findViewById(R.id.linearLayoutNotesContainer);

        RelativeLayout relativeLayout = findViewById(R.id.calendarLayout);

        AnimationDrawable animationDrawable = (AnimationDrawable) relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(7500);
        animationDrawable.setExitFadeDuration(10000);
        animationDrawable.start();

        // Initialize with today's date
        currentSelectedDate = sdf.format(new Date(calendarView.getDate()));

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // month is 0-indexed, add 1 for correct display
            currentSelectedDate = (month + 1) + "/" + dayOfMonth + "/" + year;
            displayNotesForSelectedDate(currentSelectedDate);
        });
    }

    private void displayNotesForSelectedDate(String selectedDate) {
        // Clear any previous views in the container that might be from another date's notes
        linearLayoutNotesContainer.removeAllViews();
        notes = loadData();

        for (Note note : notes) {
            if (note.getDate().equals(selectedDate)) {
                // Create a TextView for each note on the selected date and add it to the container
                TextView noteView = new TextView(this);
                boolean isUrgent = getUrgentStatus(note.getTitle());

                // Set the text for the TextView
                String noteText = getString(R.string.note_format, note.getTitle(), note.getContent());
                noteView.setText(noteText);

                // Customize your TextView appearance here
                noteView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set the size of the text
                noteView.setTextColor(Color.BLACK); // Set the text color
                noteView.setPadding(16, 16, 16, 16); // Set padding for the TextView (in pixels)

                if (isUrgent) {
                    // Set a rounded corner background for urgent notes
                    setRoundedCornerBackground(noteView, Color.parseColor("#C21807"));
                } else {
                    // Set a rounded corner background for non-urgent notes
                    setRoundedCornerBackground(noteView, Color.LTGRAY);
                }

                // Optional: Add margins to the TextView
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                layoutParams.setMargins(16, 16, 16, 16); // Set margins (in pixels)
                noteView.setLayoutParams(layoutParams);

                // Add the TextView to the LinearLayout container
                linearLayoutNotesContainer.addView(noteView);
            }
        }
    }

    private List<Note> loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("DayPlanner", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("notes", null);
        Type type = new TypeToken<ArrayList<Note>>() {
        }.getType();
        List<Note> notes = gson.fromJson(json, type);
        if (notes == null) {
            notes = new ArrayList<>(); // return an empty list instead of null
        }
        return notes;
    }

    private boolean getUrgentStatus(String title) {
        SharedPreferences sharedPreferences = getSharedPreferences("DayPlanner", MODE_PRIVATE);
        return sharedPreferences.getBoolean(title + "_urgent", false);
    }

    private void setRoundedCornerBackground(View view, int backgroundColor) {
        // Set a rounded corner background for the view
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(backgroundColor);
        gradientDrawable.setCornerRadius(20); // Adjust the radius as needed
        view.setBackground(gradientDrawable);
    }

}