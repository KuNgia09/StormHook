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















