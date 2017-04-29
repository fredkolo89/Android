package com.example.maps.drive_mode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.example.cityguideapp.R;

import java.util.ArrayList;
import java.util.Arrays;


public class DriveChoose extends Activity {


    private DriveAdapter mAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drive_places);


        String driveWay[] = {"driving", "walking", "bicycling"};

        ArrayList<String> driveList = new ArrayList<String>();
        driveList.addAll( Arrays.asList(driveWay) );

        mAdapter = new DriveAdapter(this, R.layout.row_button_maps);
        ListView listMaps = (ListView) findViewById(R.id.listDrivePlaces);
        mAdapter.addAll(driveList);
        listMaps.setAdapter(mAdapter);



    }
    public void addtocart(String place){

        Intent i = new Intent();
        i.putExtra("driveWay", place);
        setResult(RESULT_OK, i);
        finish();

    }



}
