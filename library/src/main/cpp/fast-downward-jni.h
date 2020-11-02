#ifndef PDDL_SANDBOX_FAST_DOWNWARD_JNI_H
#define PDDL_SANDBOX_FAST_DOWNWARD_JNI_H

#include <jni.h>

extern "C"
JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* javaVm, void* reserved);

extern "C"
JNIEXPORT jstring JNICALL
Java_com_softbankrobotics_fastdownward_FastDownwardKt_translatePDDLToSAS(
        JNIEnv *env, jclass /*clazz*/, jstring j_domain, jstring j_problem);

extern "C"
JNIEXPORT jstring JNICALL
Java_com_softbankrobotics_fastdownward_FastDownwardKt_searchPlanFromSAS(
        JNIEnv *env, jclass /*clazz*/, jstring sas_problem, jstring search_strategy);

extern "C"
JNIEXPORT jstring JNICALL
Java_com_softbankrobotics_fastdownward_FastDownwardKt_searchPlanFastDownward(
        JNIEnv *env, jclass clazz, jstring domain, jstring problem, jstring strategy);

#endif //PDDL_SANDBOX_FAST_DOWNWARD_JNI_H
