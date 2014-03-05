package com.wallmat.StoreAxis;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.ImageView;
import com.radiusnetworks.ibeacon.*;

import java.util.Collection;
import java.util.UUID;
import android.os.Handler;

/**
 * Created by donald on 3/5/14.
 */
public class BeaconLayerActivity extends Activity implements IBeaconConsumer {
    private static final String LOGTAG = "StoreAxis";
    //private UUID uuidBle = UUID.fromString("E2C56DB5-DFFB-48D2-B060-D0F5A71096E0");
    private String uuidBle = "E2C56DB5-DFFB-48D2-B060-D0F5A71096E0";
    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);

    // distance
    private static final int DIST_NONE = -1;
    private static final int DIST_NEAR = 0;
    private static final int DIST_CLOSER = 1;
    private static final int DIST_FAR = 2;

    private ImageView imgNear = null;
    private ImageView imgCloser = null;
    private ImageView imgFar = null;
    private ImageView imgNearFlashing = null;
    private ImageView imgCloserFlashing = null;
    private ImageView imgFarFlashing = null;


    private int nNumIndex = 0;
    private int nRssi = 0;

/*
    private Handler flashingHandler = new Handler();
    private Runnable flashing = new Runnable() {
        @Override
        public void run() {
            if (nRssi == 0)
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resetFlashing();
                    }
                });
            else
                runOnUiThread(new Runnable() {
                    @Override
                public void run() {
                        setFlashing(getDistanceWithRSSI(nRssi));
                    }
                });
        }
    };
*/
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locationlayers);

        imgNear = (ImageView) findViewById(R.id.img_near);
        imgCloser = (ImageView) findViewById(R.id.img_closer);
        imgFar = (ImageView) findViewById(R.id.img_far);
        imgNearFlashing = (ImageView) findViewById(R.id.img_near_flashing);
        imgCloserFlashing = (ImageView) findViewById(R.id.img_closer_flahsing);
        imgFarFlashing = (ImageView) findViewById(R.id.img_far_flahsing);

        ResolutionSet._instance.iterateChild(findViewById(R.id.layout_locationlayers));


        iBeaconManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        iBeaconManager.unBind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (iBeaconManager.isBound(this)) iBeaconManager.setBackgroundMode(this, true);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (iBeaconManager.isBound(this)) iBeaconManager.setBackgroundMode(this, false);
    }
    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
                if (iBeacons.size() > 0) {
                    //logToDisplay("The first iBeacon I see is about "+iBeacons.iterator().next().getAccuracy()+" meters away.");
                    nRssi = iBeacons.iterator().next().getRssi();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setFlashing(getDistanceWithRSSI(nRssi));
                        }
                    });
                }
                else
                {
                    nRssi = 0;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setFlashing(getDistanceWithRSSI(nRssi));
                        }
                    });
                    /*
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resetFlashing();
                        }
                    });*/
                }
            }

        });

        try {
            iBeaconManager.startRangingBeaconsInRegion(new Region(uuidBle, null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private int getDistanceWithRSSI(int rssi)
    {
        if (rssi == 0)
            return DIST_FAR;
        if (rssi < -90)
            return DIST_FAR;
        if (rssi < -70)
            return DIST_CLOSER;
        return DIST_NEAR;
    }

    private void resetFlashing()
    {
        imgNear.setVisibility(View.VISIBLE);
        imgNearFlashing.setVisibility(View.INVISIBLE);
        imgCloser.setVisibility(View.VISIBLE);
        imgCloserFlashing.setVisibility(View.INVISIBLE);
        imgFar.setVisibility(View.VISIBLE);
        imgFarFlashing.setVisibility(View.INVISIBLE);

    }

    private void setFlashing(int nIndex)
    {
        if (nIndex == DIST_NONE)
            resetFlashing();
        if (nIndex == DIST_NEAR)
        {
            if (nNumIndex % 2 == 0) {
                imgNear.setVisibility(View.VISIBLE);
                imgNearFlashing.setVisibility(View.INVISIBLE);
            } else {
                imgNear.setVisibility(View.INVISIBLE);
                imgNearFlashing.setVisibility(View.VISIBLE);
            }


            imgCloser.setVisibility(View.VISIBLE);
            imgCloserFlashing.setVisibility(View.INVISIBLE);
            imgFar.setVisibility(View.VISIBLE);
            imgFarFlashing.setVisibility(View.INVISIBLE);


        }
        else if (nIndex == DIST_CLOSER)
        {

            imgNear.setVisibility(View.VISIBLE);
            imgNearFlashing.setVisibility(View.INVISIBLE);

            if (nNumIndex % 2 == 0) {

                imgCloser.setVisibility(View.VISIBLE);
                imgCloserFlashing.setVisibility(View.INVISIBLE);

            } else {

                imgCloser.setVisibility(View.INVISIBLE);
                imgCloserFlashing.setVisibility(View.VISIBLE);

            }

            imgFar.setVisibility(View.VISIBLE);
            imgFarFlashing.setVisibility(View.INVISIBLE);
        }
        else if (nIndex == DIST_FAR)
        {
            imgNear.setVisibility(View.VISIBLE);
            imgNearFlashing.setVisibility(View.INVISIBLE);
            imgCloser.setVisibility(View.VISIBLE);
            imgCloserFlashing.setVisibility(View.INVISIBLE);

            if (nNumIndex % 2 == 0) {
                imgFar.setVisibility(View.VISIBLE);
                imgFarFlashing.setVisibility(View.INVISIBLE);
            }else{
                imgFar.setVisibility(View.INVISIBLE);
                imgFarFlashing.setVisibility(View.VISIBLE);
            }
        }
        nNumIndex++;
    }
}