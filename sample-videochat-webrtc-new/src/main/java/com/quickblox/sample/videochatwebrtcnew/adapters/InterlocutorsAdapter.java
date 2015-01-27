package com.quickblox.sample.videochatwebrtcnew.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.quickblox.sample.videochatwebrtcnew.R;

/**
 * Created by tereha on 27.01.15.
 */
public class InterlocutorsAdapter  extends ArrayAdapter<String> {


    private final Context context;
    private final String[] interlocutors;


    public InterlocutorsAdapter(Context context, String[] interlocutors) {
        super(context, R.layout.list_item_interlocutors, interlocutors);
        this.context = context;
        this.interlocutors = interlocutors;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View interlocutorsListItem = inflater.inflate(R.layout.list_item_interlocutors, parent, false);

        TextView numberOfList = (TextView) interlocutorsListItem.findViewById(R.id.interlocutorNumber);
        numberOfList.setText(String.valueOf(position +1));

        TextView interlocutorName = (TextView) interlocutorsListItem.findViewById(R.id.interlocutorName);
        interlocutorName.setText(interlocutors[position]);

        CheckBox interlocutorCheckBox = (CheckBox) interlocutorsListItem.findViewById(R.id.interlocutorCheckBox);
        //interlocutorCheckBox

        return interlocutorsListItem;

    }
}