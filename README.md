# StormHook
## StormHook是一款Android侵入式Java Hook框架

* 支持Android Art和Dalvik
* 支持Andorid 4.0-6.0
* 支持注入到其他进程Hook

# 模块：
* StormHookSample：用来测试Hook效果的例子
* InjectSo：用来注入到StormHookSample进程的so文件
* HookCore:  加载到StormHookSample进程的dex

# 原理：

## 如何进入目标进程的native世界
通过注入InjectSo模块中的libhook.so到目标进程，就进入了目标的native世界

## 如何进入目标进程的java世界
使用LoadDex函数加载外部Dex，并执行指令的入口类，对Java函数进行hook操作

## 如何获取全局的JavaVm
在JNI开发当中，JavaVM参数可以通过JNI_OnLoad参数获取，但是对于我们注入的so ，我们无法通过这种方式获取JavaVm，但是Android提供了另外一种方法可以获取到全局的JavaVm


## 加载外部Dex
使用反射的方法调用"dalvik/system/DexFile"类中的loadDex来动态加载Dex，获取一个dex对象


## 获取当前dex所对应的PathClassLoader，表示为g_classLoader

## 使用MultiDex将主Dex和外部加载的Dex合并
执行这一步之后，g_classLoader可以找到外部加载Dex的类


## 找到外部Dex的入口类
这里我提供了2种方法：
方法一：使用PathClassLoader.loadClass(className)；
主Dex对应的是pathClassLoader
由于我们将外部Dex和当前Dex进行MultiDex操作，那么这2个Dex的类都可以通过pathClassLoader来找到外部dex目标类

方法二：dexFile.loadClass(className);
通过LoadDex加载外部Dex之后，会得到一个dex对象dexObj，也可以使用dexObj.loadClass来找到外部dex目标类


## 执行外部Dex入口类进行Java Hook操作
### Dalvik Hook
在Dalvik的Hook当中，我使用的方法类似AndFix，将origin method对应的DalvikMethod结构替换为replace method的DalvikMethod结构

### Art Hook
使用mar-v-hook的Art Hook方案

# 感谢
* https://github.com/alibaba/AndFix
* https://github.com/mar-v-in/ArtHook
* https://yq.aliyun.com/articles/74598















