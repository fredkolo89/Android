package com.example.menu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.barcode.BarcodeActivity;
import com.example.cityguideapp.MainActivity;
import com.example.cityguideapp.R;
import com.example.maps.MapsActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MenuActivity extends Activity {

    private Button scan_btn;


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

    public void toDOOpen(View view) {
        Intent intent=new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    public void OpenBarcode(View view) {
        Intent intent = new Intent(this, BarcodeActivity.class);
        startActivity(intent);
    }

    public void OpenMaps(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
}

