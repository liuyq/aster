#
# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
#	javacv:../lib/javacv.jar \
#	javacpp:../lib/javacpp.jar \
#	javacv-linux-x86:../lib/javacv-linux-x86.jar
#include $(BUILD_MULTI_PREBUILT)
#include $(CLEAR_VARS)

# Build Owl
LOCAL_MODULE := owl
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-java-files-under, org/zeroxlab/owl) $(call all-java-files-under, com)
LOCAL_JAR_MANIFEST := ../etc/owl/manifest.txt
#LOCAL_STATIC_JAVA_LIBRARIES := \
#	javacv \
#	javacpp \
#	javacv-linux-x86
include $(BUILD_HOST_JAVA_LIBRARY)
include $(CLEAR_VARS)

# Build WookieeRunner
LOCAL_MODULE := wookieerunner
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-java-files-under, org/zeroxlab/wookieerunner)
LOCAL_JAR_MANIFEST := ../etc/wookieerunner/manifest.txt
LOCAL_STATIC_JAVA_LIBRARIES := owl
LOCAL_JAVA_LIBRARIES := \
	chimpchat \
	monkeyrunner \
	jython \
	sdklib \
	ddmlib \
	guavalib
#LOCAL_JAVA_RESOURCE_DIRS := resources
include $(BUILD_HOST_JAVA_LIBRARY)
include $(CLEAR_VARS)
