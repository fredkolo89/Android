package com.example.maps;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cityguideapp.R;
import com.example.helper.DirectionFinderListener;
import com.example.maps.drive_mode.DriveChoose;
import com.example.maps.multiple_place.PlacesCheckChoose;
import com.example.maps.single_place.PlacesChoose;
import com.example.models.BarcodeItem;
import com.example.models.Route;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.squareup.okhttp.OkHttpClient;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener, Serializable {

    private GoogleMap mMap;
    private Button btnFindPath;
    private Button etOrigin;
    private Button etDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private CheckBox findAll;
    private Button checkFromAll;
    private Button mode;

    private String driveMode = "driving";
    private String originText = "Origin address";
    private String destinationText ="Destination address";
    private String drive = "driving";
    private int viewSatelliteMaps=1;

    private ArrayList<String> listCheckPlaces = new ArrayList<>();

    private int REQUEST_CODE;
    private MobileServiceClient mClient;
    private MobileServiceTable<BarcodeItem> mToDoTable;

    List<BarcodeItem> thumbs= new ArrayList<>();
    List<String> namePlace= new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        thumbs = (List<BarcodeItem>)bundle.getSerializable("value");

        for(BarcodeItem ent : thumbs){
            namePlace.add(ent.getName());
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnFindPath = (Button) findViewById(R.id.btnFindPath);
        etOrigin = (Button) findViewById(R.id.start_address);
        etDestination = (Button) findViewById(R.id.end_address);
        findAll = (CheckBox) findViewById(R.id.checkBox_find_all);
        checkFromAll = (Button)findViewById(R.id.checkPlacesFromAll);
        mode = (Button)findViewById(R.id.driverMode);

        etOrigin.setText(originText);
        etDestination.setText(destinationText);
        mode.setText(driveMode);

        btnFindPath.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {

                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    List <String> checkPlaces = new ArrayList<>();

                                    if(findAll.isChecked()){
                                        for(BarcodeItem res : thumbs){
                                            if(!res.getName().equals(etOrigin.getText()) && !res.getName().equals(etDestination.getText())){
                                                checkPlaces.add(res.getName());
                                            }
                                        }
                                        sendRequest(checkPlaces);
                                    }
                                    else if(listCheckPlaces.size()>0)  {
                                        for(String res : listCheckPlaces){
                                            if(!res.equals(etOrigin.getText()) && !res.equals(etDestination.getText())){
                                                checkPlaces.add(res);
                                            }
                                        }
                                        listCheckPlaces.clear();
                                        sendRequest(checkPlaces);
                                    }
                                    else{
                                        sendRequest(null);
                                    }
                                }
                            });
                        } catch (final Exception e){
                            createAndShowDialogFromTask(e, "Error");
                        }

                        return null;
                    }
                };

                runAsyncTask(task);
            }
        });

        try {
            mClient = new MobileServiceClient(
                    "https://cityguideapp.azurewebsites.net",
                    this);
            mClient.setAndroidHttpClientFactory(new OkHttpClientFactory() {
                @Override
                public OkHttpClient createOkHttpClient() {
                    OkHttpClient client = new OkHttpClient();
                    client.setReadTimeout(20, TimeUnit.SECONDS);
                    client.setWriteTimeout(20, TimeUnit.SECONDS);
                    return client;
                }
            });

            mToDoTable = mClient.getTable(BarcodeItem.class);

        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            createAndShowDialog(e, "Error");
        }

    }


    public void checkPlacesFromAll(View view) {
        Intent intent = new Intent(this, PlacesCheckChoose.class);
        intent.putStringArrayListExtra("value", (ArrayList<String>) namePlace);
        startActivityForResult(intent, REQUEST_CODE);
    }


    public void chosePlaceOrigin(View view) {
        Intent intent = new Intent(this, PlacesChoose.class);
        intent.putStringArrayListExtra("value", (ArrayList<String>) namePlace);
        startActivityForResult(intent, REQUEST_CODE);
    }


    public void chosePlaceDestination(View view) {
        Intent intent = new Intent(this, PlacesChoose.class);
        intent.putStringArrayListExtra("value", (ArrayList<String>) namePlace);
        startActivityForResult(intent, REQUEST_CODE);
    }

    public void chooseDriverMode(View view) {
        Intent intent = new Intent(this, DriveChoose.class);
        intent.putStringArrayListExtra("value", (ArrayList<String>) namePlace);
        startActivityForResult(intent, REQUEST_CODE);
    }

    public void chooseViewMode(View view) {
        if (viewSatelliteMaps==1)
            viewSatelliteMaps=2;
        else
            viewSatelliteMaps=1;
    }


    public void itemClicked(View v) {
        if(findAll.isChecked()){
                checkFromAll.setVisibility(View.INVISIBLE);
        }
        else{
            checkFromAll.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (etOrigin.getText().equals("Origin address") && data.getStringExtra("PLACE_NAME")!=null) {
            originText=data.getStringExtra("PLACE_NAME");
            etOrigin.setText(originText);
        }
        else if(data.getStringExtra("PLACE_NAME")!=null){
            destinationText = data.getStringExtra("PLACE_NAME");
            etDestination.setText(destinationText);
        }
        if(data.getStringArrayListExtra("PLACE_NAME_LIST")!=null){
            listCheckPlaces = data.getStringArrayListExtra("PLACE_NAME_LIST");
        }

        if(data.getStringExtra("DRIVE_WAY")!=null){
            drive = data.getStringExtra("DRIVE_WAY");
            mode.setText(drive);
        }
    }


    private void sendRequest(List <String> allPoints) {
        String origin = etOrigin.getText().toString();
        String destination = etDestination.getText().toString();
        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
                new DirectionFinder(this, origin, destination, allPoints, mode.getText().toString()).execute();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                try {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mMap = googleMap;
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.880618,20.637981), 10));
                            for (BarcodeItem item : thumbs) {
                                originMarkers.add(mMap.addMarker(new MarkerOptions()
                                        .title(item.getName())
                                        .position(new LatLng(Double.parseDouble(item.getWidthtPosition()), Double.parseDouble(item.getLengthPosition())))));
                            }
                            if (ActivityCompat.checkSelfPermission(mClient.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mClient.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                return;
                            }
                            mMap.setMyLocationEnabled(true);
                        }
                    });
                } catch (final Exception e){
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        runAsyncTask(task);
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }
        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }
        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        PolylineOptions polylineOptions = new PolylineOptions();
        float dist = 0;
        String distS = new String();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            distS = route.distance.text.replaceAll(",", ".");
            distS = distS.replaceAll(" ", "");
            distS = distS.replaceAll("km", "");
            dist = dist + Float.parseFloat(distS);

            ((TextView) findViewById(R.id.tvDistance)).setText(Float.toString(dist));

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title(route.endAddress)
                    .position(route.endLocation)));

            polylineOptions.
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

        }

        mMap.setMapType(viewSatelliteMaps);
        polylinePaths.add(mMap.addPolyline(polylineOptions));
    }


    private void createAndShowDialogFromTask(final Exception exception, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, "Error");
            }
        });
    }


    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if(exception.getCause() != null){
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }


    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }


    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }

}



