//
// Created by cbn on 15/11/14.
//
#include "cbn_cn_ndkexample_MainActivity.h"

JNIEXPORT jstring JNICALL Java_cbn_cn_ndkexample_MainActivity_getStringFromNative(JNIEnv * env, jobject obj){
    return (*env)->NewStringUTF(env,"I'm comes from to Native Function!");
}

