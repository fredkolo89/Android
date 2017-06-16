package com.example.maps.multiple_place;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.example.cityguideapp.R;

import java.util.ArrayList;
import java.util.List;

public class PlacesCheckChoose extends Activity {

    private MapsCheckAdapter mAdapter;
    private ArrayList<String> checkPlaces = new ArrayList<>();

    List<String> thumbs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_check_maps_places);

        Intent intent = this.getIntent();
        thumbs =  intent.getStringArrayListExtra("value");

        mAdapter = new MapsCheckAdapter(this, R.layout.row_list_to_do);
        ListView listMaps = (ListView) findViewById(R.id.listCheckMapsPlaces);
        listMaps.setAdapter(mAdapter);

        mAdapter.addAll(thumbs);

    }

    public void acceptCheckChoose(View view) {
        Intent i = new Intent();
        i.putStringArrayListExtra("PLACE_NAME_LIST", checkPlaces);
        setResult(RESULT_OK, i);
        finish();

    }

    public void addToList(String place){

        checkPlaces.add(place);
    }

}
