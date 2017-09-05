package com.storm.hook;

import android.app.Activity;
import android.util.Log;

import de.larma.arthook.$;
import de.larma.arthook.Hook;
import de.larma.arthook.OriginalMethod;

/**
 * Created by Administrator on 2017/8/20.
 * This is only for Art Hook Function decleration
 */

public  class Inject_Static {

    public static final  String TAG="hook";



    @Hook("java.lang.System->currentTimeMillis")
    public static long System_currentTimeMillis() {
        Log.d(TAG, "currentTimeMillis is hooked)");
        return (long) OriginalMethod.by(new $() {}).invokeStatic() ;
    }


    @Hook("android.net.wifi.WifiInfo->getMacAddress")
    public static  String getMacAddress_hook(Object a){
        Log.d(TAG, "getMacAddress is hooked :)");
        return OriginalMethod.by(new $() {}).invoke(a) ;

    }


    //@Hook("com.example.stromhooktest.MainActivity-><init>")
    public static void MainActivity_init(Activity a){
        Log.d(TAG,"MainActivity <init> is hooked");
        OriginalMethod.by(new $() {}).invoke(a);
    }

    //@Hook("de.larma.arthook.test.MyActivity->test_public")
    @Hook("com.example.stormhookdemo.MainActivity->test_public")
    public static void test_public_hook(Object a){
        Log.d(TAG,"test_public is hooked");
        OriginalMethod.by(new $() {}).invoke(a);


    }


    //@Hook("de.larma.arthook.test.MyActivity->test_private")
    @Hook("com.example.stormhookdemo.MainActivity->test_private")
    public  static int test_private(Object a){
        Log.d(TAG,"test_private is hooked");

        return OriginalMethod.by(new $() {}).invoke(a);


    }

    //@Hook("de.larma.arthook.test.MyActivity->test_privatestatic")
    @Hook("com.example.stormhookdemo.MainActivity->test_privatestatic")
    public static void test_privatestatic(){
        Log.d(TAG,"test_private is hooked");
        OriginalMethod.by(new $() {}).invokeStatic();

    }






}

