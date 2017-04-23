package com.example.maps;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import com.example.barcode.BarcodeItem;
import com.example.cityguideapp.R;

/**
 * Adapter to bind a ToDoItem List to a view
 */
public class MapsCheckAdapter extends ArrayAdapter<BarcodeItem> {

    /**
     * Adapter context
     */
    Context mContext;

    /**
     * Adapter View layout
     */
    int mLayoutResourceId;

    public MapsCheckAdapter(Context context, int layoutResourceId) {
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

        final BarcodeItem currentItem = getItem(position);

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(mLayoutResourceId, parent, false);
        }


        final CheckBox check = (CheckBox) row.findViewById(R.id.checkToDoItem);

            row.setTag(currentItem);

            check.setText(currentItem.getName());

                     check.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        if (check.isChecked()) {
                            if (mContext instanceof PlacesCheckChoose) {
                                PlacesCheckChoose activity = (PlacesCheckChoose) mContext;
                                activity.addToList(currentItem.getName());
                            }
                        }
                    }
                });

        return row;
    }


}