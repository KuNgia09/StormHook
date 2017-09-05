#include <jni.h>
#include <string.h>
#include <android/log.h>
#include "dalvik_vm.h"
//
// Created by qiulinmin on 7/7/17.
//
static const char* kClassMethodHookChar = "com/example/DalvikHook/MethodHook";

static struct {
    jmethodID m1;
    jmethodID m2;
    size_t methodSize;
} methodHookClassInfo;

#define ALOG(...) __android_log_print(ANDROID_LOG_DEBUG, __VA_ARGS__)


const char* origin_name;
const char* replace_name;

static long methodHook(JNIEnv* env, jclass type, jobject srcMethodObj, jobject destMethodObj) {
	Method * srcMethod = reinterpret_cast<Method *>(env -> FromReflectedMethod(srcMethodObj));
	Method * destMethod = reinterpret_cast<Method *>(env -> FromReflectedMethod(destMethodObj));
    int* backupMethod = new int[methodHookClassInfo.methodSize];

    memcpy(backupMethod, srcMethod, methodHookClassInfo.methodSize);
/*
    if(IS_METHOD_FLAG_SET(srcMethod, ACC_STATIC) || IS_METHOD_FLAG_SET(srcMethod, ACC_PRIVATE) ||*(srcMethod->name) == '<'){
    	ALOG("storm","targetMethod is private | static | <init>");
    	memcpy(srcMethod, destMethod, methodHookClassInfo.methodSize);
    	srcMethod->name=origin_name;
    }
    else{
    	memcpy(srcMethod, destMethod, methodHookClassInfo.methodSize);
    }
*/


    memcpy(srcMethod, destMethod, methodHookClassInfo.methodSize);
    return reinterpret_cast<long>(backupMethod);
}

static jobject methodRestore(JNIEnv* env, jclass type, jobject srcMethod, jlong methodPtr) {
    int* backupMethod = reinterpret_cast<int*>(methodPtr);
    Method * artMethodSrc = reinterpret_cast<Method *>(env -> FromReflectedMethod(srcMethod));
    memcpy((void*)artMethodSrc, backupMethod, methodHookClassInfo.methodSize);

    //artMethodSrc->clazz->directMethods=artMethodSrc;
    delete []backupMethod;
    return srcMethod;
}

static JNINativeMethod gMethods[] = {
        {
                "hook_native",
                "(Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;)J",
                (void*)methodHook
        },
        {
                "restore_native",
                "(Ljava/lang/reflect/Method;J)Ljava/lang/reflect/Method;",
                (void*)methodRestore
        }
};

extern "C"
JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv *env = nullptr;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return JNI_FALSE;
    }
    jclass classEvaluateUtil = env->FindClass(kClassMethodHookChar);
    if(env -> RegisterNatives(classEvaluateUtil, gMethods, sizeof(gMethods)/ sizeof(gMethods[0])) < 0) {
        ALOG("dalvik_hook","RegisterNatives failed");
        return JNI_FALSE;
    }
    ALOG("dalvik_hook","RegisterNatives successful");
    methodHookClassInfo.m1 = env -> GetStaticMethodID(classEvaluateUtil, "m1", "()V");
    methodHookClassInfo.m2 = env -> GetStaticMethodID(classEvaluateUtil, "m2", "()V");
    methodHookClassInfo.methodSize = reinterpret_cast<size_t>(methodHookClassInfo.m2) - reinterpret_cast<size_t>(methodHookClassInfo.m1);
    return JNI_VERSION_1_4;
}



