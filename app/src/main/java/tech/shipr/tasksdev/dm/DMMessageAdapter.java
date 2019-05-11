package tech.shipr.tasksdev.dm;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import tech.shipr.tasksdev.R;

public class DMMessageAdapter extends ArrayAdapter<DMMessage> {

    public DMMessageAdapter(Context context, int resource, List<DMMessage> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_dm_message, parent, false);
        }

        ImageView photoImageView = convertView.findViewById(R.id.photoImageView);
        TextView messageTextView = convertView.findViewById(R.id.messageTextView);
      //  TextView authorTextView = (TextView) convertView.findViewById(R.id.nameTextView);

        // Get message from View
        DMMessage message = getItem(position);



        boolean isPhoto = message.getPhotoUrl() != null;

        //Load message based on type
        if (isPhoto) {


            //Get Properties
            Context context = photoImageView.getContext();
            String text = message.getPhotoUrl();

            //Prepare view for image
            messageTextView.setVisibility(View.GONE);
            photoImageView.setVisibility(View.VISIBLE);

            //Load the image into the view
            Glide.with(context)
                    .load(text)
                    .into(photoImageView);
        } else {
            messageTextView.setVisibility(View.VISIBLE);
            photoImageView.setVisibility(View.GONE);
            messageTextView.setText(message.getText());
        }
     //   authorTextView.setText(message.getName());

        return convertView;
    }
}
