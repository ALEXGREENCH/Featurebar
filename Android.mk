LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := sprd-support-featurebar

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

# Add denpency for PhoneWindow override resource.
LOCAL_JAVA_LIBRARIES := android.policy

include $(BUILD_STATIC_JAVA_LIBRARY)
