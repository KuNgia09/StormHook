package com.storm.hook;

import android.util.Log;

import com.example.DalvikHook.DalvikHookManager;

import dalvik.system.PathClassLoader;
import de.larma.arthook.ArtHook;

/**
 * Created by Administrator on 2017/8/29.
 */

public class main {


    //private static String dalvik_oldPath="/data/local/tmp/libdalvikhook_native.so";
    //private static String art_oldPath="/data/local/tmp/libarthook_native.so";



    public static String TAG="storm";

    public static void Entry(PathClassLoader PathClassLoader, String pkgName, boolean Inject_flag){
        Log.d(TAG,"Inject dex entry is called");
        //Add PathClassLoader so path or use System.load() instead of System.loadLibrary
        OperateDexClassLoader.AddNativeLibraryDirectory(PathClassLoader);
        if(Runtime.isArt()){

            ArtHook.flag=Inject_flag;

            if(ArtHook.flag){

                ArtHook.hook(Inject_Static.class);
            }
            else{

                ArtHook.hook(Inject_SaveOrigProto.class);
            }
        }
        else{
            Runtime.isArt=false;
            DalvikHookManager.hook(Inject_SaveOrigProto.class);
        }

    }

}
