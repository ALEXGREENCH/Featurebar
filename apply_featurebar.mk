
LOCAL_STATIC_JAVA_LIBRARIES += sprd-support-featurebar

LOCAL_AAPT_FLAGS += --auto-add-overlay --extra-packages com.sprd.android.support.featurebar

ifndef SPRD_SUPPORT_DIR
SPRD_SUPPORT_DIR := vendor/sprd/platform/frameworks/support
endif

featurebar_res := $(SPRD_SUPPORT_DIR)/featurebar/res

ifdef LOCAL_RESOURCE_DIR

LOCAL_RESOURCE_DIR += $(featurebar_res)

else

LOCAL_RESOURCE_DIR := $(wildcard $(LOCAL_PATH)/res) $(featurebar_res)

endif # LOCAL_RESOURCE_DIR
