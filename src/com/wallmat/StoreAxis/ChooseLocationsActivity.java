package com.wallmat.StoreAxis;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by donald on 2/25/14.
 */
public class ChooseLocationsActivity extends Activity {
    private HorizontalPager mHorPager = null;
    private ImageView imgSel = null;
    private int nSelTarget = 0;

    private int nResSelArray[] = {R.drawable.locations_sel1, R.drawable.locations_sel2, R.drawable.locations_sel3};

    HorizontalPager.OnScreenSwitchListener scroll_listener = new HorizontalPager.OnScreenSwitchListener() {

        @Override
        public void onScreenSwitched(int screen) {
            //To change body of implemented methods use File | Settings | File Templates.
            //mHorPager.setCurrentScreen(screen, false);]
            if (screen < nResSelArray.length)
            {
                nSelTarget = screen;
                if (imgSel != null)
                    imgSel.setImageResource(nResSelArray[screen]);
            }
        }

        @Override
        public void onScreenTapped(int screen) {
            Intent intent = new Intent(ChooseLocationsActivity.this, ChooseOptionsActivity.class);
            startActivity(intent);
            overridePendingTransition(TransformManager.GetContinueInAnim(), TransformManager.GetContinueOutAnim());
            finish();
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locations);

        ResolutionSet._instance.iterateChild(findViewById(R.id.layout_locations));

        initControls();
    }

    private void initControls() {
        mHorPager = (HorizontalPager)findViewById(R.id.hor_pager);

        ViewGroup.LayoutParams layout_params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        ImageView imgView1 = new ImageView(mHorPager.getContext());
        imgView1.setImageResource(R.drawable.locations_lowes);
        imgView1.setLayoutParams(layout_params);

        ImageView imgView2 = new ImageView(mHorPager.getContext());
        imgView2.setImageResource(R.drawable.locations_target);
        imgView2.setLayoutParams(layout_params);

        ImageView imgView3 = new ImageView(mHorPager.getContext());
        imgView3.setImageResource(R.drawable.locations_walmart);
        imgView3.setLayoutParams(layout_params);

        mHorPager.addView(imgView1);
        mHorPager.addView(imgView2);
        mHorPager.addView(imgView3);

        imgSel = (ImageView)findViewById(R.id.img_sel);

        mHorPager.setOnScreenSwitchListener(scroll_listener);
    }
}