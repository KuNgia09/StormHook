package com.storm.hook;

/**
 * Created by Administrator on 2017/8/27.
 */

public class Runtime {
    public static boolean isArt=true;
    public static int sdkVersion=0;
    public static boolean isArt() {
        return getVmVersion().startsWith("2");
    }

    public static String getVmVersion() {
        return System.getProperty("java.vm.version");
    }
}
