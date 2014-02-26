package com.wallmat.StoreAxis;

import android.bluetooth.*;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by donald on 2/26/14.
 */
public class BluetoothControlThread extends Thread {

    private static final String LOGTAG = "StoreAxis";

    private boolean bRunning = true;
    private BluetoothAdapter mAdapter = null;
    private UUID mServiceUuid = null;
    private BluetoothGattService mService = null;
    private BluetoothGatt mGatt = null;
    private BluetoothDevice mDevice = null;
    private int mRssi = 0;
    private BluetoothControlListener mListener = null;

    private long mLastReadRssiTime = 0;

    private final int STATE_BLENONE = -1;
    private final int STATE_BLESTARTSCAN = 0;
    private final int STATE_BLESCANNING = 10;
    private final int STATE_BLESCANTRIGGERED = 11;
    private final int STATE_BLESCANNED = 20;
    private final int STATE_BLECONNECTING = 21;
    private final int STATE_BLECONNECTTRIGGERED = 22;
    private final int STATE_BLECONNECTED = 30;
    private final int STATE_BLEFLASHING = 40;
    private final int STATE_BLEDISCONNECTED = 50;
    private final int STATE_BLESTOP = 60;

    private int nState = STATE_BLENONE;

    private boolean mConnected = false;
    private boolean mDisconnected = false;
    private boolean mScanned = false;

    public BluetoothControlThread(BluetoothAdapter adapter, UUID serviceUuid)
    {
        mAdapter = adapter;
        mServiceUuid = serviceUuid;
    }

    public void setListener(BluetoothControlListener listener)
    {
        mListener = listener;
    }

    public void startControl()
    {
        super.start();
        if (nState == STATE_BLENONE)
            nState = STATE_BLESTARTSCAN;
    }

    public void stopControl()
    {
        nState = STATE_BLESTOP;
    }

    @Override
    public void run() {
        while(bRunning) {
            long currTime = System.currentTimeMillis();
            switch (nState)
            {
                case STATE_BLENONE:
                    Log.d(LOGTAG, "state : STATE_BLENONE");
                    break;
                case STATE_BLESTARTSCAN:
                    nState = STATE_BLESCANNING;
                    Log.d(LOGTAG, "state : STATE_BLESTARTSCAN => STATE_BLESCANNING");
                    //break;
                case STATE_BLESCANNING:
                    nState = STATE_BLESCANTRIGGERED;
                    Log.d(LOGTAG, "state : STATE_BLESCANNING => STATE_BLESCANTRIGGERED");
                    // start scanning
                    scanLeDevice(true);
                    break;
                case STATE_BLESCANTRIGGERED:
                    if (mScanned == true)
                    {
                        nState = STATE_BLESCANNED;
                        Log.d(LOGTAG, "state : STATE_BLESCANTRIGGERED => STATE_BLESCANNED");
                        mScanned = false;
                    }
                    break;
                case STATE_BLESCANNED:

                    if (mListener != null)
                        mListener.onScanned();

                    // stop scanning
                    scanLeDevice(false);
                    nState = STATE_BLECONNECTING;
                    Log.d(LOGTAG, "state : STATE_BLESCANNED => STATE_BLECONNECTING");
                    break;
                case STATE_BLECONNECTING:
                    // connect to service
                    nState = STATE_BLECONNECTTRIGGERED;
                    Log.d(LOGTAG, "state : STATE_BLECONNECTING => STATE_BLECONNECTTRIGGERED");
                    mGatt = mDevice.connectGatt(null, false, mGattCallback);
                    break;
                case STATE_BLECONNECTTRIGGERED:
                    if (mConnected == true)
                    {
                        nState = STATE_BLECONNECTED;
                        Log.d(LOGTAG, "state : STATE_BLECONNECTTRIGGERED => STATE_BLECONNECTED");
                        mConnected = false;
                    }
                    if (mDisconnected == true)
                    {
                        nState = STATE_BLEDISCONNECTED;
                        Log.d(LOGTAG, "state : STATE_BLECONNECTTRIGGERED => STATE_BLEDISCONNECTED");
                        mDisconnected = false;
                    }
                    break;
                case STATE_BLECONNECTED:
                    if (mDisconnected == true)
                    {
                        mDisconnected = false;
                        nState = STATE_BLEDISCONNECTED;
                        Log.d(LOGTAG, "state : STATE_BLECONNECTED => STATE_BLEDISCONNECTED");
                        break;
                    }
                    if (mListener != null)
                        mListener.onConnected();
                    mLastReadRssiTime = System.currentTimeMillis();
                    nState = STATE_BLEFLASHING;
                    Log.d(LOGTAG, "state : STATE_BLECONNECTED => STATE_BLEFLASHING");
                    break;
                case STATE_BLEFLASHING:
                    if (mDisconnected == true)
                    {
                        mDisconnected = false;
                        nState = STATE_BLEDISCONNECTED;
                        Log.d(LOGTAG, "state : STATE_BLEFLASHING => STATE_BLEDISCONNECTED");
                        break;
                    }

                    if (mRssi != 0)
                    {
                        if (mListener != null)
                            mListener.onReadRssi(mRssi);
                    }
                    if (currTime - mLastReadRssiTime >= 500)
                    {
                        // read rssi
                        mGatt.readRemoteRssi();
                        mLastReadRssiTime = currTime;
                    }
                    break;
                case STATE_BLEDISCONNECTED:
                    if (mGatt != null)
                        mGatt.close();
                    mGatt = null;
                    mDevice = null;
                    mService = null;
                    mRssi = 0;

                    if (mListener != null)
                        mListener.onDisconnected();

                    // start scanning
                    nState = STATE_BLESTARTSCAN;
                    Log.d(LOGTAG, "state : STATE_BLEDISCONNECTED => STATE_BLESTARTSCAN");
                    break;
                case STATE_BLESTOP:
                    nState = STATE_BLENONE;
                    Log.d(LOGTAG, "state : STATE_BLESTOP => STATE_BLENONE");

                    if (mAdapter != null)
                        mAdapter.stopLeScan(mLeScanCallback);
                    bRunning = false;
                    if (mGatt != null)
                        mGatt.close();
                    mGatt = null;
                    mDevice = null;
                    mService = null;
                    mRssi = 0;
                    break;
                default:
                    break;
            }

            try {
                Thread.sleep(50, 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void scanLeDevice(final boolean enable) {
        try
        {
            if (enable) {
                mAdapter.startLeScan(mLeScanCallback);
                Log.d(LOGTAG, "scanLeDevice(true): scanning started");
            } else {
                mAdapter.stopLeScan(mLeScanCallback);
                Log.d(LOGTAG, "scanLeDevice(false): scanning stopped");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.d(LOGTAG, "scanLeDevice : " + e);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    Log.d(LOGTAG, "LeScanCallback : " + "device = " + device + ", rssi = " + rssi);
                    if (hasServices(scanRecord))
                    {
                        mDevice = device;
                        mScanned = true;
                    }
                }
            };

    //Gatt Callback
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            switch(newState)
            {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.d(LOGTAG, "onConnectionStateChange : " + gatt.getDevice() + ": Connected!");
                    mService = gatt.getService(mServiceUuid);
                    mConnected = true;
                    break;
                case BluetoothProfile.STATE_CONNECTING:
                    Log.d(LOGTAG, "onConnectionStateChange: " + gatt.getDevice() + ": Connecting...");
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.d(LOGTAG, "onConnectionStateChange: " + gatt.getDevice() + ": Disconnected!");
                    mDisconnected = true;
                    break;
                case BluetoothProfile.STATE_DISCONNECTING:
                    Log.d(LOGTAG, "onConnectionStateChange: " + gatt.getDevice() + ": Disconnecting...");
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.d(LOGTAG, "onReadRemoteRssi: " + gatt.getDevice() + ": rssi = " + rssi + ", status = " + status);
            mRssi = rssi;
        }
    };

    private boolean hasServices(byte[] scanRecord) {
        List<UUID> uuids = parseUUIDs(scanRecord);
        UUID serviceUuids[] = new UUID[]{mServiceUuid};
        for(int i = 0; i < uuids.size(); i++)
        {
            for(int j = 0; j < serviceUuids.length; j++)
            {
                if (serviceUuids[j].toString().equalsIgnoreCase(uuids.get(i).toString()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private List<UUID> parseUUIDs(final byte[] advertisedData) {
        List<UUID> uuids = new ArrayList<UUID>();

        int offset = 0;
        while (offset < (advertisedData.length - 2)) {
            int len = advertisedData[offset++];
            if (len == 0)
                break;

            int type = advertisedData[offset++];
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (len > 1) {
                        int uuid16 = advertisedData[offset++];
                        uuid16 += (advertisedData[offset++] << 8);
                        len -= 2;
                        uuids.add(UUID.fromString(String.format(
                                "%08x-0000-1000-8000-00805f9b34fb", uuid16)));
                    }
                    break;
                case 0x06:// Partial list of 128-bit UUIDs
                case 0x07:// Complete list of 128-bit UUIDs
                    // Loop through the advertised 128-bit UUID's.
                    while (len >= 16) {
                        try {
                            // Wrap the advertised bits and order them.
                            ByteBuffer buffer = ByteBuffer.wrap(advertisedData,
                                    offset++, 16).order(ByteOrder.LITTLE_ENDIAN);
                            long mostSignificantBit = buffer.getLong();
                            long leastSignificantBit = buffer.getLong();
                            uuids.add(new UUID(leastSignificantBit,
                                    mostSignificantBit));
                        } catch (IndexOutOfBoundsException e) {
                            // Defensive programming.
                            Log.e(LOGTAG, e.toString());
                            continue;
                        } finally {
                            // Move the offset to read the next uuid.
                            offset += 15;
                            len -= 16;
                        }
                    }
                    break;
                default:
                    offset += (len - 1);
                    break;
            }
        }
        return uuids;
    }
}
