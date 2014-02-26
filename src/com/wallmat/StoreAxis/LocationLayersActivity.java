package com.wallmat.StoreAxis;

import android.app.Activity;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by donald on 2/24/14.
 */
public class LocationLayersActivity extends Activity implements BluetoothControlListener{

    private static final String LOGTAG = "StoreAxis";

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothControlThread mBleThread = null;
    private long mLastFlashTime = 0;


    // distance
    private static final int DIST_NONE = -1;
    private static final int DIST_NEAR = 0;
    private static final int DIST_CLOSER = 1;
    private static final int DIST_FAR = 2;

    private ImageView imgNear = null;
    private ImageView imgCloser = null;
    private ImageView imgFar = null;

    private int nNumIndex = 0;
    private int nRssi = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locationlayers);

        imgNear = (ImageView) findViewById(R.id.img_near);
        imgCloser = (ImageView) findViewById(R.id.img_closer);
        imgFar = (ImageView) findViewById(R.id.img_far);

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
        Log.d(LOGTAG, "Adapter: " + mBluetoothAdapter);

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

        // initialize
        if (mBluetoothAdapter.isEnabled())
        {
            mBleThread = new BluetoothControlThread(mBluetoothAdapter, UUID.fromString("F9266FD7-EF07-45D6-8EB6-BD74F13620F9"));
            mBleThread.setListener(this);
            mBleThread.startControl();
        }
        else
            mBleThread = null;
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

        try {
            if (mBleThread != null)
            {
                mBleThread.stopControl();
                mBleThread.join();
                mBleThread = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
}


    private int getDistanceWithRSSI(int rssi)
    {
        if (rssi < -90)
            return DIST_FAR;
        if (rssi < -70)
            return DIST_CLOSER;
        return DIST_NEAR;
    }

    private void resetFlashing()
    {
        imgNear.setImageResource(R.drawable.locate_near_normal);
        imgCloser.setImageResource(R.drawable.locate_closer_normal);
        imgFar.setImageResource(R.drawable.locate_far_normal);
    }

    private void setFlashing(int nIndex)
    {
        if (nIndex == DIST_NONE)
            resetFlashing();
        if (nIndex == DIST_NEAR)
        {
            if (nNumIndex % 2 == 0)
                imgNear.setImageResource(R.drawable.locate_near_normal);
            else
                imgNear.setImageResource(R.drawable.locate_near_flashing);
            imgCloser.setImageResource(R.drawable.locate_closer_normal);
            imgFar.setImageResource(R.drawable.locate_far_normal);
        }
        else if (nIndex == DIST_CLOSER)
        {
            imgNear.setImageResource(R.drawable.locate_near_normal);
            if (nNumIndex % 2 == 0)
                imgCloser.setImageResource(R.drawable.locate_closer_normal);
            else
                imgCloser.setImageResource(R.drawable.locate_closer_flashing);
            imgFar.setImageResource(R.drawable.locate_far_normal);
        }
        else if (nIndex == DIST_FAR)
        {
            imgNear.setImageResource(R.drawable.locate_near_normal);
            imgCloser.setImageResource(R.drawable.locate_closer_normal);
            if (nNumIndex % 2 == 0)
                imgFar.setImageResource(R.drawable.locate_far_normal);
            else
                imgFar.setImageResource(R.drawable.locate_far_flashing);
        }
        nNumIndex++;
    }

    @Override
    public void onScanned() {

    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resetFlashing();
            }
        });
    }

    @Override
    public void onReadRssi(int rssi) {
        nRssi = rssi;
        long curTime = System.currentTimeMillis();
        if (curTime - mLastFlashTime > 1000)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int nIndex = getDistanceWithRSSI(nRssi);
                    setFlashing(nIndex);
                }
            });
            mLastFlashTime = curTime;
        }
    }
}