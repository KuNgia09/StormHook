package com.storm.hook;

import android.util.Log;

import com.example.DalvikHook.DalvikHookManager;

import de.larma.arthook.$;
import de.larma.arthook.Hook;
import de.larma.arthook.OriginalMethod;
import de.larma.arthook.test.MyApplication;

/**
 * Created by Administrator on 2017/8/24.
 */

public  class Inject_SaveOrigProto {

    public static final  String TAG="hook";

    /**
     * Sample hook of a static native method
     */
    @Hook("java.lang.System->currentTimeMillis")
    public static long currentTimeMillis() {
        Log.d("gg", "currentTimeMillis is much better in seconds :)");
        if(Runtime.isArt)
            return (long) OriginalMethod.by(new $() {}).invokeStatic() / 1000L;
        else
            return (long) DalvikHookManager.get().getClassMethodName().invokeStatic() / 1000L;
    }

    @Hook("android.net.wifi.WifiInfo->getMacAddress")
    public String getMacAddress_hook(){
        Log.d(TAG, "getMacAddress is hooked :)");
        if(Runtime.isArt)
            return OriginalMethod.by(new $() {}).invoke(this) ;
        else
            return DalvikHookManager.get().getClassMethodName().invoke(this);
    }




    //@Hook("de.larma.arthook.test.MyActivity->test_public")
    @Hook("com.example.stormhookdemo.MainActivity->test_public")
    public void test_public_hook(){
        Log.d(TAG,"test_public is hooked");
        if(Runtime.isArt)
            OriginalMethod.by(new $() {}).invoke(this);
        else{
            DalvikHookManager.get().getClassMethodName().invoke(this);
        }

    }


    //@Hook("de.larma.arthook.test.MyActivity->test_private")
    @Hook("com.example.stormhookdemo.MainActivity->test_private")
    private int test_private(){
        Log.d(TAG,"test_private is hooked");
        if(Runtime.isArt)
            return OriginalMethod.by(new $() {}).invoke(this);
        else{
            return DalvikHookManager.get().getClassMethodName().invoke(this);
        }

    }

    //@Hook("de.larma.arthook.test.MyActivity->test_privatestatic")
    @Hook("com.example.stormhookdemo.MainActivity->test_privatestatic")
    private static void test_privatestatic(){
        Log.d(TAG,"test_privatestatic is hooked");
        if(Runtime.isArt)
            OriginalMethod.by(new $() {}).invokeStatic();
        else{
            DalvikHookManager.get().getClassMethodName().invokeStatic();
        }

    }










}