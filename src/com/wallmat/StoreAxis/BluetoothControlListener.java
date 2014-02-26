package com.wallmat.StoreAxis;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

/**
 * Created by donald on 2/26/14.
 */
public interface BluetoothControlListener {
    public void onScanned();
    public void onConnected();
    public void onDisconnected();
    public void onReadRssi(int rssi);
}
