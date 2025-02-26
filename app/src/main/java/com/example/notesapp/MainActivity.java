package com.example.notesapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "NotePrefs";
    private static final String NOTES_KEY = "notes";
    private List<Note> noteList;
    private LinearLayout notesContainer;
    private EditText titleEditText, contentEditText, searchEditText;
    private Button saveButton, updateButton;
    private Note selectedNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notesContainer = findViewById(R.id.notesContainer);
        titleEditText = findViewById(R.id.titleEditText);
        contentEditText = findViewById(R.id.contentEditText);
        searchEditText = findViewById(R.id.searchEditText);
        saveButton = findViewById(R.id.saveButton);
        updateButton = findViewById(R.id.updateButton);

        noteList = new ArrayList<>();

        saveButton.setOnClickListener(v -> saveNote());
        updateButton.setOnClickListener(v -> updateNote());

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadNotesFromPreferences();
        displayNotes();
    }

    private void saveNote() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        if (!title.isEmpty() && !content.isEmpty()) {
            Note note = new Note(title, content);
            noteList.add(note);
            saveNotesToPreferences();
            createNoteView(note);
            clearInputFields();
        }
    }

    private void updateNote() {
        if (selectedNote != null) {
            selectedNote.setTitle(titleEditText.getText().toString().trim());
            selectedNote.setContent(contentEditText.getText().toString().trim());
            saveNotesToPreferences();
            refreshNoteViews();
            clearInputFields();
            updateButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.VISIBLE);
        }
    }

    private void createNoteView(final Note note) {
        View noteView = LayoutInflater.from(this).inflate(R.layout.note_items, null);
        TextView titleTextView = noteView.findViewById(R.id.titleTextView);
        TextView contentTextView = noteView.findViewById(R.id.contentTextView);
        Button editButton = noteView.findViewById(R.id.editButton);
        Button deleteButton = noteView.findViewById(R.id.deleteButton);

        titleTextView.setText(note.getTitle());
        contentTextView.setText(note.getContent());

        editButton.setOnClickListener(v -> {
            titleEditText.setText(note.getTitle());
            contentEditText.setText(note.getContent());
            selectedNote = note;
            saveButton.setVisibility(View.GONE);
            updateButton.setVisibility(View.VISIBLE);
        });

        deleteButton.setOnClickListener(v -> showDeleteDialog(note));

        notesContainer.addView(noteView);
    }

    private void showDeleteDialog(Note note) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    noteList.remove(note);
                    saveNotesToPreferences();
                    refreshNoteViews();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void filterNotes(String query) {
        notesContainer.removeAllViews();
        for (Note note : noteList) {
            if (note.getTitle().toLowerCase().contains(query.toLowerCase())) {
                createNoteView(note);
            }
        }
    }

    private void refreshNoteViews() {
        notesContainer.removeAllViews();
        for (Note note : noteList) {
            createNoteView(note);
        }
    }

    private void saveNotesToPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(noteList);
        editor.putString(NOTES_KEY, json);
        editor.apply();
    }

    private void loadNotesFromPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(NOTES_KEY, null);
        Type type = new TypeToken<ArrayList<Note>>() {}.getType();
        noteList = gson.fromJson(json, type);
        if (noteList == null) {
            noteList = new ArrayList<>();
        }
    }

    private void displayNotes() {
        notesContainer.removeAllViews(); // Clear existing views
        for (Note note : noteList) {
            createNoteView(note); // Recreate note views
        }
    }

    private void clearInputFields() {
        titleEditText.setText("");
        contentEditText.setText("");
    }

    private static class Note {
        private String title;
        private String content;

        public Note(String title, String content) {
            this.title = title;
            this.content = content;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
