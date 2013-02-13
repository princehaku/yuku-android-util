LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_CFLAGS += -O3 -fno-strict-aliasing

LOCAL_MODULE    := snappy
LOCAL_SRC_FILES := \
	map.c \
	scmd.c \
	util.c \
	snappy.c \
	yuku_snappy_codec_SnappyImplNative.cpp

# for logging
LOCAL_LDLIBS    += -llog

include $(BUILD_SHARED_LIBRARY)
