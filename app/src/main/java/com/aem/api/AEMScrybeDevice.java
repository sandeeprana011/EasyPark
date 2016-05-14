package com.aem.api;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build.VERSION;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class AEMScrybeDevice {
    private static final UUID MY_UUID_SECURE;
    final String VERSION;
    private BluetoothSocket accBTSocket;
    private BluetoothSocket bluetoothSocket;
    private Context context;
    private boolean deviceFound;
    private BluetoothAdapter localDevice;
    Object mutex;
    private boolean pairDevice;
    private BluetoothDevice remoteDevice;
    private ArrayList<BluetoothDevice> remoteDeviceList;
    private boolean scanFlag;
    IAemScrybe scrybeDeviceInterface;

    private class DeviceFoundReceiver extends BroadcastReceiver {
        DeviceFoundReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            AEMScrybeDevice.this.remoteDeviceList.add((BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE"));
        }
    }

    private class DiscoveryReciever extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.bluetooth.adapter.action.DISCOVERY_FINISHED")) {
                ArrayList<String> printerList = new ArrayList();
                for (int i = 0; i < AEMScrybeDevice.this.remoteDeviceList.size(); i++) {
                    if (((BluetoothDevice) AEMScrybeDevice.this.remoteDeviceList.get(i)).getName().contains("rinter")) {
                        printerList.add(((BluetoothDevice) AEMScrybeDevice.this.remoteDeviceList.get(i)).getName());
                    }
                }
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                context.unregisterReceiver(this);
                AEMScrybeDevice.this.scrybeDeviceInterface.onDiscoveryComplete(printerList);
            }
        }
    }

    static {
        MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    }

    public AEMScrybeDevice(IAemScrybe impl) {
        this.VERSION = "1.0";
        this.remoteDeviceList = new ArrayList();
        this.deviceFound = false;
        this.pairDevice = false;
        this.scanFlag = false;
        this.scrybeDeviceInterface = impl;
    }

    public void startDiscover(Context iContext) {
        this.scanFlag = true;
        this.remoteDeviceList.clear();
        this.localDevice = null;
        this.context = iContext;
        this.localDevice = BluetoothAdapter.getDefaultAdapter();
        if (this.localDevice != null) {
            try {
                if (!this.localDevice.isEnabled()) {
                    this.localDevice.enable();
                }
            } catch (Exception e) {
            }
            DiscoveryReciever discoverReceiver = new DiscoveryReciever();
            iContext.registerReceiver(new DeviceFoundReceiver(), new IntentFilter("android.bluetooth.device.action.FOUND"));
            iContext.registerReceiver(discoverReceiver, new IntentFilter("android.bluetooth.adapter.action.DISCOVERY_FINISHED"));
            BluetoothAdapter.getDefaultAdapter().startDiscovery();
        }
    }

    public String pairPrinter(String printerName) {
        if (!this.scanFlag) {
            return "NOT_SCANNED";
        }
        for (int i = 0; i < this.remoteDeviceList.size(); i++) {
            if (printerName.contentEquals(((BluetoothDevice) this.remoteDeviceList.get(i)).getName())) {
                this.deviceFound = true;
                this.pairDevice = pairDevice((BluetoothDevice) this.remoteDeviceList.get(i));
                break;
            }
        }
        if (!this.deviceFound) {
            return "DEVICE_NOT_FOUND";
        }
        if (this.pairDevice) {
            return "PAIRED";
        }
        return "FAILED_TO_PAIRED";
    }

    public String getSDKVersion() {
        return "1.0";
    }

    public boolean connectToPrinter(String printerName) throws IOException {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        BluetoothDevice device = getPrinterByName(printerName);
        if (this.bluetoothSocket != null) {
            this.bluetoothSocket.close();
        }
        String deviceVersion = VERSION.RELEASE;
        this.bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_SECURE);
        if (this.bluetoothSocket == null) {
            try {
                this.bluetoothSocket = (BluetoothSocket) device.getClass().getMethod("createInsecureRfcommSocket", new Class[]{Integer.TYPE}).invoke(device, new Object[]{Integer.valueOf(1)});
            } catch (Exception e) {
                return false;
            }
        }
        if (this.bluetoothSocket == null) {
            return false;
        }
        this.bluetoothSocket.connect();
        return true;
    }

    public boolean disConnectPrinter() throws IOException {
        if (this.bluetoothSocket == null) {
            return false;
        }
        this.bluetoothSocket.getOutputStream().flush();
        this.bluetoothSocket.getInputStream().close();
        this.bluetoothSocket.getOutputStream().close();
        this.bluetoothSocket.close();
        this.bluetoothSocket = null;
        return true;
    }

    private BluetoothDevice getPrinterByName(String printerName) {
        for (BluetoothDevice device : BluetoothAdapter.getDefaultAdapter().getBondedDevices()) {
            if (device.getName() != null && device.getName().contains(printerName)) {
                this.remoteDevice = device;
                return device;
            }
        }
        return null;
    }

    private boolean pairDevice(BluetoothDevice device) {
        if (device.getBondState() == 12) {
            return true;
        }
        try {
            device.getClass().getMethod("createBond", null).invoke(device, null);
            return true;
        } catch (Exception e) {
            e.getMessage();
            return false;
        }
    }

    public CardReader getCardReader(IAemCardScanner readerImpl) {
        if (this.bluetoothSocket == null) {
            return null;
        }
        return new CardReader(this.bluetoothSocket, readerImpl);
    }

    public AEMPrinter getAemPrinter() {
        if (this.bluetoothSocket == null) {
            return null;
        }
        return new AEMPrinter(this.bluetoothSocket);
    }

    public ArrayList<String> getPairedPrinters() {
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().enable();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        ArrayList<String> pairedPrinters = new ArrayList();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName() != null && device.getName().contains("rinter")) {
                pairedPrinters.add(device.getName());
            }
        }
        return pairedPrinters;
    }
}
