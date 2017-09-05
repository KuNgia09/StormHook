package com.example.stormhookdemo;


import com.example.stormhookdemo.R;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	
	private static final String TAG = "storm";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button bt_action = (Button) findViewById(R.id.button_action);

        Button bt_getCurrenttime=(Button)findViewById(R.id.button_getCurrenttime);
        Button bt_getWifiAddr=(Button)findViewById(R.id.button_getWifiAddr);

        Button bt_public=(Button)findViewById(R.id.button_public);
        Button bt_private=(Button)findViewById(R.id.button_private);
        Button bt_privatecstatic=(Button)findViewById(R.id.button_privatestatic);

        bt_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        bt_getCurrenttime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long currentime=System.currentTimeMillis();
                Log.d(TAG,"small Currentime:"+currentime);
            }
        });

        bt_getWifiAddr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = wifi.getConnectionInfo();
                Log.d("storm","Wifi mac :" + info.getMacAddress());
            }
        });

        bt_public.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test_public();
            }
        });


        bt_private.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tmp=test_private();
                Log.d(TAG,"test_private return:"+tmp);
            }
        });
        
        bt_privatecstatic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test_privatestatic();

            }
        });
        
    } 
	
	public void  test_public(){
        Log.d("storm","test_public is called");
    }

    private int test_private(){
        Log.d("storm","test_private is called");
        return 10;
    }

    private static void test_privatestatic(){
        Log.d("storm","test_privatestatic is called");
    }

	
	
	
	
	
}
