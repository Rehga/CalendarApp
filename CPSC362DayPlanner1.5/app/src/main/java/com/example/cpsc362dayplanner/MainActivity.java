package com.example.cpsc362dayplanner;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    TextView emptyTextView;
    LinearLayout linearLayoutCardContainer;
    Button addButton;
    List<Note> notes;
    private Map<String, Boolean> urgentStatusMap = new HashMap<>();
    private SwitchCompat toggleUrgent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout relativeLayout = findViewById(R.id.mainLayout);

        AnimationDrawable animationDrawable = (AnimationDrawable) relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(7500);
        animationDrawable.setExitFadeDuration(10000);
        animationDrawable.start();

        emptyTextView = findViewById(R.id.emptyTextView);
        addButton = findViewById(R.id.button1);
        linearLayoutCardContainer = findViewById(R.id.linearLayoutCardContainer);

        // button to get to calendarView
        ImageButton calendarViewButton = findViewById(R.id.calendarViewButton);
        calendarViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, calendarViewActivity.class);
                startActivity(intent);
            }
        });

        toggleUrgent = new SwitchCompat(this);  // Initialize toggleUrgent here
        toggleUrgent.setText("Urgent");
        toggleUrgent.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        notes = loadData();
        if (notes != null) {
            for (Note note : notes) {
                boolean isUrgent = urgentStatusMap.containsKey(note.getTitle()) && Boolean.TRUE.equals(urgentStatusMap.get(note.getTitle()));
                makeCard(note, isUrgent);
            }
        } else {
            updateEmptyTextViewVisibility();
        }

        addButton.setOnClickListener(v -> openAddDialog());
    }

    private void makeCard(Note note, boolean isUrgent) {
        // Create new CardView and set its properties
        CardView cardView = new CardView(this);
        LinearLayout ll = new LinearLayout(this);
        TextView titleText = new TextView(this);
        TextView contentText = new TextView(this);

        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        ll.setPadding(20, 20, 20, 20);

        titleText.setText(note.getTitle());
        titleText.setTextSize(32);
        titleText.setTextColor(Color.BLACK);
        titleText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        contentText.setText(note.getContent());
        contentText.setTextSize(18);
        contentText.setVisibility(View.GONE); // initially hide the content

        Button editButton = new Button(this);
        editButton.setText("Edit");
        editButton.setVisibility(View.GONE);  // initially hidden
        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");
        deleteButton.setVisibility(View.GONE);  // initially hidden

        TextView dateTimeText = new TextView(this);
        dateTimeText.setTextSize(18);
        dateTimeText.setTextColor(Color.BLACK);
        dateTimeText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Set the combined date and time text
        String dateTimeString = getDateTimeString(note.getDate(), note.getTime());
        if (!dateTimeString.isEmpty()) {
            dateTimeText.setText(dateTimeString);
            dateTimeText.setVisibility(View.VISIBLE);
        } else {
            dateTimeText.setVisibility(View.GONE);
        }

        // Add TextViews to the LinearLayout of the CardView
        ll.addView(titleText);
        ll.addView(contentText);
        ll.addView(dateTimeText);
        ll.addView(editButton);
        ll.addView(deleteButton);

        // Add LinearLayout to the CardView
        cardView.addView(ll);

        // Set layout parameters for the CardView
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int margin = getResources().getDimensionPixelSize(R.dimen.card_margin);
        params.setMargins(margin, margin, margin, margin);
        cardView.setLayoutParams(params);


        // Set click listener to expand/collapse content text
        cardView.setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition(linearLayoutCardContainer, new AutoTransition());
            if(contentText.getVisibility() == View.VISIBLE) {
                contentText.setVisibility(View.GONE);
                editButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
            } else {
                contentText.setVisibility(View.VISIBLE);
                editButton.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.VISIBLE);
            }
        });

        applyUrgencyStyle(cardView, isUrgent);

        editButton.setOnClickListener(v -> openEditDialog(titleText, contentText, note, cardView));
        deleteButton.setOnClickListener(v -> {
            // Remove card from UI
            linearLayoutCardContainer.removeView(cardView);

            // Remove note from saved notes and update storage
            Iterator<Note> iterator = notes.iterator();
            while (iterator.hasNext()) {
                Note iterNote = iterator.next();
                if (iterNote.getTitle().equals(note.getTitle())) {
                    iterator.remove();
                    break;
                }
            }
            saveData(notes);
            updateEmptyTextViewVisibility();
        });

        int insertIndex = findInsertIndex(note);

        // Add CardView to the linearLayoutCardContainer
        linearLayoutCardContainer.addView(cardView, insertIndex);

        if (notes == null) {
            notes = new ArrayList<>();
        }

        sendNoteAddedBroadcast();
        emptyTextView.setVisibility(View.GONE);
    }

    private int findInsertIndex(Note newNote) {
        int size = linearLayoutCardContainer.getChildCount();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy h:mm a", Locale.getDefault());

        for (int i = 0; i < size; i++) {
            CardView childCardView = (CardView) linearLayoutCardContainer.getChildAt(i);
            LinearLayout ll = (LinearLayout) childCardView.getChildAt(0);
            TextView dateTimeText = (TextView) ll.getChildAt(2);

            if (dateTimeText.getVisibility() == View.VISIBLE) {
                String existingDateTimeString = dateTimeText.getText().toString();

                try {
                    Date newDateTime = dateFormat.parse(newNote.getDate() + " " + newNote.getTime());
                    Date existingDateTime = dateFormat.parse(existingDateTimeString);

                    if (newDateTime.before(existingDateTime)) {
                        return i; // Found the correct position
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return size; // Insert at the end if no suitable position is found
    }


    private void openEditDialog(TextView titleText, TextView contentText, Note note, CardView cardView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Item");

        final EditText inputTitle = new EditText(this);
        final EditText inputContent = new EditText(this);

        inputTitle.setText(note.getTitle());
        inputContent.setText(note.getContent());

        final Button dateButton = new Button(this);
        dateButton.setText(note.getDate().isEmpty() ? "Set Date" : note.getDate());
        final Button timeButton = new Button(this);
        timeButton.setText(note.getTime().isEmpty() || note.getTime().equals("Set Time") ? "Set Time" : note.getTime());

        // Set onClickListener for date button
        dateButton.setOnClickListener(v -> showDatePickerDialog(dateButton));

        // Set onClickListener for time button
        timeButton.setOnClickListener(v -> showTimePickerDialog(timeButton));

        SwitchCompat toggleUrgent = new SwitchCompat(this);
        toggleUrgent.setText("Urgent");
        toggleUrgent.setChecked(urgentStatusMap.containsKey(note.getTitle()) && Boolean.TRUE.equals(urgentStatusMap.get(note.getTitle())));
        toggleUrgent.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(inputTitle);
        ll.addView(inputContent);
        ll.addView(dateButton);
        ll.addView(timeButton);
        ll.addView(toggleUrgent);
        builder.setView(ll);

        builder.setPositiveButton("OK", (dialog, which) -> {
            note.setTitle(inputTitle.getText().toString());
            note.setContent(inputContent.getText().toString());
            note.setDate(dateButton.getText().toString());
            // Only save the time if it's been set by the user, not the default "Set Time" text
            if (!timeButton.getText().toString().equals("Set Time")) {
                note.setTime(timeButton.getText().toString());
            } else {
                note.setTime(""); // Set to empty if "Set Time" was not changed
            }
            boolean isUrgent = toggleUrgent.isChecked();
            urgentStatusMap.put(note.getTitle(), isUrgent);
            updateCard(note, cardView);
            saveData(notes);
            sortNotes();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateCard(Note note, CardView cardView) {
        // Assuming the cardView's child at index 0 is the LinearLayout containing the TextViews
        LinearLayout ll = (LinearLayout) cardView.getChildAt(0);
        TextView titleText = (TextView) ll.getChildAt(0);  // The title TextView
        TextView contentText = (TextView) ll.getChildAt(1);  // The content TextView
        TextView dateTimeText = (TextView) ll.getChildAt(2);

        titleText.setText(note.getTitle());
        contentText.setText(note.getContent());
        String dateTimeString = getDateTimeString(note.getDate(), note.getTime());
        if (!dateTimeString.isEmpty()) {
            dateTimeText.setText(dateTimeString);
            dateTimeText.setVisibility(View.VISIBLE);
        } else {
            dateTimeText.setVisibility(View.GONE);
        }
        boolean isUrgent = urgentStatusMap.containsKey(note.getTitle()) && Boolean.TRUE.equals(urgentStatusMap.get(note.getTitle()));
        applyUrgencyStyle(cardView, isUrgent);
    }

    private void openAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Item");

        final EditText inputTitle = new EditText(this);
        inputTitle.setHint("Enter Title");
        final EditText inputContent = new EditText(this);
        inputContent.setHint("Enter Content");
        final Button dateButton = new Button(this);
        dateButton.setText("Set Date");
        final Button timeButton = new Button(this);
        timeButton.setText("Set Time");

        // Set OnClickListener for the date button
        dateButton.setOnClickListener(v -> showDatePickerDialog(dateButton));
        // Set OnClickListener for the time button
        timeButton.setOnClickListener(v -> showTimePickerDialog(timeButton));

        SwitchCompat toggleUrgent = new SwitchCompat(this);
        toggleUrgent.setText("Urgent");
        toggleUrgent.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(inputTitle);
        ll.addView(inputContent);
        ll.addView(dateButton);
        ll.addView(timeButton);
        ll.addView(toggleUrgent);

        builder.setView(ll);

        builder.setPositiveButton("OK", (dialog, which) -> {
            Note newNote = new Note(inputTitle.getText().toString(), inputContent.getText().toString());
            newNote.setDate(dateButton.getText().toString());
            newNote.setTime(timeButton.getText().toString());
            boolean isUrgent = toggleUrgent.isChecked();
            urgentStatusMap.put(newNote.getTitle(), isUrgent);
            makeCard(newNote, isUrgent);
            notes.add(newNote);
            saveData(notes);
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        // Set the dialog cancel listener
        dialog.setOnCancelListener(dialogInterface -> {
            // This will be called when the dialog is canceled, make the main view visible again
            findViewById(R.id.mainLayout).setVisibility(View.VISIBLE);
        });
        // Set the dialog dismiss listener
        dialog.setOnDismissListener(dialogInterface -> {
            // This will be called when the dialog is dismissed, make the main view visible again
            findViewById(R.id.mainLayout).setVisibility(View.VISIBLE);
        });
        // Hide the main layout before showing the dialog
        findViewById(R.id.mainLayout).setVisibility(View.INVISIBLE);
        // Show the dialog
        dialog.show();
        // Get the Window of the dialog
        Window window = dialog.getWindow();
        // If the Window is not null, change the gravity
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

            // Copy the layout parameters of the existing Window
            layoutParams.copyFrom(window.getAttributes());

            // Set the gravity of the Window to the top
            layoutParams.gravity = Gravity.TOP;

            // Apply the new layout parameters
            window.setAttributes(layoutParams);
        }
    }
    private void saveData(List<Note> notes) {
        SharedPreferences sharedPreferences = getSharedPreferences("DayPlanner", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Map.Entry<String, Boolean> entry : urgentStatusMap.entrySet()) {
            editor.putBoolean(entry.getKey() + "_urgent", entry.getValue());
        }
        Gson gson = new Gson();
        String json = gson.toJson(notes);
        editor.putString("notes", json);
        editor.apply();
        updateEmptyTextViewVisibility();
    }
    private List<Note> loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("DayPlanner", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("notes", null);
        Type type = new TypeToken<ArrayList<Note>>() {}.getType();
        List<Note> notes = gson.fromJson(json, type);

        if (notes != null) {
            for (Note note : notes) {
                boolean isUrgent = sharedPreferences.getBoolean(note.getTitle() + "_urgent", false);
                urgentStatusMap.put(note.getTitle(), isUrgent);
            }
        }

        updateEmptyTextViewVisibility();
        return notes;
    }
    private void showDatePickerDialog(Button dateButton) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String date = (monthOfYear + 1) + "/" + dayOfMonth + "/" + year1;
                    dateButton.setText(date);
                }, year, month, day);

        datePickerDialog.show();
    }
    private void showTimePickerDialog(Button timeButton) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute1) -> {
                    // Determine whether the hour is AM or PM
                    String amPm = hourOfDay < 12 ? "AM" : "PM";
                    // Adjust hour to 12-hour format
                    int hourToShow = hourOfDay % 12;
                    if (hourToShow == 0) hourToShow = 12;

                    // Format the minute with leading zero if necessary
                    String minuteFormatted = String.format(Locale.getDefault(), "%02d", minute1);

                    // Format the time without a leading zero for the hour
                    String time = String.format(Locale.getDefault(), "%d:%s %s", hourToShow, minuteFormatted, amPm);
                    timeButton.setText(time);
                }, hour, minute, false); // false for 12-hour time

        timePickerDialog.show();
    }

    private void updateEmptyTextViewVisibility() {
        if (notes == null || notes.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            emptyTextView.setVisibility(View.GONE);
        }
    }
    private String getDateTimeString(String date, String time) {
        boolean setTime = time == null || time.isEmpty() || time.equals("Set Time");
        boolean setDate = date == null || date.isEmpty() || date.equals("Set Date");

        if ((setDate) && (setTime)) {
            return "";  // Return empty if both date and time are not set
        }
        // Only date available
        if (setTime) {
            return date;
        }
        // Only time available
        if (setDate) {
            return time;
        }
        // Both date and time available, concatenate them
        return date + " " + time;
    }

    private void sendNoteAddedBroadcast() {
        Intent intent = new Intent("com.example.NEW_NOTE_CREATED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void applyUrgencyStyle(CardView cardView, boolean isUrgent) {
        int backgroundColor = isUrgent ? ContextCompat.getColor(this, R.color.urgentRed) : Color.WHITE;
        int radius = 20; // Set the corner radius as needed

        // Create a rounded corner drawable
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(backgroundColor);
        drawable.setCornerRadius(radius);

        // Set the drawable as the background of the CardView
        cardView.setBackground(drawable);
    }
    private void sortNotes() {
        if (notes != null) {
            Collections.sort(notes, new Comparator<Note>() {
                @Override
                public int compare(Note note1, Note note2) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy h:mm a", Locale.getDefault());

                    try {
                        Date date1 = dateFormat.parse(note1.getDate() + " " + note1.getTime());
                        Date date2 = dateFormat.parse(note2.getDate() + " " + note2.getTime());

                        return date1.compareTo(date2);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return 0;
                    }
                }
            });

            // Remove existing views from linearLayoutCardContainer
            linearLayoutCardContainer.removeAllViews();

            // Add sorted notes to linearLayoutCardContainer
            for (Note note : notes) {
                boolean isUrgent = urgentStatusMap.containsKey(note.getTitle()) && Boolean.TRUE.equals(urgentStatusMap.get(note.getTitle()));
                makeCard(note, isUrgent);
            }
        }
    }

}