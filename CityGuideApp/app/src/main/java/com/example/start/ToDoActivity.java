package com.example.start;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.example.cityguideapp.R;

public class ToDoActivity extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if (Build.VERSION.SDK_INT>=11){
            ActionBar bar = getActionBar();
            bar.hide();
        }

        Thread thread = new Thread(){
          public void run(){
              try {
                  sleep(3000);
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
              finally {
                  Intent intent = new Intent("ToDoActivity");
                  startActivity(intent);
              }
          }
        };
        thread.start();

    }

    @Override
    public void onPause(){
        super.onPause();
        finish();
    }
}