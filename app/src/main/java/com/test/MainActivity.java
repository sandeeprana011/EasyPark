//package com.test;
//
//import android.app.Activity;
//import android.app.AlertDialog.Builder;
//import android.app.ProgressDialog;
//import android.content.DialogInterface;
//import android.content.DialogInterface.OnClickListener;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.ContextMenu;
//import android.view.ContextMenu.ContextMenuInfo;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.Toast;
//import com.aem.api.AEMPrinter;
//import com.aem.api.AEMPrinter.BARCODE_HEIGHT;
//import com.aem.api.AEMPrinter.BARCODE_TYPE;
//import com.aem.api.AEMScrybeDevice;
//import com.aem.api.CardReader;
//import com.aem.api.CardReader.CARD_TRACK;
//import com.aem.api.CardReader.DLCardData;
//import com.aem.api.CardReader.MSRCardData;
//import com.aem.api.CardReader.RCCardData;
//import com.aem.api.IAemCardScanner;
//import com.aem.api.IAemScrybe;
//import com.example.tempproject.C0029R;
//import com.google.zxing.WriterException;
//import java.io.IOException;
//import java.util.ArrayList;
//import org.apache.commons.io.IOUtils;
//
//public class MainActivity extends Activity implements IAemCardScanner, IAemScrybe {
//    CARD_TRACK cardTrackType;
//    String creditData;
//    MSRCardData creditDetails;
//    EditText editText;
//    AEMPrinter m_AemPrinter;
//    AEMScrybeDevice m_AemScrybeDevice;
//    ProgressDialog m_WaitDialogue;
//    CardReader m_cardReader;
//    ArrayList<String> printerList;
//    EditText rfdEditText;
//
//    /* renamed from: com.example.tempproject.MainActivity.1 */
//    class C00241 implements Runnable {
//        private final /* synthetic */ String val$buffer;
//
//        C00241(String str) {
//            this.val$buffer = str;
//        }
//
//        public void run() {
//            MainActivity.this.editText.setText(this.val$buffer.toString());
//        }
//    }
//
//    /* renamed from: com.example.tempproject.MainActivity.2 */
//    class C00252 implements Runnable {
//        private final /* synthetic */ String val$data;
//
//        C00252(String str) {
//            this.val$data = str;
//        }
//
//        public void run() {
//            MainActivity.this.editText.setText(this.val$data);
//        }
//    }
//
//    /* renamed from: com.example.tempproject.MainActivity.3 */
//    class C00263 implements Runnable {
//        private final /* synthetic */ String val$data;
//
//        C00263(String str) {
//            this.val$data = str;
//        }
//
//        public void run() {
//            MainActivity.this.editText.setText(this.val$data);
//        }
//    }
//
//    /* renamed from: com.example.tempproject.MainActivity.4 */
//    class C00274 implements Runnable {
//        private final /* synthetic */ String val$data;
//
//        C00274(String str) {
//            this.val$data = str;
//        }
//
//        public void run() {
//            MainActivity.this.rfdEditText.setText("RF ID:   " + this.val$data);
//            MainActivity.this.editText.setText("ID " + this.val$data);
//        }
//    }
//
//    /* renamed from: com.example.tempproject.MainActivity.5 */
//    class C00285 implements OnClickListener {
//        C00285() {
//        }
//
//        public void onClick(DialogInterface dialog, int item) {
//        }
//    }
//
//    public MainActivity() {
//        this.m_cardReader = null;
//        this.m_AemPrinter = null;
//    }
//
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(C0029R.layout.activity_main);
//        this.printerList = new ArrayList();
//        this.creditData = new String();
//        this.editText = (EditText) findViewById(C0029R.id.edittext);
//        this.rfdEditText = (EditText) findViewById(C0029R.id.RFid);
//        this.m_AemScrybeDevice = new AEMScrybeDevice(this);
//        registerForContextMenu((Button) findViewById(C0029R.id.pairing));
//    }
//
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        menu.setHeaderTitle("Select Printer to connect");
//        for (int i = 0; i < this.printerList.size(); i++) {
//            menu.add(0, v.getId(), 0, (CharSequence) this.printerList.get(i));
//        }
//    }
//
//    public boolean onContextItemSelected(MenuItem item) {
//        super.onContextItemSelected(item);
//        String printerName = item.getTitle().toString();
//        try {
//            this.m_AemScrybeDevice.connectToPrinter(printerName);
//            this.m_cardReader = this.m_AemScrybeDevice.getCardReader(this);
//            this.m_AemPrinter = this.m_AemScrybeDevice.getAemPrinter();
//            showAlert("Connected with " + printerName);
//        } catch (IOException e) {
//            if (e.getMessage().contains("Service discovery failed")) {
//                showAlert("Not Connected\n" + printerName + " is unreachable or off otherwise it is connected with other device");
//            } else if (e.getMessage().contains("Device or resource busy")) {
//                showAlert("the device is already connected");
//            } else {
//                showAlert("Unable to connect");
//            }
//        }
//        return true;
//    }
//
//    protected void onDestroy() {
//        if (this.m_AemScrybeDevice != null) {
//            try {
//                this.m_AemScrybeDevice.disConnectPrinter();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        super.onDestroy();
//    }
//
//    public void onShowPairedPrinters(View v) {
//        Toast.makeText(getApplicationContext(), this.m_AemScrybeDevice.pairPrinter("BTprinter0314"), 0).show();
//        this.printerList = this.m_AemScrybeDevice.getPairedPrinters();
//        if (this.printerList.size() > 0) {
//            openContextMenu(v);
//        } else {
//            showAlert("No Paired Printers found");
//        }
//    }
//
//    public void onDisconnectDevice(View v) {
//        if (this.m_AemScrybeDevice != null) {
//            try {
//                this.m_AemScrybeDevice.disConnectPrinter();
//                showAlert("disconnected");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public void onReadSmartCard(View v) {
//        if (this.m_cardReader == null) {
//            showAlert("Printer not connected");
//            return;
//        }
//        this.editText.setText("");
//        try {
//            this.m_cardReader.readDL();
//        } catch (IOException e) {
//            showAlert("Printer not connected");
//        }
//    }
//
//    public void onReadMSR(View v) {
//        if (this.m_cardReader == null) {
//            showAlert("Printer not connected");
//            return;
//        }
//        this.editText.setText("");
//        try {
//            this.m_cardReader.readMSR();
//        } catch (IOException e) {
//            showAlert("Printer not connected");
//        }
//    }
//
//    public void onPrintBill(View v) {
//        if (this.m_AemPrinter == null) {
//            showAlert("Printer not connected");
//            return;
//        }
//        try {
//            this.m_AemPrinter.setFontType((byte) 8);
//            this.m_AemPrinter.setFontType((byte) 3);
//            this.m_AemPrinter.print("  TWO INCH PRINTER: TEST PRINT");
//            String d = "________________________________";
//            this.m_AemPrinter.print(d);
//            this.m_AemPrinter.print("SNO|    ITEM   |RATE| QTY|  AMT ");
//            this.m_AemPrinter.print(d);
//            this.m_AemPrinter.setFontType((byte) 6);
//            this.m_AemPrinter.print(" 13|Colgate GEL|  35|  02| 70.0\n 29|Pears Soap |  25|  01| 25.0\n 88|Lux Shower |  46|  01| 46.0\n 15|Dabur Honey|  65|  01| 65.0\n 52|Cadbury DM |  20|  10| 200.0\n128|Maggie Soup|  36|  04| 144.0\n________________________________\n");
//            this.m_AemPrinter.setFontType((byte) 8);
//            this.m_AemPrinter.setFontType((byte) 3);
//            this.m_AemPrinter.print("     TOTAL AMOUNT (Rs.)   550.00\n");
//            this.m_AemPrinter.setFontType(AEMPrinter.FONT_002);
//            this.m_AemPrinter.print(d);
//            String data = " Thank you! Have a pleasant day\n";
//            this.m_AemPrinter.setCarriageReturn();
//            this.m_AemPrinter.setCarriageReturn();
//            this.m_AemPrinter.setCarriageReturn();
//            this.m_AemPrinter.setCarriageReturn();
//        } catch (IOException e) {
//            if (e.getMessage().contains("socket closed")) {
//                showAlert("Printer not connected");
//            }
//        }
//    }
//
//    public void onReadTrack12(View v) {
//    }
//
//    public void onReadTrack3(View v) {
//    }
//
//    public void onPrint(View v) {
//        if (this.m_AemPrinter == null) {
//            showAlert("Printer not connected");
//            return;
//        }
//        try {
//            this.m_AemPrinter.print(this.editText.getText().toString());
//            this.m_AemPrinter.setCarriageReturn();
//            this.m_AemPrinter.setCarriageReturn();
//            this.m_AemPrinter.setCarriageReturn();
//            this.m_AemPrinter.setCarriageReturn();
//        } catch (IOException e) {
//            if (e.getMessage().contains("socket closed")) {
//                showAlert("Printer not connected");
//            }
//        }
//    }
//
//    public void onPrintQRCode(View v) {
//        if (this.m_AemPrinter == null) {
//            showAlert("Printer not connected");
//            return;
//        }
//        try {
//            Bitmap bitmap = this.m_AemPrinter.createQRCode(this.editText.getText().toString());
//            ImageView imageView = (ImageView) findViewById(C0029R.id.image);
//            try {
//                this.m_AemPrinter.printImage(bitmap, getApplicationContext(), AEMPrinter.IMAGE_CENTER_ALIGNMENT);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            imageView.setImageBitmap(bitmap);
//        } catch (WriterException e2) {
//        }
//    }
//
//    public void onPrintBarcode(View v) {
//        if (this.m_AemPrinter == null) {
//            showAlert("Printer not connected");
//            return;
//        }
//        try {
//            this.m_AemPrinter.printBarcode(this.editText.getText().toString(), BARCODE_TYPE.CODE39, BARCODE_HEIGHT.DOUBLEDENSITY_FULLHEIGHT);
//            this.m_AemPrinter.setCarriageReturn();
//            this.m_AemPrinter.setCarriageReturn();
//            this.m_AemPrinter.setCarriageReturn();
//            this.m_AemPrinter.setCarriageReturn();
//        } catch (IOException e) {
//            showAlert("Printer not connected");
//        }
//    }
//
//    public void onPrintImage(View v) {
//        if (this.m_AemPrinter == null) {
//            showAlert("Printer not connected");
//            return;
//        }
//        try {
//            this.m_AemPrinter.setCarriageReturn();
//            this.m_AemPrinter.setCarriageReturn();
//            this.m_AemPrinter.setCarriageReturn();
//            this.m_AemPrinter.setCarriageReturn();
//            this.m_AemPrinter.printImage(BitmapFactory.decodeStream(getAssets().open("aemlogo.jpg")), getApplicationContext(), AEMPrinter.IMAGE_CENTER_ALIGNMENT);
//        } catch (IOException e) {
//        }
//    }
//
//    public void onScanMSR(String buffer, CARD_TRACK cardTrack) {
//        this.cardTrackType = cardTrack;
//        this.creditData = buffer;
//        runOnUiThread(new C00241(buffer));
//    }
//
//    public void onScanDLCard(String buffer) {
//        DLCardData dlCardData = this.m_cardReader.decodeDLData(buffer);
//        String name = "NAME:" + dlCardData.NAME + IOUtils.LINE_SEPARATOR_UNIX;
//        String SWD = "SWD Of: " + dlCardData.SWD_OF + IOUtils.LINE_SEPARATOR_UNIX;
//        String dob = "DOB: " + dlCardData.DOB + IOUtils.LINE_SEPARATOR_UNIX;
//        String dlNum = "DLNUM: " + dlCardData.DL_NUM + IOUtils.LINE_SEPARATOR_UNIX;
//        String issAuth = "ISS AUTH: " + dlCardData.ISS_AUTH + IOUtils.LINE_SEPARATOR_UNIX;
//        String doi = "DOI: " + dlCardData.DOI + IOUtils.LINE_SEPARATOR_UNIX;
//        String tp = "VALID TP: " + dlCardData.VALID_TP + IOUtils.LINE_SEPARATOR_UNIX;
//        runOnUiThread(new C00252(new StringBuilder(String.valueOf(name)).append(SWD).append(dob).append(dlNum).append(issAuth).append(doi).append(tp).append("VALID NTP: " + dlCardData.VALID_NTP + IOUtils.LINE_SEPARATOR_UNIX).toString()));
//    }
//
//    public void onScanRCCard(String buffer) {
//        RCCardData rcCardData = this.m_cardReader.decodeRCData(buffer);
//        String regNum = "REG NUM: " + rcCardData.REG_NUM + IOUtils.LINE_SEPARATOR_UNIX;
//        String regName = "REG NAME: " + rcCardData.REG_NAME + IOUtils.LINE_SEPARATOR_UNIX;
//        runOnUiThread(new C00263(new StringBuilder(String.valueOf(regNum)).append(regName).append("REG UPTO: " + rcCardData.REG_UPTO + IOUtils.LINE_SEPARATOR_UNIX).toString()));
//    }
//
//    public void onScanRFD(String buffer) {
//        StringBuffer stringBuffer = new StringBuffer();
//        stringBuffer.append(buffer);
//        String temp = "";
//        try {
//            temp = stringBuffer.deleteCharAt(8).toString();
//        } catch (Exception e) {
//        }
//        runOnUiThread(new C00274(temp));
//    }
//
//    public void onDiscoveryComplete(ArrayList<String> aemPrinterList) {
//        this.printerList = aemPrinterList;
//        for (int i = 0; i < aemPrinterList.size(); i++) {
//            Log.e("STATUS", this.m_AemScrybeDevice.pairPrinter((String) aemPrinterList.get(i)));
//        }
//    }
//
//    public void onDecodeCreditData(View v) {
//        if (this.m_cardReader == null) {
//            showAlert("Printer not connected");
//        } else if (this.creditData.length() <= 0) {
//            showAlert("The data is unavailable");
//        } else {
//            this.creditDetails = this.m_cardReader.decodeCreditCard(this.creditData, this.cardTrackType);
//            String cardNumber = "cardNumber: " + this.creditDetails.m_cardNumber;
//            String HolderName = "HolderName: " + this.creditDetails.m_AccoundHolderName;
//            String ExpirayDate = "Expiray Date: " + this.creditDetails.m_expiryDate;
//            String ServiceCode = "Service Code: " + this.creditDetails.m_serviceCode;
//            String pvki = "PVKI: " + this.creditDetails.m_pvkiNumber;
//            String pvv = "PVV: " + this.creditDetails.m_pvvNumber;
//            showAlert(new StringBuilder(String.valueOf(cardNumber)).append(IOUtils.LINE_SEPARATOR_UNIX).append(HolderName).append(IOUtils.LINE_SEPARATOR_UNIX).append(ExpirayDate).append(IOUtils.LINE_SEPARATOR_UNIX).append(ServiceCode).append(IOUtils.LINE_SEPARATOR_UNIX).append(pvki).append(IOUtils.LINE_SEPARATOR_UNIX).append(pvv).append(IOUtils.LINE_SEPARATOR_UNIX).append("CVV: " + this.creditDetails.m_cvvNumber).toString());
//        }
//    }
//
//    public void showAlert(String alertMsg) {
//        Builder alertBox = new Builder(this);
//        alertBox.setMessage(alertMsg).setCancelable(false).setPositiveButton("OK", new C00285());
//        alertBox.create().show();
//    }
//}
