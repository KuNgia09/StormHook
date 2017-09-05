package de.larma.arthook.test;

import android.app.Activity;
import android.content.Context;
import android.net.sip.SipAudioCall;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MyActivity extends Activity {

    private static final String TAG = "storm";

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Log.d(TAG, "setContentView(" + Integer.toHexString(R.layout.activity_my) + ");");
            setContentView(R.layout.activity_my);


            Button bt_action = (Button) findViewById(R.id.button_action);

            Button bt_getCurrenttime=(Button)findViewById(R.id.button_getCurrenttime);
            Button bt_getWifiAddr=(Button)findViewById(R.id.button_getWifiAddr);

            Button bt_public=(Button)findViewById(R.id.button_public);
            Button bt_private=(Button)findViewById(R.id.button_private);
            Button bt_privatecstatic=(Button)findViewById(R.id.button_privatestatic);

            bt_action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    test_public();
                    test_private();
                    test_privatestatic();

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
            /*
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                    String serialnumber = telephonyManager.getSimSerialNumber();
                    Log.d(TAG,"serialnumber:"+serialnumber);
                }
            });
            */
        } catch (Exception e) {
            Log.d(TAG, "Catching exception");
            Log.d(TAG, "e: ", e);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        Log.d(TAG, "before Activity.setContentView");
        super.setContentView(layoutResID);
        Log.d(TAG, "after Activity.setContentView");
    }








    private static void showToast(Activity a,String msg) {
        Toast toast = Toast.makeText(a, msg, Toast.LENGTH_SHORT);
        toast.show();

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
