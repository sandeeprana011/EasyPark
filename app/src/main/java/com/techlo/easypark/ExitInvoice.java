package com.techlo.easypark;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aem.api.AEMPrinter;
import com.aem.api.AEMScrybeDevice;
import com.aem.api.IAemScrybe;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

public class ExitInvoice extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 987;
    private String amount;
    private String totalTime;
    private String enteredAt;
    private String exitAt;
    private String vehicleNo;
    private AEMScrybeDevice device;
    private BluetoothAdapter mBluetoothAdapter;
    private int vehicleType;
    private SharedPreferences preferences;
    private String parkingSite;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exit_invoice);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        TextView tamount = (TextView) findViewById(R.id.t_amountpayable_exitinvoie);
        TextView tTotalTime = (TextView) findViewById(R.id.t_totaltime_exitinvoie);
        TextView tEnteredAt = (TextView) findViewById(R.id.t_enteredat_exitinvoie);
        TextView tExitAt = (TextView) findViewById(R.id.t_exitat_exitinvoie);
        TextView tVehiclNo = (TextView) findViewById(R.id.t_vehicleno);

        Intent intent = getIntent();

        amount = intent.getStringExtra(Fields.AMOUNT) + ".";
        totalTime = intent.getStringExtra(Fields.TOTAL_TIME);
        enteredAt = intent.getStringExtra(Fields.ENTERED_AT);
        exitAt = intent.getStringExtra(Fields.EXIT_AT);
        vehicleNo = intent.getStringExtra(Fields.VEHICLE_NO);
        vehicleType = intent.getIntExtra(Fields.VEHICLE_TYPE, 0);


//        parkingSite = preferences.getString(Fields.HEADER, parkingSite);
        parkingSite = "01-SDMC LAJPAT NAGAR,HALDIRAM";
        username = preferences.getString(Fields.USERNAME, " ");


        tamount.setText(amount);
        tTotalTime.setText(totalTime);
        tEnteredAt.setText(enteredAt);
        tExitAt.setText(exitAt);
        tVehiclNo.setText(vehicleNo);


    }

    public void printinvoice(View view) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                sendDataToDevice();
            }
        }
    }


    private void sendDataToDevice() {
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        mBluetoothAdapter.cancelDiscovery();
        if (devices.size() > 0) {
//            dialogShow(devices);


            String vehTypeName, rate;
            if (vehicleType == IntermediateActivity.TWO_WHEELERS) {
                vehTypeName = "2 WHEELER";
                rate = "₹ 10/hour";
            } else {
                vehTypeName = "4 WHEELER";
                rate = "₹ 20/hour";

            }


            dialogAemSearchAndPrint(
                    "Parking Site",
                    parkingSite,
                    "Heading 1.",
                    "Heading 2.",
                    vehicleNo + ".",
                    username + ".",
                    enteredAt + ".",
                    exitAt + ".",
                    totalTime + ".",
                    amount + ".",
                    rate + ".",
                    vehTypeName + "."
            );
        } else {
            Log.wtf("log", "no device found");
        }
    }

    public void dialogAemSearchAndPrint(final String header1,
                                        final String header2,
                                        final String footer1,
                                        final String footer2,
                                        final String vehicleNo,
                                        final String operator,
                                        final String enteredAt,
                                        final String exitAt,
                                        final String totalTime,
                                        final String amount,
                                        final String rate,
                                        final String vehTypeStringText
    ) {
        device = new AEMScrybeDevice(new IAemScrybe() {
            @Override
            public void onDiscoveryComplete(final ArrayList<String> arrayList) {
                Log.e("Printers", "list");
//                final AlertDialog.Builder builderSingle = new AlertDialog.Builder(ExitInvoice.this);
//        builderSingle.setIcon(R.drawable.ic_launcher);
//                builderSingle.setTitle("Select Printer:-");

//                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
//                        ExitInvoice.this,
//                        android.R.layout.select_dialog_singlechoice);

                String printerName = "";

                for (String name : arrayList) {
//                    arrayAdapter.add(name);
                    if (name.contains("inter")) {
                        printerName = name;
                    }
                }
//                builderSingle.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        String printerName = arrayList.get(which);
//                        String string = device.pairPrinter(printerName);
                Log.e("printer", printerName);
                try {
                    device.connectToPrinter(printerName);
                    final AEMPrinter printer = device.getAemPrinter();


                    onPrintBill(
                            printer,
                            device,
                            printerName,
                            vehTypeStringText,
                            vehicleNo,
                            enteredAt,
                            exitAt,
                            totalTime,
                            amount,
                            rate,
                            header1,
                            header2,
                            operator
                    );

                    boolean a = device.disConnectPrinter();


                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("error", "io bluetooth printing");
                    Toast.makeText(getApplicationContext(), "Error While printing bluetoth printing", Toast.LENGTH_LONG).show();
                }


            }
        });
//                builderSingle.show();


//            }
//        });

        try {
            device.startDiscover(this);
        } catch (Exception ex) {
            Log.e("Exception", "Exception");
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
//            if (tTimeClock == null) return;
//            tTimeClock.setText(values[0]);
        }

    }


    public void onPrintBill(
            AEMPrinter m_AemPrinter,
            AEMScrybeDevice m_AemScrybeDevice,
            String printerName,
            String veh_cat,
            String veh_numbr,
            String enteredAt,
            String exitAt,
            String totalTime,
            String amount,
            String rate,
            String textHeader1,
            String textHeader2,
            String fieldOperator) {
        try {
            m_AemScrybeDevice.connectToPrinter(printerName);
            m_AemPrinter = m_AemScrybeDevice.getAemPrinter();
            showAlert("Connected with " + printerName);
        } catch (IOException e) {
            if (e.getMessage().contains("Service discovery failed")) {
                showAlert("Not Connected\n"
                        + printerName
                        + " is unreachable or off otherwise it is connected with other device");
            } else if (e.getMessage().contains("Device or resource busy")) {
                showAlert("the device is already connected");
            } else {
                showAlert("Unable to connect");
            }
        }
        if (m_AemPrinter == null) {
            showAlert("Printer not connected");
            return;
        }

        try {
            m_AemPrinter.setFontType(AEMPrinter.DOUBLE_HEIGHT);
            m_AemPrinter.setFontType(AEMPrinter.TEXT_ALIGNMENT_CENTER);
            m_AemPrinter.setFontType(AEMPrinter.DOUBLE_HEIGHT);
            m_AemPrinter.setFontType(AEMPrinter.TEXT_ALIGNMENT_CENTER);
            String data = "PARKING - OUT SLIP";
            m_AemPrinter.print(data);
            String d = "________________________________";
            m_AemPrinter.print(d);
            m_AemPrinter.setFontType(AEMPrinter.TEXT_ALIGNMENT_CENTER);
            data = textHeader1 + ".";
            m_AemPrinter.print(data);
            data = textHeader2 + ".";
            m_AemPrinter.print(data);
            data = "FSO-01 - " + fieldOperator;
            m_AemPrinter.print(data);
            d = "________________________________";
            m_AemPrinter.print(d);
            data = "VEHICLE TYPE: " + veh_cat;
            m_AemPrinter.print(data);
            data = "Rate : " + rate;
            m_AemPrinter.print(data);
            data = "Amount : " + amount;
            m_AemPrinter.print(data);
            data = "Veh. No :" + veh_numbr;
            m_AemPrinter.setFontType(AEMPrinter.DOUBLE_HEIGHT);
            m_AemPrinter.setFontType(AEMPrinter.TEXT_ALIGNMENT_CENTER);
            m_AemPrinter.print(data);
            data = "Time IN: " + enteredAt;
            m_AemPrinter.print(data);
            data = "Time OUT: " + exitAt;
            m_AemPrinter.print(data);
            m_AemPrinter.print(d);
            data = "** Parking at owners risk no responsibility for valuable items like Laptop, Wallet, Helmet etc." +
                    "If token is lost Rs 50 will be charged after verification\nPowered by: V PARK";

            m_AemPrinter.print(data);
            m_AemPrinter.setFontType(AEMPrinter.DOUBLE_HEIGHT);
            m_AemPrinter.setFontType(AEMPrinter.TEXT_ALIGNMENT_CENTER);
            m_AemPrinter.setCarriageReturn();

        }
        // catch (IOException e)
        catch (Exception e) {
            if (e.getMessage().contains("socket closed"))
                showAlert("Printer not connected");
        }
    }

    private void showAlert(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }


}
