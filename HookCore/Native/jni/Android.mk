

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= method-hook-lib.cpp
LOCAL_CFLAGS	:= -std=gnu++11 -fpermissive
LOCAL_SHARED_LIBRARIES :=
LOCAL_LDLIBS    := -llog
LOCAL_STATIC_LIBRARIES :=
LOCAL_MODULE:= dalvikhook_native

include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE:= arthook_native
LOCAL_SRC_FILES := hook.c
LOCAL_LDLIBS+=-llog
LOCAL_SHARED_LIBRARIES :=
LOCAL_CFLAGS :=-DANDROID_NDK -std=c99
include $(BUILD_SHARED_LIBRARY)



