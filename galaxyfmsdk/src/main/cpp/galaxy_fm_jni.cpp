#include <jni.h>
#include <string>
#include "v4l2_fm_tuner.h"
#include <android/log.h>

#define LOG_TAG "GalaxyFM-JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

static V4l2FmTuner gTuner;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_samsung_galaxyfmsdk_jni_GalaxyFmJniBridge_nativeOpen(JNIEnv *env, jobject thiz, jstring device_path) {
    const char *path = env->GetStringUTFChars(device_path, nullptr);
    bool result = gTuner.openDevice(path ? path : "/dev/radio0");
    if (path) {
        env->ReleaseStringUTFChars(device_path, path);
    }
    return result ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_samsung_galaxyfmsdk_jni_GalaxyFmJniBridge_nativeClose(JNIEnv *env, jobject thiz) {
    gTuner.closeDevice();
}

JNIEXPORT jboolean JNICALL
Java_com_samsung_galaxyfmsdk_jni_GalaxyFmJniBridge_nativePowerUp(JNIEnv *env, jobject thiz, jfloat freq_mhz) {
    return gTuner.powerUp(freq_mhz) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_samsung_galaxyfmsdk_jni_GalaxyFmJniBridge_nativePowerDown(JNIEnv *env, jobject thiz) {
    return gTuner.powerDown() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_samsung_galaxyfmsdk_jni_GalaxyFmJniBridge_nativeSetFrequency(JNIEnv *env, jobject thiz, jfloat freq_mhz) {
    return gTuner.setFrequency(freq_mhz) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jfloat JNICALL
Java_com_samsung_galaxyfmsdk_jni_GalaxyFmJniBridge_nativeGetFrequency(JNIEnv *env, jobject thiz) {
    return gTuner.getFrequency();
}

JNIEXPORT jboolean JNICALL
Java_com_samsung_galaxyfmsdk_jni_GalaxyFmJniBridge_nativeSeek(JNIEnv *env, jobject thiz, jboolean seek_up, jboolean wrap_around) {
    return gTuner.seek(seek_up == JNI_TRUE, wrap_around == JNI_TRUE) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jint JNICALL
Java_com_samsung_galaxyfmsdk_jni_GalaxyFmJniBridge_nativeGetRssi(JNIEnv *env, jobject thiz) {
    return gTuner.getRssi();
}

JNIEXPORT jboolean JNICALL
Java_com_samsung_galaxyfmsdk_jni_GalaxyFmJniBridge_nativeIsStereo(JNIEnv *env, jobject thiz) {
    return gTuner.isStereo() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jobject JNICALL
Java_com_samsung_galaxyfmsdk_jni_GalaxyFmJniBridge_nativeGetRdsData(JNIEnv *env, jobject thiz) {
    FmRdsNativeData data = gTuner.getRdsData();
    jclass clazz = env->FindClass("com/samsung/galaxyfmsdk/model/FmRdsData");
    if (!clazz) return nullptr;

    jmethodID constructor = env->GetMethodID(clazz, "<init>", "(Ljava/lang/String;Ljava/lang/String;IIZZ)V");
    if (!constructor) return nullptr;

    jstring jPs = env->NewStringUTF(data.psName.c_str());
    jstring jRt = env->NewStringUTF(data.radioText.c_str());

    jobject result = env->NewObject(clazz, constructor, jPs, jRt, data.pty, data.piCode, data.tp ? JNI_TRUE : JNI_FALSE, data.ta ? JNI_TRUE : JNI_FALSE);

    env->DeleteLocalRef(jPs);
    env->DeleteLocalRef(jRt);
    return result;
}

JNIEXPORT jboolean JNICALL
Java_com_samsung_galaxyfmsdk_jni_GalaxyFmJniBridge_nativeSetMute(JNIEnv *env, jobject thiz, jboolean mute) {
    return gTuner.setMute(mute == JNI_TRUE) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_samsung_galaxyfmsdk_jni_GalaxyFmJniBridge_nativeSetBand(JNIEnv *env, jobject thiz, jint band) {
    return gTuner.setBand(band) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_samsung_galaxyfmsdk_jni_GalaxyFmJniBridge_nativeSetDeEmphasis(JNIEnv *env, jobject thiz, jint emphasis) {
    return gTuner.setDeEmphasis(emphasis) ? JNI_TRUE : JNI_FALSE;
}

} // extern "C"
