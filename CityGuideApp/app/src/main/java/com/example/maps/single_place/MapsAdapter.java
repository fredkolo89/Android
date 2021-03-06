package com.example.maps.single_place;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.example.cityguideapp.R;

public class MapsAdapter extends ArrayAdapter<String> {

    Context mContext;

    int mLayoutResourceId;

    public MapsAdapter(Context context, int layoutResourceId) {
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

        final Button button = (Button) row.findViewById(R.id.selectPlace);

            row.setTag(currentItem);
            button.setText(currentItem);

            button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                        if (mContext instanceof PlacesChoose) {
                            PlacesChoose activity = (PlacesChoose) mContext;
                            activity.addtocart(currentItem);
                        }
                    }
             });

        return row;
    }

}