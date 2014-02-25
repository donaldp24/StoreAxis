package com.wallmat.StoreAxis;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import java.util.UUID;

/**
 * Created by donald on 2/24/14.
 */
public class LocationLayersActivity extends Activity {

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 5 seconds.
    private static final long SCAN_PERIOD = 5000;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locationlayers);

        ResolutionSet._instance.iterateChild(findViewById(R.id.layout_locationlayers));

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.str_ble_not_supported, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LocationLayersActivity.this, StartActivity.class);
            startActivity(intent);
            overridePendingTransition(TransformManager.GetBackInAnim(), TransformManager.GetBackOutAnim());
            finish();

            return;
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.str_ble_not_supported, Toast.LENGTH_SHORT).show();


            Intent intent = new Intent(LocationLayersActivity.this, StartActivity.class);
            startActivity(intent);
            overridePendingTransition(TransformManager.GetBackInAnim(), TransformManager.GetBackOutAnim());
            finish();

            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Initializes list view adapter.
        //mLeDeviceListAdapter = new LeDeviceListAdapter();
        //setListAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {

            Intent intent = new Intent(LocationLayersActivity.this, StartActivity.class);
            startActivity(intent);
            overridePendingTransition(TransformManager.GetBackInAnim(), TransformManager.GetBackOutAnim());
            finish();

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        //mLeDeviceListAdapter.clear();
    }


    private void scanLeDevice(final boolean enable) {
        try
        {
            if (enable) {
                // Stops scanning after a pre-defined scan period.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mScanning = false;
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    }
                }, SCAN_PERIOD);

                UUID[] uuids = new UUID[1];
                uuids[0] = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");

                mScanning = true;
                mBluetoothAdapter.startLeScan(uuids, mLeScanCallback);
            } else {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //mLeDeviceListAdapter.addDevice(device);
                            //mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };
    //
}