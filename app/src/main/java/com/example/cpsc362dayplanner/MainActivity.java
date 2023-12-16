package com.example.cpsc362dayplanner;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Iterator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView emptyTextView;
    LinearLayout linearLayoutCardContainer;
    Button addButton;
    List<Note> notes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emptyTextView = findViewById(R.id.emptyTextView);
        addButton = findViewById(R.id.button1);
        linearLayoutCardContainer = findViewById(R.id.linearLayoutCardContainer);

        notes = loadData();
        if(notes != null) {
            for (Note note : notes) {
                addCard(note);
            }
        }

        addButton.setOnClickListener(v -> openAddDialog());
    }

    private void addCard(Note note) {
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

        // Add TextViews to the LinearLayout of the CardView
        ll.addView(titleText);
        ll.addView(contentText);
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

        editButton.setOnClickListener(v -> openEditDialog(titleText, contentText, note));
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
            emptyTextView.setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Add CardView to the linearLayoutCardContainer
        linearLayoutCardContainer.addView(cardView);
        emptyTextView.setVisibility(View.GONE);
    }

    private void openEditDialog(TextView titleText, TextView contentText, Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Item");

        final EditText inputTitle = new EditText(this);
        final EditText inputContent = new EditText(this);

        inputTitle.setText(titleText.getText());
        inputContent.setText(contentText.getText());

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(inputTitle);
        ll.addView(inputContent);
        builder.setView(ll);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newTitle = inputTitle.getText().toString();
            String newContent = inputContent.getText().toString();

            titleText.setText(newTitle);
            contentText.setText(newContent);

            note.setTitle(newTitle);
            note.setContent(newContent);

            saveData(notes);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void openAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Item");

        final EditText inputTitle = new EditText(this);
        final EditText inputContent = new EditText(this);

        inputTitle.setHint("Enter Title");
        inputContent.setHint("Enter Content");

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(inputTitle);
        ll.addView(inputContent);
        builder.setView(ll);

        builder.setPositiveButton("OK", (dialog, which) -> {
            Note newNote = new Note(inputTitle.getText().toString(), inputContent.getText().toString());
            addCard(newNote);
            // Add the new note to the notes list and save.
            if (notes == null) {
                notes = new ArrayList<>();
            }
            notes.add(newNote);
            saveData(notes);
        });
        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void saveData(List<Note> notes) {
        SharedPreferences sharedPreferences = getSharedPreferences("DayPlanner", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(notes);
        editor.putString("notes", json);
        editor.apply();
    }

    private List<Note> loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("DayPlanner", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("notes", null);
        Type type = new TypeToken<ArrayList<Note>>() {}.getType();
        List<Note> notes = gson.fromJson(json, type);

        // ensure that notes is never null
        if (notes == null) {
            notes = new ArrayList<>();
        }

        emptyTextView.setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);
        return notes;
    }
}