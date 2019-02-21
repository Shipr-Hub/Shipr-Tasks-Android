package io.github.yhdesai.tasks;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<DeveloperToDo> {
    public MessageAdapter(Context context, int resource, List<DeveloperToDo> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView messageTextView =  convertView.findViewById(android.R.id.text1);

        DeveloperToDo message = getItem(position);

            messageTextView.setVisibility(View.VISIBLE);
            messageTextView.setText(message.getText());

        return convertView;
    }
}
