package com.example.maps.drive_mode;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.example.cityguideapp.R;

/**
 * Adapter to bind a ToDoItem List to a view
 */
public class DriveAdapter extends ArrayAdapter<String> {

    /**
     * Adapter context
     */
    Context mContext;

    /**
     * Adapter View layout
     */
    int mLayoutResourceId;

    public DriveAdapter(Context context, int layoutResourceId) {
        super(context, layoutResourceId);

        mContext = context;
        mLayoutResourceId = layoutResourceId;
    }

    /**
     * Returns the view for a specific item on the list
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        final String currentItem = getItem(position);

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(mLayoutResourceId, parent, false);
        }


        final Button button = (Button) row.findViewById(R.id.selectPlace);

            row.setTag(currentItem);

            button.setText(currentItem.toString());



            button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                    if (mContext instanceof DriveChoose) {
                        DriveChoose activity = (DriveChoose) mContext;
                        activity.addtocart(currentItem);
                    }
                }

        });

        return row;
    }

}