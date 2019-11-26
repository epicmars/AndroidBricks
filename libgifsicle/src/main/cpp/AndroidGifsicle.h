//
// Created on 2019-11-08.
//
#include <jni.h>

#ifndef ANDROIDBRICKS_ANDROIDGIFSICLE_H
#define ANDROIDBRICKS_ANDROIDGIFSICLE_H


#ifdef __cplusplus
extern "C" {
#endif

#define TAG "Gifsicle"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, TAG,__VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

void init();

void explode(const char *workspace);

JNIEXPORT void JNICALL Java_com_androidpi_bricks_libgifsicle_Gifsicle_gifInfo(JNIEnv *env, jobject this, jstring gif);

JNIEXPORT void JNICALL Java_com_androidpi_bricks_libgifsicle_Gifsicle_gifImages(JNIEnv *env, jobject this, jstring gif, jstring workspace);

#ifdef __cplusplus
}
#endif

#endif //ANDROIDBRICKS_ANDROIDGIFSICLE_H
