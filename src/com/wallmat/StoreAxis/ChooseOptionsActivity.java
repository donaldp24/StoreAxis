package com.wallmat.StoreAxis;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.wallmat.StoreAxis.STData.STOptionInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by donald on 2/25/14.
 */
public class ChooseOptionsActivity extends Activity {

    private ListView listView;

    private ArrayList<STOptionInfo> options;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chooseoptions);

        listView = (ListView)findViewById(R.id.list_options);

        options = getOptionList();

        listView.setAdapter(new OptionDataAdapter(ChooseOptionsActivity.this, R.id.list_options, options));

        ResolutionSet._instance.iterateChild(findViewById(R.id.layout_chooseoptions));
    }

    private ArrayList<STOptionInfo> getOptionList()
    {
        ArrayList<STOptionInfo> optionList = new ArrayList<STOptionInfo>();

        STOptionInfo info;

        info = new STOptionInfo();
        info.nId = 0;
        info.nResImg = R.drawable.chooseoption_img_good1;
        info.strExplain = "";
        optionList.add(info);

        info = new STOptionInfo();
        info.nId = 0;
        info.nResImg = R.drawable.chooseoption_img_good2;
        info.strExplain = "";
        optionList.add(info);

        info = new STOptionInfo();
        info.nId = 0;
        info.nResImg = R.drawable.chooseoption_img_good3;
        info.strExplain = "";
        optionList.add(info);

        info = new STOptionInfo();
        info.nId = 0;
        info.nResImg = R.drawable.chooseoption_img_good4;
        info.strExplain = "";
        optionList.add(info);

        info = new STOptionInfo();
        info.nId = 0;
        info.nResImg = R.drawable.chooseoption_img_good5;
        info.strExplain = "";
        optionList.add(info);

        info = new STOptionInfo();
        info.nId = 0;
        info.nResImg = R.drawable.chooseoption_img_good6;
        info.strExplain = "";
        optionList.add(info);

        info = new STOptionInfo();
        info.nId = 0;
        info.nResImg = R.drawable.chooseoption_img_good7;
        info.strExplain = "";
        optionList.add(info);

        return optionList;
    }

    class OptionDataAdapter extends ArrayAdapter<STOptionInfo>
    {
        ArrayList<STOptionInfo> list;
        Context ctx;

        public OptionDataAdapter(Context ctx, int resourceId, ArrayList<STOptionInfo> list) {
            super(ctx, resourceId, list);
            this.ctx = ctx;
            this.list = list;
        }

        @Override
        public int getCount() {
            return (list.size());
        }


        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            LayoutInflater inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if ((position + 1) % 2 == 0)
            {
                v = inflater.inflate(R.layout.evenchooseoption, null);
            }
            else
            {
                v = inflater.inflate(R.layout.oddchooseoption, null);
            }
            ResolutionSet._instance.iterateChild(v.findViewById(R.id.layout_option));

            STOptionInfo info = list.get(position);
            ImageView imgView = (ImageView)v.findViewById(R.id.img_good);
            imgView.setBackgroundResource(info.nResImg);

            TextView txtExplain = (TextView)v.findViewById(R.id.option_txt_exp);
            txtExplain.setText(info.strExplain);

            Button btnBack = (Button)v.findViewById(R.id.btn_back);

            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(ctx, LocationLayersActivity.class);
                    startActivity(intent);
                    overridePendingTransition(TransformManager.GetContinueInAnim(), TransformManager.GetContinueOutAnim());
                    finish();
                }
            });


            Log.d("position : ", position + "");

            return v;
        }
    }
}