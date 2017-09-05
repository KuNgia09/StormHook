#include "jni.h"
#include "android_runtime/AndroidRuntime.h"
#include "android/log.h"
#include "stdio.h"
#include "stdlib.h"
#include "MethodHooker.h"
//#include <utils/CallStack.h>
//#include "art.h"
#define log(a,b) __android_log_write(ANDROID_LOG_INFO,a,b); // LOG类型:info
#define log_(b) __android_log_write(ANDROID_LOG_INFO,"storm",b); // LOG类型:info


extern "C" void InjectInterface(char*arg) __attribute__((constructor));

extern "C" void InjectInterface(char*arg)
{
	log_("*-*-*-*-*-* Injected so *-*-*-*-*-*-*-*");
	Hook();
	log_("*-*-*-*-*-*-*- End -*-*-*-*-*-*-*-*-*-*");

}

