package com.example.maps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ListView;

import com.example.models.BarcodeItem;
import com.example.cityguideapp.R;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.util.ArrayList;
import java.util.List;

public class PlacesChoose extends Activity {



    private MobileServiceClient mClient;
    private MobileServiceTable<BarcodeItem> mToDoTable;
    private EditText mTextNewToDo;
    private MapsAdapter mAdapter;
    private ListView listPerasat;

    List<String> thumbs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_maps_places);



        Intent intent = this.getIntent();
        thumbs =  intent.getStringArrayListExtra("cos");




            mTextNewToDo = (EditText) findViewById(R.id.textNewToDo);



            // Create an adapter to bind the items with the view
            mAdapter = new MapsAdapter(this, R.layout.row_button_maps);
            ListView listMaps = (ListView) findViewById(R.id.listMapsPlaces);
            listMaps.setAdapter(mAdapter);

            mAdapter.addAll(thumbs);


         listPerasat = (ListView) findViewById(R.id.listMapsPlaces);
//
//        listPerasat.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//                String NamaPrst = ((Button) view.findViewById(R.id.selectPlace)).getText().toString();
//                Intent i = new Intent();
//                i.putExtra("NAMA_PERASAT", NamaPrst);
//                setResult(RESULT_OK, i);
//                finish();
//            }
//        });

    }


    public void addtocart(String place){

                Intent i = new Intent();
                i.putExtra("NAMA_PERASAT", place);
                setResult(RESULT_OK, i);
                finish();

    }





}
