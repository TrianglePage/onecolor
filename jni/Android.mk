LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_INSTALL_MODULES:=on
OPENCV_CAMERA_MODULES:=off
OPENCV_LIB_TYPE:=STATIC
include D:\C_Code\Android\OpenCV-android-sdk\sdk\native\jni\OpenCV.mk
LOCAL_LDLIBS    += -lm -llog -landroid

LOCAL_MODULE    := img_processor
LOCAL_SRC_FILES := img_processor.cpp

include $(BUILD_SHARED_LIBRARY)