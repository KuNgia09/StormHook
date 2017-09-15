# StormHook:Android侵入式Java Hook框架
* 支持Android Art和Dalvik
* 支持Andorid 4.0-6.0
* 支持注入到其他进程Hook
* 支持public，static，private方法
* 支持调用原始的Java方法

# 模块
* StormHookSample：用来测试Hook效果的例子
* InjectSo：用来注入到StormHookSample进程的so文件
* HookCore: 加载到StormHookSample进程的dex

# 用法
Step1：
编译InjectSo模块 生成**libhook.so**
```C
adb push libhook.so /data/local/tmp/
```
Step2：HookCore模块是Android Studio工程，将生成的Apk中的**classes.dex**提取出来重命名为**hook.dex**
```C
adb push hook.dex /data/local/tmp/
```

Step3: 
HookCore/Native是jni工程 编译生成**libdalvikhook_native.so**和**libarthook_native.so**
```C
adb push libdalvikhook_native.so /data/local/tmp/
adb push libarthook_native.so /data/local/tmp/
```

Step4:
关闭**selinux**
```C
root@hammerhead:/ # setenforce 0
root@hammerhead:/ # getenforce
Permissive
```

Step5:
注入**libhook.so**到StormHookSample App中
```C
root@hammerhead:/data/local/tmp # ps |grep storm
u0_a71    17772 182   923264 41632 ffffffff 400ca73c S com.example.stormhookdemo

root@hammerhead:/data/local/tmp # ./inject /data/local/tmp/libhook.so 17772
target_pid:456c,soPath:/data/local/tmp/libhook.so
library path = /data/local/tmp/libhook.so
```




hook成功显示的log
```C
adb logcat -s "storm"
V/storm   (17772): g_JavaVM:414cef00
V/storm   (17772): LoadDex optFile path:/data/data/com.example.stormhookdemo/hoo
k.dat
V/storm   (17772): classLoaders size:1
V/storm   (17772): original Element size:1
V/storm   (17772): ClassMethodHook[Can't find class:com/storm/hook/main in bootc
lassloader
V/storm   (17772): loadClass com/storm/hook/main successful clazz:0x1d600059
D/storm   (17772): Inject dex entry is called
D/storm   (17772): After addNativeLibraryDirectory pathLoader is:dalvik.system.P
athClassLoader[DexPathList[[zip file "/data/app/com.example.stormhookdemo-1.apk"
, dex file "dalvik.system.DexFile@425e31c0"],nativeLibraryDirectories=[/data/loc
al/tmp, /data/app-lib/com.example.stormhookdemo-1, /vendor/lib, /system/lib]]]
D/storm   (17772): [+]find original method (getMacAddress_hook)
D/storm   (17772): [+]find original method (test_public_hook)
D/storm   (17772): [+]find original method (currentTimeMillis)
D/storm   (17772): [+]find original method (test_private)
D/storm   (17772): [+]find original method (test_privatestatic)
D/storm   (17772): Inject dex hook success
I/storm   (17772): *-*-*-*-*-*-*- End -*-*-*-*-*-*-*-*-*-*

```

测试button事件的log输出
```C
adb logcat -s "storm","hook"
D/hook    (19001): getMacAddress is hooked :)
D/storm   (19001): Wifi mac :c4:43:8f:f7:d1:03
D/hook    (19001): test_public is hooked
D/storm   (19001): test_public is called
D/hook    (19001): test_private is hooked
D/storm   (19001): test_private is called
D/storm   (19001): test_private return:10
D/hook    (19001): currentTimeMillis is much better in seconds :)
D/storm   (19001): small Currentime:5885303
D/hook    (19001): test_private is hooked
D/storm   (19001): test_private is called
D/storm   (19001): test_private return:10
D/hook    (19001): test_privatestatic is hooked
D/storm   (19001): test_privatestatic is called


```













# 原理

早期的Dalvik hook是修改Java方法的签名属性为native，replacemethod是native方法，需要通过java反射来调用原始函数，这种方式来hook代码非常不方便。
在StormHook框架，我是将replace method全部用Java代码实现。

首先注入so来进入目标进程的native世界,然后使用LoadDex来加载Dex
```C
jclass DexFile=jenv->FindClass("dalvik/system/DexFile");
if(ClearException(jenv))
{
	ALOG("storm","find DexFile class failed");
	return 0;
}
jmethodID loadDex=jenv->GetStaticMethodID(DexFile,"loadDex","(Ljava/lang/String;Ljava/lang/String;I)Ldalvik/system/DexFile;");
```
然后根据MultiDex原理将动态加载的dex与原始Dex合并，执行外部加载Dex的入口类，这样我们就进入进程的Java世界
来进行Java Hook操作。

如何找到动态加载的Dex中的java类，有2种方法：

方法一：使用PathClassLoader.loadClass(className)；
主Dex对应的是pathClassLoader
由于我们将外部Dex和当前Dex进行MultiDex操作，那么这2个Dex的类都可以通过pathClassLoader来找到外部dex目标类
```C
jstring className=jenv->NewStringUTF(name);
jclass clazzCL = jenv->GetObjectClass(g_classLoader);
jmethodID loadClass = jenv->GetMethodID(clazzCL,"loadClass","(Ljava/lang/String;)Ljava/lang/Class;");
jclass tClazz = (jclass)jenv->CallObjectMethod(g_classLoader,loadClass,className);
```

方法二：dexFile.loadClass(className);
通过LoadDex加载外部Dex之后，会得到一个dex对象dexObj，也可以使用dexObj.loadClass来找到外部dex目标类
```C
jclass DexFile=jenv->FindClass("dalvik/system/DexFile");
jmethodID loadClass=jenv->GetMethodID(DexFile,"loadClass","(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/Class;");
if(ClearException(jenv))
{
	ALOG("storm","find loadClass methodId failed");
	return 0;
}
//important dexObject.loadClass()
jstring className=jenv->NewStringUTF(name);
jclass tClazz = (jclass)jenv->CallObjectMethod(dexObject,loadClass,className,g_classLoader);
```




# 参考
* https://github.com/alibaba/AndFix
* https://github.com/mar-v-in/ArtHook
* [Android热修复升级探索——追寻极致的代码热替换](https://yq.aliyun.com/articles/74598)
* [Android 7.0 行为变更](https://developer.android.com/about/versions/nougat/android-7.0-changes.html?hl=zh-cn#ndk)















