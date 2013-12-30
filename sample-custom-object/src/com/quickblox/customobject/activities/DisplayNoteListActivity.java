package com.quickblox.customobject.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.quickblox.customobject.R;
import com.quickblox.customobject.adapter.NoteListAdapter;

public class DisplayNoteListActivity extends Activity implements AdapterView.OnItemClickListener {

    private final String POSITION = "position";
    private ListView notesLv;
    private NoteListAdapter noteListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_list);
        initialize();
    }

    @Override
    public void onResume() {
        super.onResume();
        noteListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent intent = new Intent(this, ShowNoteActivity.class);
        intent.putExtra(POSITION, position);
        startActivity(intent);
    }

    private void initialize() {
        notesLv = (ListView) findViewById(R.id.note_list);
        notesLv.setOnItemClickListener(this);
        noteListAdapter = new NoteListAdapter(this);
        notesLv.setAdapter(noteListAdapter);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_new_note:
                Intent intent = new Intent(this, AddNewNoteActivity.class);
                startActivity(intent);
                break;
        }
    }
}
