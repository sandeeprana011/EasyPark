package com.techlo.easypark;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.lang.annotation.ElementType;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;

public class IntermediateActivity extends AppCompatActivity {

    static int MOVEMENT;
    public static final int MOV_ENTERY = 0;
    public static final int MOV_EXIT = 1;
//    static int TYPE;

    public static final int TWO_WHEELERS = 0;
    public static final int FOUR_WHEELERS = 1;
    private TextView tTimeClock;
    ClockShow showClock;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intermediate);


        tTimeClock= (TextView) findViewById(R.id.timeClock);

        showClock=new ClockShow();
        showClock.execute();


    }

    public void logOut(View view) {
//        view.setEnabled(false);
//        Intent intent = new Intent(this, EnterOrExitActivity.class);
//        intent.putExtra(Fields.MOVEMENT, MOVEMENT);
////        intent.putExtra(Fields.TYPE, TYPE);
//        Log.e("MOVEMENT", String.valueOf(MOVEMENT));
//        startActivity(intent);
        System.exit(0);

    }


    public void parkIn(View view) {
        Intent intent = new Intent(this, EnterOrExitActivity.class);
        intent.putExtra(Fields.MOVEMENT, MOV_ENTERY);
//        intent.putExtra(Fields.TYPE, TYPE);
        Log.e("MOVEMENT", String.valueOf(MOVEMENT));
        startActivity(intent);

    }

    public void parkOut(View view) {
        Intent intent = new Intent(this, EnterOrExitActivity.class);
        intent.putExtra(Fields.MOVEMENT, MOV_EXIT);
//        intent.putExtra(Fields.TYPE, TYPE);
        Log.e("MOVEMENT", String.valueOf(MOVEMENT));
        startActivity(intent);
    }

    class ClockShow extends AsyncTask<Void,String,Void>{

        private boolean KEEP_RUNNING = true;

        @Override
        protected Void doInBackground(Void... params) {
            while (KEEP_RUNNING){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Date date = Calendar.getInstance().getTime();
                final String timeStamp = sdf.format(date);
                publishProgress(timeStamp);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (tTimeClock==null) return;
            tTimeClock.setText(values[0]);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        showClock.cancel(true);
    }
}
