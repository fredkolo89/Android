package com.example.maps.single_place;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.example.cityguideapp.R;

import java.util.ArrayList;
import java.util.List;

public class PlacesChoose extends Activity {

    private MapsAdapter mAdapter;
    List<String> thumbs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_maps_places);

        Intent intent = this.getIntent();
        thumbs =  intent.getStringArrayListExtra("value");

        mAdapter = new MapsAdapter(this, R.layout.row_button_maps);
        ListView listMaps = (ListView) findViewById(R.id.listMapsPlaces);
        listMaps.setAdapter(mAdapter);
        mAdapter.addAll(thumbs);
    }


    public void addtocart(String place){

                Intent i = new Intent();
                i.putExtra("PLACE_NAME", place);
                setResult(RESULT_OK, i);
                finish();
    }

}
