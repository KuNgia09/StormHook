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
将InjectSo模块编译的so libhook.so放入到 /data/local/tmp/目录

Step2：
HookCore是Android Studio工程，将生成的Apk中的classes.dex提取出来 
重命名为hook.dex,放入到 /data/local/tmp/目录
```C
root@hammerhead:/data/local/tmp # ll
-rwxrwxr-x shell    shell      523480 2015-04-13 10:35 android_server
-rw-rw-rw- shell    shell       79872 2017-09-15 08:26 hook.dex
-rwxrwxr-x shell    shell       13652 2017-08-18 19:39 inject
-rw-rw-rw- shell    shell       17532 2017-09-15 08:01 libarthook_native.so
-rw-rw-rw- shell    shell       66828 2017-09-15 08:01 libdalvikhook_native.so
-rw-rw-rw- shell    shell       25796 2017-09-15 08:00 libhook.so
root@hammerhead:/data/local/tmp # getenforce
Permissive
```

Step3:
将HookCore/Native是jni工程
将编译生成的so libdalvikhook_native.so和libarthook_native.so放入到/data/local/tmp/目录

Step4:
关闭selinux

Step5:
注入Step1中的so到StormHookSample App中
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

测试button显示的效果
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

## 1.如何进入目标进程的native世界
通过注入InjectSo模块中的libhook.so到目标进程，就进入了目标的native世界

## 2.如何进入目标进程的java世界
使用LoadDex函数加载外部Dex，并执行指定的入口类，对Java函数进行hook操作，这样就进入到目标进程的Java世界

## 3.如何获取全局的JavaVm
在JNI开发当中，JavaVM参数可以通过JNI_OnLoad参数获取，但是对于我们注入的so ，我们无法通过这种方式获取JavaVm，但是Android提供了另外一种方法可以获取到全局的JavaVm
```C
android::AndroidRuntime::getJavaVM();
```


## 4.加载外部Dex
使用反射的方法调用"dalvik/system/DexFile"类中的loadDex来动态加载Dex，获取一个dex对象
```C
jclass DexFile=jenv->FindClass("dalvik/system/DexFile");
if(ClearException(jenv))
{
	ALOG("storm","find DexFile class failed");
	return 0;
}
jmethodID loadDex=jenv->GetStaticMethodID(DexFile,"loadDex","(Ljava/lang/String;Ljava/lang/String;I)Ldalvik/system/DexFile;");
```

## 5.获取主dex所对应的PathClassLoader

## 6.根据MultiDex原理将主Dex和外部加载的Dex合并

## 7.找到外部Dex的入口类
这里我提供了2种方法：

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

## 8.执行外部Dex入口类进行Java Hook操作
### Dalvik Hook
采用的方法类似AndFix，将origin method对应的DalvikMethod结构替换为replace method的DalvikMethod结构

### Art Hook
采用的是mar-v-hook的Art Hook方案

# 参考
* https://github.com/alibaba/AndFix
* https://github.com/mar-v-in/ArtHook
* [Android热修复升级探索——追寻极致的代码热替换](https://yq.aliyun.com/articles/74598)
* [Android 7.0 行为变更](https://developer.android.com/about/versions/nougat/android-7.0-changes.html?hl=zh-cn#ndk)















