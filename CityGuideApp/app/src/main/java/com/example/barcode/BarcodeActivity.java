package com.example.barcode;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cityguideapp.R;
import com.example.helper.AsyncResponse;
import com.example.models.BarcodeItem;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class BarcodeActivity extends Activity implements AsyncResponse {

    private MobileServiceClient mClient;
    
    private MobileServiceTable<BarcodeItem> mToDoTable;

    private TextView textViewTitle;
    private ImageView imageFirst;
    private ImageView imageSecond;
    private TextView textOne;
    private TextView textSecond;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);

        textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        imageFirst = (ImageView) findViewById(R.id.imageFirst);
        imageSecond = (ImageView) findViewById(R.id.imageSecond);
        textOne = (TextView) findViewById(R.id.textFirst);
        textSecond = (TextView) findViewById(R.id.textSecond);

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

            Bundle b = getIntent().getExtras();
            String name = b.getString("visitPlace");

            TakeItem asyncTask =new TakeItem(name);
            asyncTask.delegate = this;

            asyncTask.execute();


        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e){
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

    @Override
    public void processFinish(BarcodeItem output) {
        ///
        textViewTitle.setText(output.getName());

        Picasso.with(this).load(output.getImageLinkFirst()).into(imageFirst);
        Picasso.with(this).load(output.getImageLinkSecond()).into(imageSecond);

        textOne.setText(output.getDescriptionFirst());
        textSecond.setText(output.getDescriptionSecond());
    }



    public class TakeItem extends AsyncTask<Void, Void, BarcodeItem> {

        public AsyncResponse delegate = null;
        BarcodeItem desig = null;

        private String name;

        public TakeItem(String name) {
            this.name = name;
        }

        public String getName() {
                return name;
            }
        public void setName(String name) {
                this.name = name;
            }



        @Override
        protected BarcodeItem doInBackground(Void... params) {
            try {
                final MobileServiceList<BarcodeItem> result =
                        mToDoTable.where().field("name").eq(name).execute().get();
                for (BarcodeItem item : result) {

                       desig = item;
                }

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return desig;
        }

        @Override
        protected void onPostExecute( BarcodeItem los) {
        ///
            delegate.processFinish(los);
        }
    }

}