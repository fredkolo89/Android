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

import com.example.barcode.AsyncResponse;
import com.example.barcode.BarcodeItem;
import com.example.cityguideapp.R;
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
import com.microsoft.windowsazure.mobileservices.MobileServiceException;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import com.squareup.okhttp.OkHttpClient;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.Route;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener, AsyncResponse {

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

    private List<BarcodeItem> barcodeItems = new ArrayList<>();
    private ArrayList<String> listCheckPlaces = new ArrayList<>();

    private int SOME_REQUEST_CODE;

    private MobileServiceClient mClient;

    private MobileServiceTable<BarcodeItem> mToDoTable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnFindPath = (Button) findViewById(R.id.btnFindPath);
        etOrigin = (Button) findViewById(R.id.origin_address);
        etDestination = (Button) findViewById(R.id.destination_address);
        findAll = (CheckBox) findViewById(R.id.checkBox_find_all);
        checkFromAll = (Button)findViewById(R.id.checkPlacesFromAll);

        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {




                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {

                        try {
                            final List<BarcodeItem> results = refreshItemsFromMobileServiceTable();

                            //Offline Sync
                            //final List<ToDoItem> results = refreshItemsFromMobileServiceTableSyncTable();



                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    List <String> checkPlaces = new ArrayList<>();


                                    if(findAll.isChecked()){
                                        for(BarcodeItem res : results){
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

            initLocalStore().get();


            MapsActivity.trzask asyncTask = new MapsActivity.trzask();

            asyncTask.delegate = this;

            asyncTask.execute();

        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            createAndShowDialog(e, "Error");
        }


    }

    public void checkPlacesFromAll(View view) {
        Intent intent = new Intent(this, PlacesCheckChoose.class);
        startActivityForResult(intent, SOME_REQUEST_CODE);
    }


    public void chosePlaceOrigin(View view) {
        Intent intent = new Intent(this, PlacesChoose.class);
        startActivityForResult(intent, SOME_REQUEST_CODE);
    }


    public void chosePlaceDestination(View view) {
        Intent intent = new Intent(this, PlacesChoose.class);
        startActivityForResult(intent, SOME_REQUEST_CODE);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (etOrigin.getText().equals("Origin address") && data.getStringExtra("NAMA_PERASAT")!=null) {
            etOrigin.setText(data.getStringExtra("NAMA_PERASAT"));
            etDestination.setVisibility(View.VISIBLE);
        }
        else
            etDestination.setText(data.getStringExtra("NAMA_PERASAT"));

        if(data.getStringArrayListExtra("NAMA_List_PERASAT")!=null){
            listCheckPlaces = data.getStringArrayListExtra("NAMA_List_PERASAT");
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
                new DirectionFinder(this, origin, destination, allPoints).execute();

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
                    final List<BarcodeItem> results = refreshItemsFromMobileServiceTable();

                    //Offline Sync
                    //final List<ToDoItem> results = refreshItemsFromMobileServiceTableSyncTable();



                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMap = googleMap;
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.880618,20.637981), 10));
                            for (BarcodeItem item : results) {
                                originMarkers.add(mMap.addMarker(new MarkerOptions()
                                        .title(item.getName())
                                        .position(new LatLng(Double.parseDouble(item.getWidthtPosition()), Double.parseDouble(item.getLengthPosition())))));
                            }
                                if (ActivityCompat.checkSelfPermission(mClient.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mClient.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
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




    private List<BarcodeItem> refreshItemsFromMobileServiceTable() throws ExecutionException, InterruptedException, MobileServiceException {
        return mToDoTable.execute().get();
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

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

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
        polylinePaths.add(mMap.addPolyline(polylineOptions));
    }



    /**
     * Initialize local storage
     * @return
     * @throws MobileServiceLocalStoreException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private AsyncTask<Void, Void, Void> initLocalStore() throws MobileServiceLocalStoreException, ExecutionException, InterruptedException {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceSyncContext syncContext = mClient.getSyncContext();

                    if (syncContext.isInitialized())
                        return null;

                    SQLiteLocalStore localStore = new SQLiteLocalStore(mClient.getContext(), "OfflineStore", null, 1);

                    Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("deleted", ColumnDataType.Boolean);
                    tableDefinition.put("lengthPosition", ColumnDataType.String);
                    tableDefinition.put("widthtPosition", ColumnDataType.String);
                    tableDefinition.put("descriptionFirst", ColumnDataType.String);
                    tableDefinition.put("descriptionSecond", ColumnDataType.String);
                    tableDefinition.put("name", ColumnDataType.String);
                    tableDefinition.put("imageLinkFirst", ColumnDataType.String);
                    tableDefinition.put("imageLinkSecond", ColumnDataType.String);


                    localStore.defineTable("barcodeItem", tableDefinition);

                    SimpleSyncHandler handler = new SimpleSyncHandler();

                    syncContext.initialize(localStore, handler).get();

                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        return runAsyncTask(task);
    }

    /**
     * Creates a dialog and shows it
     *
     * @param exception
     *            The exception to show in the dialog
     * @param title
     *            The dialog title
     */
    private void createAndShowDialogFromTask(final Exception exception, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, "Error");
            }
        });
    }


    /**
     * Creates a dialog and shows it
     *
     * @param exception
     *            The exception to show in the dialog
     * @param title
     *            The dialog title
     */
    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if(exception.getCause() != null){
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    /**
     * Creates a dialog and shows it
     *
     * @param message
     *            The dialog message
     * @param title
     *            The dialog title
     */
    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    /**
     * Run an ASync task on the corresponding executor
     * @param task
     * @return
     */
    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }


    @Override
    public void processFinish(BarcodeItem output) {

        //etOrigin.setText(output.getName());

    }

    @Override
    public void processFinish(List<BarcodeItem> output) {
        // etOrigin = (Button) findViewById(R.id.origin_address);

        //etOrigin.setText(output.get(0).getName());
        //barcodeItems=output;

    }


    public class trzask extends AsyncTask<Void, Void,  List<BarcodeItem>> {

        public AsyncResponse delegate = null;
        List<BarcodeItem>  desig= new ArrayList<>();



        @Override
        protected  List<BarcodeItem> doInBackground(Void... params) {
            try {
                final MobileServiceList<BarcodeItem> result =
                        mToDoTable.execute().get();
                for (BarcodeItem item : result) {


                    desig.add(item);
                   // Log.v("FINALLY DESIGNATION IS", desig.getId());

                }

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return desig;
        }

        @Override
        protected void onPostExecute( List<BarcodeItem> los) {
            // super.onPostExecute(los);
            //textView.setText(los.get(0));
            delegate.processFinish(los);
        }
    }


}



