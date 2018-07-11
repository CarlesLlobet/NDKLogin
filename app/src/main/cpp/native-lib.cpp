#include <jni.h>
#include <string.h>
#include <android/log.h>
#include <cstring>

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_cllobet_ndklogin_UI_LoginActivity_comparePass(JNIEnv *env, jobject instance, jstring pass1, jstring pass2) {
    const char *nativePass1 = (env)->GetStringUTFChars(pass1, 0);
    const char *nativePass2 = (env)->GetStringUTFChars(pass2, 0);
    __android_log_write(ANDROID_LOG_ERROR, "NDK Pass 1: ", nativePass1);
    __android_log_write(ANDROID_LOG_ERROR, "NDK Pass 2: ", nativePass2);

    jboolean res = false;
    if (strcmp(nativePass1,nativePass2) == 0){
        __android_log_write(ANDROID_LOG_ERROR, "NDK Compare: ", "Estoy dentro");
        res = true;
    }

    (env)->ReleaseStringUTFChars(pass1, nativePass1);
    (env)->ReleaseStringUTFChars(pass2, nativePass2);
    return res;
}