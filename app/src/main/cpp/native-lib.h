#ifndef FAST_DOWNWARD_PLANNER_NATIVE_LIB_H
#define FAST_DOWNWARD_PLANNER_NATIVE_LIB_H

#include <jni.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_softbankrobotics_fastdownwardplanner_PythonKt_initCPython(
        JNIEnv *env, jclass /*clazz*/, jstring path);

extern "C"
JNIEXPORT jstring JNICALL
Java_com_softbankrobotics_fastdownwardplanner_FastDownwardKt_translatePDDLToSAS(
        JNIEnv *env, jclass /*clazz*/, jstring j_domain, jstring j_problem);

extern "C"
JNIEXPORT jstring JNICALL
Java_com_softbankrobotics_fastdownwardplanner_FastDownwardKt_searchPlanFromSAS(
        JNIEnv *env, jclass /*clazz*/, jstring sas_problem, jstring search_strategy);

extern "C"
JNIEXPORT jstring JNICALL
Java_com_softbankrobotics_fastdownwardplanner_FastDownwardKt_searchPlan(
        JNIEnv *env, jclass clazz, jstring domain, jstring problem, jstring strategy);

#endif //FAST_DOWNWARD_PLANNER_NATIVE_LIB_H
