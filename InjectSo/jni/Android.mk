LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE:= hook

LOCAL_SRC_FILES := so.cpp MethodHooker.cpp


LOCAL_CFLAGS    := -w -DHAVE_LITTLE_ENDIAN
#LOCAL_CFLAGS    := -I./include/ -I./dalvik/vm/ -I./dalvik -DHAVE_LITTLE_ENDIAN
LOCAL_C_INCLUDES +=$(LOCAL_PATH)/include/  
LOCAL_C_INCLUDES +=$(LOCAL_PATH)/dalvik/vm/ 
LOCAL_C_INCLUDES +=$(LOCAL_PATH)/dalvik/

#LOCAL_LDFLAGS	:=	-L./lib/  -L$(SYSROOT)/usr/lib -llog -ldvm -landroid_runtime  -lart
LOCAL_LDLIBS	:= -L./lib/  -L$(SYSROOT)/usr/lib -llog  -landroid_runtime 

LOCAL_STATIC_LIBRARIES := hookart

LOCAL_SHARED_LIBRARIES :=
include $(BUILD_SHARED_LIBRARY)

#------------------------------------------------------------------------


