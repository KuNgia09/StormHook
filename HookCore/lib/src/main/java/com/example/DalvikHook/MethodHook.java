package com.example.DalvikHook;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.util.Log;

/**
 * Created by qiulinmin on 7/7/17.
 */

public class MethodHook {

    public static void m1(){}
    public static void m2(){}

    private Method srcMethod;
    private Method hookMethod;

    private long backupMethodPtr;

    public MethodHook(Method src, Method dest) {
        srcMethod = src;
        hookMethod = dest;
        srcMethod.setAccessible(true);
        hookMethod.setAccessible(true);
    }

    public void hook() {
        if (backupMethodPtr == 0) {
            backupMethodPtr = hook_native(srcMethod, hookMethod);
        }
    }

    public void restore() {
        if (backupMethodPtr != 0) {
            restore_native(srcMethod, backupMethodPtr);
            backupMethodPtr = 0;
        }
    }

    public Object callOrigin(Object receiver, Object... args) throws InvocationTargetException, IllegalAccessException {
        Object a;
        if (backupMethodPtr != 0) {
            restore();         
            a=srcMethod.invoke(receiver, args);
            hook();
        } else {
            a=srcMethod.invoke(receiver, args);
        }
        return a;
    }
    
    @SuppressWarnings("unchecked")
    public Object callOriginStatic(Object... args){
        Object a=null;
    	if (backupMethodPtr != 0) {
            restore();
            srcMethod.setAccessible(true);
			try {
				a=srcMethod.invoke(null,args);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            hook();
        } else {
        	try {
                srcMethod.setAccessible(true);
				a=srcMethod.invoke(null,args);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			
        }
        return a;
    }

    @SuppressWarnings("unchecked")
    public <T> T invoke(Object receiver, Object... args) {
        try {
            restore();
            srcMethod.setAccessible(true);
            Object a=(T) srcMethod.invoke(receiver, args);
            hook();
            return (T)a;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Calling original method failed", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("InvocationTargetException", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T invokeStatic(Object... args) {
        try {
            restore();
            srcMethod.setAccessible(true);
            Object a=(T) srcMethod.invoke(null, args);
            hook();
            return (T)a;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Calling original method failed", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("InvocationTargetException", e);
        }

    }



    private static native long hook_native(Method src, Method dest);
    private static native Method restore_native(Method src, long methodPtr);

    static {
        System.loadLibrary("dalvikhook_native");
        //System.load("/data/local/tmp/libdalvikhook_native.so");
    }

}
