#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring

JNICALL
Java_com_example_cllobet_ndklogin_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_cllobet_ndklogin_Domain_MainActivity_stringFromJNI(JNIEnv *env, jobject instance) {

    // TODO: Ferla util
    std::string hello = "Hello from C++";

    return env->NewStringUTF(hello.c_str());
}