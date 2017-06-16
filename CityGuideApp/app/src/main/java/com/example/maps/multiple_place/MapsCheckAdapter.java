package com.example.maps.multiple_place;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import com.example.cityguideapp.R;

public class MapsCheckAdapter extends ArrayAdapter<String> {


    Context mContext;
    int mLayoutResourceId;

    public MapsCheckAdapter(Context context, int layoutResourceId) {
        super(context, layoutResourceId);

        mContext = context;
        mLayoutResourceId = layoutResourceId;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        final String currentItem = getItem(position);

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(mLayoutResourceId, parent, false);
        }

        final CheckBox check = (CheckBox) row.findViewById(R.id.checkToDoItem);

            row.setTag(currentItem);
            check.setText(currentItem);

            check.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        if (check.isChecked()) {
                            if (mContext instanceof PlacesCheckChoose) {
                                PlacesCheckChoose activity = (PlacesCheckChoose) mContext;
                                activity.addToList(currentItem);
                            }
                        }
                    }
                });

        return row;
    }

}