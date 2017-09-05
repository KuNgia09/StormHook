#include "jni.h"
#include "android_runtime/AndroidRuntime.h"
#include "android/log.h"
#include "stdio.h"
#include "stdlib.h"
#include <dlfcn.h>
#define ANDROID_SMP 0
#include "Dalvik.h"
#include "alloc/Alloc.h"
#include <sys/system_properties.h>

//#include "art.h"

#define ALOG(...) __android_log_print(ANDROID_LOG_VERBOSE, __VA_ARGS__)

static bool g_bAttatedT;
static JavaVM *g_JavaVM;
static bool g_status=false;
static jobject g_classLoader=0;
static jobject g_dexObject;
static jobject g_orignalDex;
static int g_sdkVersion;




void init()
{
	char android_sdkversion[0x10] = {0};
	__system_property_get("ro.build.version.sdk",android_sdkversion);
	int g_sdkVersion=atoi(android_sdkversion);
	//  Android N or Later cannot get JavaVM
	if(g_sdkVersion>23){
		ALOG("storm","[-]Not support current Android sdkVersion:%d",g_sdkVersion);
		return ;
	}

	g_bAttatedT = false;
	g_JavaVM = android::AndroidRuntime::getJavaVM();
	
	ALOG("storm","g_JavaVM:%x",g_JavaVM);
}

static JNIEnv *GetEnv()
{
	int status;
	JNIEnv *envnow = NULL;
	status = g_JavaVM->GetEnv((void **)&envnow, JNI_VERSION_1_4);
	if(status < 0)
	{
		status = g_JavaVM->AttachCurrentThread(&envnow, NULL);
		if(status < 0)
		{
			return NULL;
		}
		g_bAttatedT = true;
	}
	return envnow;
}

static void DetachCurrent()
{
	if(g_bAttatedT)
	{
		g_JavaVM->DetachCurrentThread();
	}
}



int ClearException(JNIEnv *jenv){
	jthrowable exception = jenv->ExceptionOccurred();
	if (exception != NULL) {
		jenv->ExceptionDescribe();
		jenv->ExceptionClear();
		return true;
	}
	return false;
}



int makeDexElements(JNIEnv* env, jobject classLoader, jobject dexFileobj)
{
	jclass PathClassLoader = env->GetObjectClass(classLoader);

	jclass BaseDexClassLoader = env->GetSuperclass(PathClassLoader);

	//get pathList fieldid
	jfieldID pathListid = env->GetFieldID(BaseDexClassLoader, "pathList", "Ldalvik/system/DexPathList;");
	jobject pathList = env->GetObjectField(classLoader, pathListid);

	//get DexPathList Class 
	jclass DexPathListClass = env->GetObjectClass(pathList);
	//get dexElements fieldid
	jfieldID dexElementsid = env->GetFieldID(DexPathListClass, "dexElements", "[Ldalvik/system/DexPathList$Element;");

	//获取elements数组 get dexElement array value
	jobjectArray dexElement = static_cast<jobjectArray>(env->GetObjectField(pathList, dexElementsid));

	


	//获取数组的个数 get DexPathList$Element Class construction method and get a new DexPathList$Element object 
	jint len = env->GetArrayLength(dexElement);
	ALOG("storm", "original Element size:%d", len);
	


	jclass ElementClass = env->FindClass("dalvik/system/DexPathList$Element");// dalvik/system/DexPathList$Element
	jmethodID Elementinit = env->GetMethodID(ElementClass, "<init>", "(Ljava/io/File;ZLjava/io/File;Ldalvik/system/DexFile;)V");
	jboolean isDirectory = JNI_FALSE;

	/**
     * get origianl dex object
     */
	jobject originalDexElement=env->GetObjectArrayElement(dexElement,0);
	if(originalDexElement!=0)
	{
		jfieldID tmp=env->GetFieldID(ElementClass,"dexFile","Ldalvik/system/DexFile;");
		g_orignalDex=env->GetObjectField(originalDexElement,tmp);
		if(ClearException(env)){
			ALOG("storm","get original DexObj faield");
		}
	}
    
	//创建一个新的dalvik/system/DexPathList$Element类 dexFileobj为新的dexFileobj
	jobject element_obj = env->NewObject(ElementClass, Elementinit, 0, isDirectory, 0, dexFileobj);

	//Get dexElement all values and add  add each value to the new array
	jobjectArray new_dexElement = env->NewObjectArray(len + 1, ElementClass, 0);
	for (int i = 0; i < len; ++i)
	{
		//将以前的Elements添加到这个新的new_dexElement数组
		env->SetObjectArrayElement(new_dexElement, i, env->GetObjectArrayElement(dexElement, i));
	}
	//将要加载的element_obj放在新数组的最后一个成员里
	env->SetObjectArrayElement(new_dexElement, len, element_obj);
	env->SetObjectField(pathList, dexElementsid, new_dexElement);

	env->DeleteLocalRef(element_obj);
	env->DeleteLocalRef(ElementClass);
	env->DeleteLocalRef(dexElement);
	env->DeleteLocalRef(DexPathListClass);
	env->DeleteLocalRef(pathList);
	env->DeleteLocalRef(BaseDexClassLoader);
	env->DeleteLocalRef(PathClassLoader);
}



static int getClassLoader(JNIEnv *jenv,jobject dexObject){
	//获取Loaders
	jclass clazzApplicationLoaders = jenv->FindClass("android/app/ApplicationLoaders");
	jthrowable exception = jenv->ExceptionOccurred();
	if (ClearException(jenv)) {
		ALOG("Exception","No class : %s", "android/app/ApplicationLoaders");
		return NULL;
	}
	jfieldID fieldApplicationLoaders = jenv->GetStaticFieldID(clazzApplicationLoaders,"gApplicationLoaders","Landroid/app/ApplicationLoaders;");
	if (ClearException(jenv)) {
		ALOG("Exception","No Static Field :%s","gApplicationLoaders");
		return NULL;
	}
	jobject objApplicationLoaders = jenv->GetStaticObjectField(clazzApplicationLoaders,fieldApplicationLoaders);
	if (ClearException(jenv)) {
		ALOG("Exception","GetStaticObjectField is failed [%s","gApplicationLoaders");
		return NULL;
	}
	//

	jfieldID fieldLoaders = jenv->GetFieldID(clazzApplicationLoaders,"mLoaders","Ljava/util/Map;");
	if (ClearException(jenv)) {
		fieldLoaders = jenv->GetFieldID(clazzApplicationLoaders,"mLoaders","Landroid/util/ArrayMap;");
		if(ClearException(jenv)){
		      ALOG("Exception","No Field :%s","mLoaders");
		      return NULL;
		}

	}

	jobject objLoaders = jenv->GetObjectField(objApplicationLoaders,fieldLoaders);
	if (ClearException(jenv)) {
		ALOG("Exception","No object :%s","mLoaders");
		return NULL;
	}
	//提取map中的values
	jclass clazzHashMap = jenv->GetObjectClass(objLoaders);
	jmethodID methodValues = jenv->GetMethodID(clazzHashMap,"values","()Ljava/util/Collection;");
	jobject values = jenv->CallObjectMethod(objLoaders,methodValues);

	jclass clazzValues = jenv->GetObjectClass(values);
	jmethodID methodToArray = jenv->GetMethodID(clazzValues,"toArray","()[Ljava/lang/Object;");
	if (ClearException(jenv)) {
		ALOG("Exception","No Method:%s","toArray");
		return NULL;
	}

	jobjectArray classLoaders = (jobjectArray)jenv->CallObjectMethod(values,methodToArray);
	if (ClearException(jenv)) {
		ALOG("Exception","CallObjectMethod failed :%s","toArray");
		return NULL;
	}

	int size = jenv->GetArrayLength(classLoaders);

	//classLoaders size always is 1 ???
	ALOG("storm","classLoaders size:%d",size);

	for(int i = 0 ; i < size ; i ++){
		jobject classLoader = jenv->GetObjectArrayElement(classLoaders,i);
		
		g_classLoader=jenv->NewGlobalRef(classLoader);
		if(g_classLoader==NULL){
			ALOG("storm","classLoader NewGlobalRef failed");
			return 0;
		}
		jenv->DeleteLocalRef(classLoader);
		return 1;	
	}
	
}

static jclass loadCLass_plan_one(JNIEnv *jenv,const char *name,jobject dexObject)
{
	loadCLass_plan_one(jenv,name,dexObject);
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
	if(ClearException(jenv))
	{
		ALOG("storm","loadClass %s failed",name);
		return 0;
	}
	return tClazz;

}

//MultiDex
static jclass loadCLass_plan_two(JNIEnv *jenv,const char *name)
{
	jstring className=jenv->NewStringUTF(name);
	jclass clazzCL = jenv->GetObjectClass(g_classLoader);
	jmethodID loadClass = jenv->GetMethodID(clazzCL,"loadClass","(Ljava/lang/String;)Ljava/lang/Class;");
	jclass tClazz = (jclass)jenv->CallObjectMethod(g_classLoader,loadClass,className);

	if(ClearException(jenv))
	{
		ALOG("storm","loadClass %s failed",name);
		return 0;
	}
	return tClazz;
}

static jclass findAppClass_test(JNIEnv *jenv,const char *name,jobject dexObject)
{
	 //there hava 2 plan to loadClass
	 // plan 1
	//jclass TargetClazz=loadCLass_plan_one(jenv,name,dexObject);
	
	// plan 2
	jclass TargetClazz=loadCLass_plan_two(jenv,name);
	if(TargetClazz!=0)
		ALOG("storm","loadClass %s successful clazz:0x%x",name,TargetClazz);
	return TargetClazz;
}


jobject LoadDex(JNIEnv* jenv,const char* dexPath,const char* pKgName)
{
	jclass DexFile=jenv->FindClass("dalvik/system/DexFile");
	if(ClearException(jenv))
	{
		ALOG("storm","find DexFile class failed");
		return 0;
	}
	jmethodID loadDex=jenv->GetStaticMethodID(DexFile,"loadDex","(Ljava/lang/String;Ljava/lang/String;I)Ldalvik/system/DexFile;");
	if(ClearException(jenv))
	{
		ALOG("storm","find loadDex methodId failed");
		return 0;
	}
	//jstring inPath=jenv->NewStringUTF("/data/data/com.example.stromhooktest/legend.dex");
	jstring inPath=jenv->NewStringUTF(dexPath);

	char optPath[256]={0};
	strcat(optPath,"/data/data/");
	strcat(optPath,pKgName);
	strcat(optPath,"/hook.dat");
	ALOG("storm","LoadDex optFile path:%s",optPath);
	jstring outPath=jenv->NewStringUTF(optPath);
	jobject dexObject=jenv->CallStaticObjectMethod(DexFile,loadDex,inPath,outPath,0);
	if(ClearException(jenv))
	{
		ALOG("storm","call loadDex method failed");
		return 0;
	}
	return dexObject;
}


jclass myFindClass(JNIEnv* jenv,const char* targetClassName,jobject dexObj)
{
	//char* targetClassName="com/legend/demo/Inject";
	jclass clazzTarget = jenv->FindClass(targetClassName);
	if (ClearException(jenv)) {
		ALOG("storm","ClassMethodHook[Can't find class:%s in bootclassloader",targetClassName);
	    clazzTarget = findAppClass_test(jenv,targetClassName,dexObj);
	    if(clazzTarget == NULL){
	    	ALOG("storm","found class %s failed",targetClassName);
	    	return false;
	    }
	}
	
	return clazzTarget;
}


int Hook(){
	
	init();
	if(!g_JavaVM){
		ALOG("storm","get gJavaVM failed ");
		return 0;
	}
    JNIEnv *jenv = GetEnv();

    const char* dexPath="/data/local/tmp/hook.dex";
    const char* pkgName="com.example.stormhookdemo";
    const char* targetClass="com/storm/hook/main";
  
	g_dexObject=LoadDex(jenv,dexPath,pkgName);
	if(g_dexObject==0) return 0;
		
	
	getClassLoader(jenv,g_dexObject);
	if(g_classLoader==0) return 0;
	
	makeDexElements(jenv,g_classLoader,g_dexObject);
	

	
	/*
	jclass base=myFindClass(jenv,"java/lang/Class",g_orignalDex);
	if(base!=0){
		jmethodID forName=jenv->GetStaticMethodID(base,"forName","(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;");
		if(ClearException(jenv)){
			ALOG("storm","found forName method faild");
		}
		jstring tmp=jenv->NewStringUTF("com.example.stromhooktest.MainActivity");
		jboolean Initial=true;
		jenv->CallStaticObjectMethod(base,forName,tmp,Initial,g_classLoader);
		if(ClearException(jenv)){
			ALOG("storm","call forName com/example/stromhooktest/MainActivity faild");
		}
	}*/



	
	jclass Inject=myFindClass(jenv,targetClass,g_dexObject);

    jmethodID main=jenv->GetStaticMethodID(Inject,"Entry","(Ldalvik/system/PathClassLoader;Ljava/lang/String;Z)V");
    if(ClearException(jenv))
    {
    	ALOG("Exception","find Inject class Entry jmethodId failed");
    	return 0;
    }
    
    //inject_flag is only used for art
    jboolean inject_flag=false;
    jenv->CallStaticVoidMethod(Inject,main,g_classLoader,jenv->NewStringUTF(pkgName),inject_flag);
    if(ClearException(jenv))
    {
    	ALOG("Exception","call Entry method failed");
    	return 0;
    }	

    DetachCurrent();
	return 1;
}

