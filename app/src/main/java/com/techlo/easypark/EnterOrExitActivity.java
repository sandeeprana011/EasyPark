package com.techlo.easypark;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class EnterOrExitActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private int VEHICLE_MOVEMENT;
    private int VEHICLE_TYPE = -1;
    private RadioGroup radioGroup;
    private TextView tTimeClock;
    private ClockShow showClock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_or_exit);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Button button = (Button) findViewById(R.id.b_vehicleenter_home);


        tTimeClock = (TextView) findViewById(R.id.timeClock);

        showClock = new ClockShow();
        showClock.execute();


        Intent intent = getIntent();
        VEHICLE_MOVEMENT = intent.getIntExtra(Fields.MOVEMENT, 0);
//        VEHICLE_TYPE = intent.getIntExtra(Fields.TYPE, 0);

        Log.e("movement,type", String.format("%d,%d", VEHICLE_MOVEMENT, VEHICLE_TYPE));
        if (VEHICLE_MOVEMENT == IntermediateActivity.MOV_ENTERY) {
            if (button != null) {
                button.setText("Enter");
            }
        } else {
            if (button != null) {
                button.setText("Exit");
            }
        }
        radioGroup = (RadioGroup) findViewById(R.id.rad_groupwheelertype_home);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_2wheeler) {
                    VEHICLE_TYPE = IntermediateActivity.TWO_WHEELERS;
                }
                if (checkedId == R.id.rb_4wheeler) {
                    VEHICLE_TYPE = IntermediateActivity.FOUR_WHEELERS;
                }
            }
        });


    }

    public void enterVehicle(View view) {
        view.setEnabled(false);
        if (VEHICLE_TYPE == (-1)) {
            Toast.makeText(this, "Please select vehicle type", Toast.LENGTH_LONG).show();
            return;
        }

        EditText eVehicleNumber = (EditText) findViewById(R.id.e_vehiclenumber_home);
        String vehicleNumber = null;
        if (eVehicleNumber != null) {
            vehicleNumber = eVehicleNumber.getText().toString();
        } else {
            Log.e("null", "vehicle number");
        }


        if (VEHICLE_MOVEMENT == IntermediateActivity.MOV_ENTERY) {
            EnterVehicle vehicle = new EnterVehicle();
            Date date = Calendar.getInstance().getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            String timeStamp = sdf.format(date);
            Log.e("timestamp", timeStamp);
//            try {

//            vehicle.execute();
            vehicle.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, vehicleNumber, timeStamp, String.valueOf(VEHICLE_TYPE));
//            }catch (Exception e){
//                Log.e("Excepiton","exception");
//            };
            Log.e("movement", "entry");
        } else {
            ExitVehicle exitVehicle = new ExitVehicle();
            Date date = Calendar.getInstance().getTime();
//            exitVehicle.execute(vehicleNumber, String.valueOf(date.getTime()), String.valueOf(VEHICLE_TYPE));
            exitVehicle.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, vehicleNumber, "", String.valueOf(VEHICLE_TYPE));
            Log.e("movement", "exit");
        }

    }

    public void cancelAction(View view) {
        finish();
    }


    class EnterVehicle extends AsyncTask<String, String, Boolean> {

        String number = null;
        String timestamp = null;
        String strEnterVehicle = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            Downloader downloader = new Downloader();
            number = params[0];
            timestamp = params[1];
            try {
                String url = Fields.URL_ENTERVEHICLE + "number=" + params[0] + "&type=0" + "&timestamp=" + params[1];
                if (url.contains(" ")) {
                    url = url.replace(" ", "%20");
                }
                strEnterVehicle = downloader.downloadContent(url);
                Log.e("entry", " network" + strEnterVehicle);
                JSONObject jsonObject = new JSONObject(strEnterVehicle);
                Log.e("string", strEnterVehicle);
//                Log.e("entry",strEnterVehicle);
                if (downloader.isOkJ(jsonObject, Fields.STATUS)) {
                    return true;
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                Log.e("exception", e.getMessage());
            }


            return false;
        }

        @Override
        protected void onPostExecute(Boolean s) {
            super.onPostExecute(s);
            if (s) {
                Toast.makeText(getApplicationContext(), "Vehicle entry succesffull", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(EnterOrExitActivity.this, InvoiceEntryVehicle.class);
                intent.putExtra(Fields.VEHICLE_NO, number);
                intent.putExtra(Fields.TIMESTAMP, timestamp);
                intent.putExtra(Fields.VEHICLE_TYPE, VEHICLE_TYPE);
                startActivity(intent);


            } else {
                Toast.makeText(getApplicationContext(), "Error! Vehicle entry failed!", Toast.LENGTH_LONG).show();
            }
        }
    }

    class ExitVehicle extends AsyncTask<String, String, String> {

        private Downloader downloader;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... params) {
            downloader = new Downloader();
            String strEnterVehicle = null;
            try {
                strEnterVehicle = downloader.downloadContent(Fields.URL_ENTERVEHICLE + "number=" + params[0] + "&timestamp=" + params[1] + "&type=1");
                Log.e("strExit", strEnterVehicle);

                return strEnterVehicle;

            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null) {


                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (downloader.isOkJ(jsonObject, Fields.NUMBER)) {
//                    if (downloader.isOkJ(jsonObject, Fields.AMOUNT)) {


                        String number = jsonObject.getString(Fields.NUMBER);
                        Log.e("onpost", number);

//                        String amount = jsonObject.getString(Fields.AMOUNT);
//                        String totalTime = jsonObject.getString(Fields.TOTAL_TIME);
                        String enterTime = jsonObject.getString(Fields.ENTERED_AT);
//                        String exitTime = jsonObject.getString(Fields.EXIT_AT);
                        String vehicleNO = jsonObject.getString(Fields.NUMBER);

                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);


                        Date exitDate = Calendar.getInstance().getTime();
//                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
//                        String timeStamp = sdf.format(date);

//                        Date exitDate = sdf.f(date);
                        Date enterDate = sdf.parse(enterTime);


                        long differ = exitDate.getTime() - enterDate.getTime();


                        int hours = (int) TimeUnit.MILLISECONDS.toHours(differ);

                        Log.e("hours", String.valueOf(hours));

                        String totalTime = sdf.format(differ);
//                        Date diffDate = new Date(differ);
//                        int hours = diffDate.getHours();
                        hours = hours + 1;
                        int amountInt;
                        if (VEHICLE_TYPE == IntermediateActivity.TWO_WHEELERS) {
                            amountInt = hours * 10;
                        } else {
                            amountInt = hours * 20;
                        }


                        Intent intent = new Intent(getApplicationContext(), ExitInvoice.class);
                        intent.putExtra(Fields.VEHICLE_NO, vehicleNO);
                        intent.putExtra(Fields.AMOUNT, String.valueOf(amountInt));
                        intent.putExtra(Fields.TOTAL_TIME, totalTime);
                        intent.putExtra(Fields.ENTERED_AT, enterTime);
                        intent.putExtra(Fields.EXIT_AT, sdf.format(exitDate));

                        startActivity(intent);

                    } else {
                        Log.e(Fields.NUMBER, "error");

                    }


                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                    Log.e("exception", "json");
                    Toast.makeText(getApplicationContext(), "Vehicle not available", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e("s is ", "null");
            }
        }
    }

    class ClockShow extends AsyncTask<Void, String, Void> {

        private boolean KEEP_RUNNING = true;

        @Override
        protected Void doInBackground(Void... params) {
            while (KEEP_RUNNING) {
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
            if (tTimeClock == null) return;
            tTimeClock.setText(values[0]);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        showClock.cancel(true);
    }
}





















