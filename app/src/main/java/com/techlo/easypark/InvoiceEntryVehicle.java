package com.techlo.easypark;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
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
import java.util.UUID;

public class InvoiceEntryVehicle extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    String parkingSite;
    String number;
    String timestamp;
    TextView tParkingsite, tVehicleNo, tRate, tTimeStamp;
    private SharedPreferences preferences;
    private int vehicleType;
    private TextView tTimeClock;
    private ClockShow showClock;
    private BluetoothAdapter mBluetoothAdapter;
    private AEMScrybeDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_entry_vehicle);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        tParkingsite = (TextView) findViewById(R.id.t_parkingsite_invoiceentry);
        tVehicleNo = (TextView) findViewById(R.id.t_vehicleno_invoiceentry);
        tRate = (TextView) findViewById(R.id.t_rate_invoiceentry);
        tTimeStamp = (TextView) findViewById(R.id.t_enteredat_invoieentry);

        tTimeClock = (TextView) findViewById(R.id.timeClock);

        showClock = new ClockShow();
        showClock.execute();


        Intent intent = getIntent();


        number = intent.getStringExtra(Fields.VEHICLE_NO);
        timestamp = intent.getStringExtra(Fields.TIMESTAMP);
        parkingSite = preferences.getString(Fields.HEADER, parkingSite);
        vehicleType = intent.getIntExtra(Fields.VEHICLE_TYPE, 0);

        tParkingsite.setText(parkingSite);
        tVehicleNo.setText("VEH. NO. : " + number);
        if (vehicleType == IntermediateActivity.TWO_WHEELERS) {
            tRate.setText("₹ 10/hour");
        } else {
            tRate.setText("₹ 20/hour");
        }
        tTimeStamp.setText(timestamp);
        tTimeClock = (TextView) findViewById(R.id.timeClock);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            sendDataToDevice();
        } else {
            Toast.makeText(this, "Please enable Bluetooth first!", Toast.LENGTH_LONG).show();
        }
    }

    private void sendDataToDevice() {
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        mBluetoothAdapter.cancelDiscovery();
        if (devices.size() > 0) {
//            dialogShow(devices);


            dialogAemSearchAndPrint(
                    "Parking Site",
                    parkingSite,
                    "T&C",
                    "",
                    "admin",
                    timestamp,
                    tRate.getText().toString(),
                    String.valueOf("Amount : "+(vehicleType+1)*10),
                    ""
                    );
        } else {
            Log.wtf("log", "no device found");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        showClock.cancel(true);
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

    public void dialogAemSearchAndPrint(final String header1,
                                        final String header2,
                                        final String footer1,
                                        final String footer2,
                                        final String operator,
                                        final String enteredAt,
                                        final String exitAt,
                                        final String totalAmount,
                                        final String totalTimeSpend
    ) {
        device = new AEMScrybeDevice(new IAemScrybe() {
            @Override
            public void onDiscoveryComplete(final ArrayList<String> arrayList) {
                Log.e("Printers", "list");
                final AlertDialog.Builder builderSingle = new AlertDialog.Builder(InvoiceEntryVehicle.this);
//        builderSingle.setIcon(R.drawable.ic_launcher);
                builderSingle.setTitle("Select Printer:-");

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                        InvoiceEntryVehicle.this,
                        android.R.layout.select_dialog_singlechoice);

                for (String name : arrayList) {
                    arrayAdapter.add(name);
                }
                builderSingle.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String printerName = arrayList.get(which);
                        String string=device.pairPrinter(printerName);
                        Log.e("printer",string);
                        try {
                            device.connectToPrinter(string);
                            AEMPrinter printer=device.getAemPrinter();

                            printer.setFontType((byte) 8);
                            printer.print(header1);
                            printer.setFontType((byte) 3);
                            printer.print(header2);
                            printer.setFontType((byte) 6);
                            printer.print(totalAmount);
                            printer.print(operator);
                            printer.print(enteredAt);
                            printer.print(exitAt);
                            printer.print(totalTimeSpend);
                            printer.print(footer1);
                            printer.print(footer2);

                            device.disConnectPrinter();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                });


            }
        });

        device.startDiscover(this);


    }

    public void pairDeviceWith() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    // Loop through paired devices
                    final AlertDialog.Builder builderSingle = new AlertDialog.Builder(InvoiceEntryVehicle.this);
//        builderSingle.setIcon(R.drawable.ic_launcher);
                    builderSingle.setTitle("Select Printer:-");

                    final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                            InvoiceEntryVehicle.this,
                            android.R.layout.select_dialog_singlechoice);

                    for (BluetoothDevice device : pairedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        arrayAdapter.add(device.getName() + "\n" + device.getAddress());

                    }
                }
            }
        } else {
            // Device does not support Bluetooth

        }
    }

    private class AcceptThread extends Thread {
        private static final String NAME = "v_park";
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String uuid = tManager.getDeviceId();
                UUID uuid1 = UUID.fromString(uuid);
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, uuid1);


            } catch (IOException e) {
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
//                    manageConnectedSocket(socket);
//                    mmServerSocket.close();
                    break;
                }
            }
        }

        /**
         * Will cancel the listening socket, and cause the thread to finish
         */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }
    }


}


























