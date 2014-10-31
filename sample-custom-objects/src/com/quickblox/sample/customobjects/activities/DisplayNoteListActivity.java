package com.quickblox.sample.customobjects.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.adapter.NoteListAdapter;

public class DisplayNoteListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private final String POSITION = "position";
    private ListView notesListView;
    private NoteListAdapter noteListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_list);
        initUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        noteListAdapter.notifyDataSetChanged();
    }

    private void initUI() {
        notesListView = (ListView) findViewById(R.id.notes_listview);
        notesListView.setOnItemClickListener(this);
        noteListAdapter = new NoteListAdapter(this);
        notesListView.setAdapter(noteListAdapter);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_new_note_button:
                Intent intent = new Intent(this, AddNewNoteActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent intent = new Intent(this, ShowNoteActivity.class);
        intent.putExtra(POSITION, position);
        startActivity(intent);
    }
}