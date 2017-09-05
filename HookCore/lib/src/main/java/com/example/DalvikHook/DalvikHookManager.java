package com.example.DalvikHook;

import com.example.DalvikHook.MethodHook;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.util.Log;
import android.util.Pair;


import de.larma.arthook.Assertions;
import de.larma.arthook.Hook;

import static de.larma.arthook.DebugHelper.logw;

/**
 * Created by qiulinmin on 7/10/17.
 */

public final class DalvikHookManager {

    private DalvikHookManager(){}

    public static DalvikHookManager get() {
        return InstanceHolder.sInstance;
    }

    private static class InstanceHolder {
        private static DalvikHookManager sInstance = new DalvikHookManager();
    }

    private Map<Pair<String, String>, MethodHook> methodHookMap = new ConcurrentHashMap<>();

    public void hookMethod(Method originMethod, Method hookMethod) {
        if (originMethod == null || hookMethod == null) {
            throw new IllegalArgumentException("argument cannot be null");
        }
//        if (!Modifier.isStatic(hookMethod.getModifiers())) {
//            throw new IllegalArgumentException("hook method must be static");
//        }
        Pair<String, String> key = Pair.create(hookMethod.getDeclaringClass().getName(), hookMethod.getName());
        if (methodHookMap.containsKey(key)) {
            MethodHook methodHook = methodHookMap.get(key);
            methodHook.restore();
        }
        MethodHook methodHook = new MethodHook(originMethod, hookMethod);
        methodHookMap.put(key, methodHook);
        methodHook.hook();
    }

    private static Object myFindTargetMethod(Method method, Class<?> targetClass, String methodName)
            throws NoSuchMethodException {
        Class<?>[] params = null;

        if (methodName.equals("()") || methodName.equals("<init>")) {
            // Constructor
            return targetClass.getConstructor(params);
        }
        try {
            Class<?>[] a=method.getParameterTypes();
            Method m = targetClass.getDeclaredMethod(methodName, a);
            return m;
        } catch (NoSuchMethodException ignored) {
            Log.d("storm",ignored.toString());
        }

        throw new NoSuchMethodException();
    }

    static Object findTargetMethod(Method method) throws NoSuchMethodException, ClassNotFoundException {
        Hook hook = method.getAnnotation(Hook.class);
        String[] split = hook.value().split("->");
        Class<?> a=Class.forName(split[0]);
        String b=split.length == 1 ? method.getName() : split[1];

        return myFindTargetMethod(method,a , b);
    }

    public static void hook(Method method) {
        if (!method.isAnnotationPresent(Hook.class))
            throw new IllegalArgumentException("method must have @Hook annotation");

        Method original;
        try {
            original =(Method)findTargetMethod(method);
        } catch (Throwable e) {
            Log.d("storm","Can't find original method (" + method.getName() + ")"+e.toString());
            throw new RuntimeException("Can't find original method (" + method.getName() + ")", e);
        }
        Log.d("storm","[+]find original method (" + method.getName() + ")");

        DalvikHookManager.get().hookMethod(original,method);
    }


    public static void hook(Class clazz) {
        for (Method method : Assertions.argumentNotNull(clazz, "clazz").getDeclaredMethods()) {
            if (method.isAnnotationPresent(Hook.class)) {
                try {
                    hook(method);
                } catch (RuntimeException e) {
                    logw(e);
                }
            }
        }
    }
    public Object callOrigin(Object receiver, Object... args) {
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[3];
        String className = stackTrace.getClassName();
        String methodName = stackTrace.getMethodName();
        MethodHook methodHook = methodHookMap.get(Pair.create(className, methodName));
        try {
            return methodHook.callOrigin(receiver, args);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;

    }

    public Object callOriginStatic(Object... args) {
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[3];
        String className = stackTrace.getClassName();
        String methodName = stackTrace.getMethodName();
        MethodHook methodHook = methodHookMap.get(Pair.create(className, methodName));
        return methodHook.callOriginStatic(args);
    }


    public MethodHook getClassMethodName(){
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[3];
        String className = stackTrace.getClassName();
        String methodName = stackTrace.getMethodName();
        MethodHook methodHook = methodHookMap.get(Pair.create(className, methodName));
        return methodHook;
    }

}
