package com.example.menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.barcode.AsyncResponse;
import com.example.barcode.BarcodeActivity;
import com.example.barcode.BarcodeItem;
import com.example.cityguideapp.R;
import com.example.maps.MapsActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
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

import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MenuActivity extends Activity implements AsyncResponse {

    private Button scan_btn;

    private MobileServiceClient mClient;

    private MobileServiceTable<BarcodeItem> mToDoTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        scan_btn= (Button) findViewById(R.id.scan_btn);
        final Activity activity = this;
        scan_btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });



        
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Anulowales skanowane", Toast.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(this, BarcodeActivity.class);
                intent.putExtra("visitPlace", result.getContents());
                startActivity(intent);
                Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    public void OpenMaps(View view) {

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


            MenuActivity.trzask asyncTask = new MenuActivity.trzask();

            asyncTask.delegate = this;

            asyncTask.execute();

        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            createAndShowDialog(e, "Error");
        }


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

        Intent intent = new Intent(this, MapsActivity.class);
       // intent.putExtra("serializable_extra", (Serializable) output);
        startActivity(intent);



        // etOrigin = (Button) findViewById(R.id.origin_address);

        //etOrigin.setText(output.get(0).getName());
        //barcodeItems=output;

    }

    private List<BarcodeItem> refreshItemsFromMobileServiceTable() throws ExecutionException, InterruptedException, MobileServiceException {
        return mToDoTable.execute().get();
    }



    public class trzask extends AsyncTask<Void, Void,  List<BarcodeItem>>  {

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
           // delegate.processFinish(los);


            Intent intent_name = new Intent();

            Bundle bundle = new Bundle();
            bundle.putSerializable("value", (Serializable) los);
            intent_name.putExtras(bundle);


            intent_name.setClass(getApplicationContext(),MapsActivity.class);
            startActivity(intent_name);

        }
    }




}

