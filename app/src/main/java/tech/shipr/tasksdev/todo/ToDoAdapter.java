package tech.shipr.tasksdev.todo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

class ToDoAdapter extends ArrayAdapter<DeveloperToDo> {
    public ToDoAdapter(Context context, int resource, List<DeveloperToDo> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(android.R.layout.simple_list_item_checked, parent, false);
        }

        TextView messageTextView = convertView.findViewById(android.R.id.text1);

        final DeveloperToDo mToDo = getItem(position);

        messageTextView.setVisibility(View.VISIBLE);
        assert mToDo != null;
        messageTextView.setText(mToDo.getText());


        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                CheckedTextView textview = (CheckedTextView) v;
                if (!textview.isChecked()) {
                    textview.setChecked(true);
                    textview.setPaintFlags(textview.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                   // Toast.makeText(getContext(), mToDo.getText() + " : " + textview.isChecked(), Toast.LENGTH_SHORT).show();
                    String key = mToDo.getKey();
                    ToDoActivity.setToDoAsDone(mToDo, key);
                } else {
                    // TODo is already selected as complete
                }
            }
        });

        return convertView;
    }


}
