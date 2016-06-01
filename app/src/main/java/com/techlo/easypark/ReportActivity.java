package com.techlo.easypark;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.aem.api.AEMPrinter;
import com.aem.api.AEMScrybeDevice;
import com.aem.api.IAemScrybe;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

public class ReportActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 8397;
    private String number;
    private int VEHICLE_TYPE;
    String thatday;
    SharedPreferences preferences;

    TextView tLabReport, tTotalAmount, tCompText;
    GridView gridView;
    private AEMScrybeDevice device;

    int two_total = 0;
    int two_exited = 0;
    int two_pending;

    int four_total = 0;
    int four_exited = 0;
    int four_pending;
    //    int total_amount = 0;
    private String formattedDate;
    private BluetoothAdapter mBluetoothAdapter;
    private String total_amount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Intent intent = getIntent();
        number = intent.getStringExtra(Fields.NUMBER);
        VEHICLE_TYPE = intent.getIntExtra(Fields.VEHICLE_TYPE, 0);


        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        formattedDate = df.format(c.getTime());

        thatday = String.format("%s 00:00:00", formattedDate);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        tLabReport = (TextView) findViewById(R.id.lab_reports);
        tTotalAmount = (TextView) findViewById(R.id.t_totalamount_report);
        gridView = (GridView) findViewById(R.id.g_gridview_report);
        tCompText = (TextView) findViewById(R.id.t_table_report);

        DownReport report = new DownReport();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            report.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            report.execute();

        Log.e("url", "url incon");


    }

    public void printToBluetooth(View view) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                Log.e("open Bluetooth", "opening");
            } else {
                sendDataToDevice(view);
                Log.e("open Bluetooth", "sending");
            }
        }

    }

    private void sendDataToDevice(View view) {
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        mBluetoothAdapter.cancelDiscovery();
        if (devices.size() > 0) {
//            dialogShow(devices);
            Log.e("size", String.valueOf(devices.size()));
            printReport(view);

        } else {
            Log.e("log", "no device found");
        }
    }


    public void printReport(View view) {
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
                            two_total, two_exited,
                            two_pending,
                            four_total,
                            four_exited,
                            four_pending,
                            total_amount,
                            formattedDate
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
//
//
//            }
//        });
        try {
            device.startDiscover(this);
        } catch (Exception ex) {
            Log.e("Exception", "Exception");
        }

    }


    class DownReport extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Void... params) {
            Downloader downloader = new Downloader();
            String url = Fields.URL_REPORT +
                    "vehicle_type=" + String.valueOf(VEHICLE_TYPE) + "&number=" + number + "&timestamp=" + thatday + "&username="
                    + preferences.getString(Fields.USERNAME, "admin");
            url = url.replace(" ", "%20");
            Log.e("url", url);

            try {
                String string = downloader.downloadContent(url);
                Log.e("Downloaded", string);
                return string;
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("dowloaded", "error");
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add("TYPE");
                arrayList.add("IN");
                arrayList.add("OUT");
                arrayList.add("PENDING");


                arrayList.add("2 WH");


                JSONObject object = new JSONObject(s);
                if (isOk(object, Fields.VEH_TWO)) {
                    two_total = object.getInt(Fields.VEH_TWO);
                    arrayList.add(String.valueOf(two_total));
                }
                if (isOk(object, Fields.VEH_TWO_EXIT)) {
                    two_exited = object.getInt(Fields.VEH_TWO_EXIT);
                    arrayList.add(String.valueOf(two_exited));
//                    total_amount = two_exited * 10;
                }
                two_pending = two_total - two_exited;
                arrayList.add(String.valueOf(two_pending));

                arrayList.add("4 WH");

                if (isOk(object, Fields.VEH_FOUR)) {
                    four_total = object.getInt(Fields.VEH_FOUR);
                    arrayList.add(String.valueOf(four_total));
                }
                if (isOk(object, Fields.VEH_FOUR_EXIT)) {
                    four_exited = object.getInt(Fields.VEH_FOUR_EXIT);
                    arrayList.add(String.valueOf(four_exited));
//                    total_amount = total_amount + (four_exited * 20);
                }
                if (isOk(object, Fields.TOTAL_AMOUNT)) {
                    total_amount = String.valueOf(object.getInt(Fields.TOTAL_AMOUNT));
                } else {
                    total_amount = "N/A";
                }

                four_pending = four_total - four_exited;

                String line1 = String.format("%s %s %s %s", "TYPE", "IN", "OUT", "PENDING");
                String line2 = String.format("%s %d %d %d", "2 WH", two_total, two_exited, two_pending);
                String line3 = String.format("%s %d %d %d", "4 WH", four_total, four_exited, four_pending);
                String comText = String.format("%s \n%s \n%s ", line1, line2, line3);

                tCompText.setText(comText);
                tLabReport.setText("REPORT\n" + thatday);
                tTotalAmount.setText("Amount : " + String.valueOf(total_amount));


                arrayList.add(String.valueOf(four_pending));


                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_list_item_1, arrayList);
                gridView.setAdapter(arrayAdapter);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        boolean isOk(JSONObject object, String name) {
            if (object.has(name) && !object.isNull(name)) {
                return true;
            } else return false;
        }
    }

    public void onPrintBill(
            AEMPrinter m_AemPrinter,
            AEMScrybeDevice m_AemScrybeDevice,
            String printerName,

            int two_total,
            int two_exited,
            int two_pending,

            int four_total,
            int four_exited,
            int four_pending,
            String total_Amount,
            String todayDate

    ) {


        String headingTime = String.format("Report \n%s", todayDate);

        String line1 = String.format("%s %s %s %s", "TYPE", "IN", "OUT", "PENDING");
        String line2 = String.format("%s %d %d %d", "2 WH", two_total, two_exited, two_pending);
        String line3 = String.format("%s %d %d %d", "4 WH", four_total, four_exited, four_pending);

        String lineAmount = String.format("Amount : %s ", total_Amount);


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
            String data = "*" + headingTime + "*";
            m_AemPrinter.print(data);
            String d = "________________________________";
            m_AemPrinter.print(d);
            m_AemPrinter.setFontType(AEMPrinter.TEXT_ALIGNMENT_CENTER);
            data = line1 + ".";
            m_AemPrinter.print(data);
            data = line2 + ".";
            m_AemPrinter.print(data);
            data = line3 + ".";
            m_AemPrinter.print(data);
            m_AemPrinter.setFontType(AEMPrinter.DOUBLE_HEIGHT);
            m_AemPrinter.setFontType(AEMPrinter.TEXT_ALIGNMENT_CENTER);
            m_AemPrinter.print(data + ".");
            data = lineAmount + ".";
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
