package com.storm.hook;

import android.app.Activity;
import android.util.Log;

import com.example.DalvikHook.DalvikHookManager;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;

import de.larma.arthook.$;
import de.larma.arthook.Hook;
import de.larma.arthook.OriginalMethod;
import de.larma.arthook.test.MyApplication;

/**
 * Created by Administrator on 2017/8/29.
 */

public class OperateDexClassLoader {

    private static Object getPathList(Object baseDexClassLoader)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    private static Object getField(Object obj, Class<?> cl, String field)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
    }

    private static void setField(Object obj, Class<?> cl, String field,
                                 Object value) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        localField.set(obj, value);
    }

    public static void AddNativeLibraryDirectory(ClassLoader pathClassLoader){
        try {

            //获取PathLoader的pathList对象
            Object pathList = getPathList(pathClassLoader);

            //获取nativeLibraryDirectories属性
            Field nativeLibraryDirectories = pathList.getClass().getDeclaredField("nativeLibraryDirectories");
            nativeLibraryDirectories.setAccessible(true);

            /*Android 6.0下
            /** List of application native library directories.
            /* private final List<File> nativeLibraryDirectories;*/

            /*Android 5.1
            /** List of native library directories. *
            private final File[] nativeLibraryDirectories;*/
            Runtime.sdkVersion = android.os.Build.VERSION.SDK_INT;
            if(Runtime.sdkVersion>22){
                List<File> files1 = (List<File>)nativeLibraryDirectories.get(pathList);
                File tmp=new File("/data/local/tmp/");
                files1.add(tmp);
                nativeLibraryDirectories.set(pathList, files1);
            }
            else{
                File []files1 = (File[])nativeLibraryDirectories.get(pathList);
                Object filesss = Array.newInstance(File.class, files1.length + 1);
                //给PathLoader添加自定义so路径/storage/emulated/legacy/
                Array.set(filesss, 0, new File("/data/local/tmp/"));
                //将系统自己的追加上
                for(int i = 1;i<files1.length+1;i++){
                    Array.set(filesss,i,files1[i-1]);
                }
                nativeLibraryDirectories.set(pathList, filesss);
            }


            /*
             * I/info    (28248): inject success pathList:
             * DexPathList[[zip file "/data/app/com.example.multidexdemo-1.apk", dex file "dalvik.system.DexFile@41df5318"],
             * nativeLibraryDirectories=[/data/app-lib/com.example.multidexdemo-1, /vendor/lib, /system/lib]]
             */
            Log.d("storm", "After addNativeLibraryDirectory pathLoader is:"+pathClassLoader);

        } catch (Exception e) {
            Log.i("storm", "inject dexclassloader error:" + e.toString());
        }
    }



}
