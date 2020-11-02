//
// Created by victor.paleologue on 02/11/20.
//
#include <jni.h>

#ifndef FAST_DOWNWARD_ANDROID_PYTHON_JNI_H

extern "C"
JNIEXPORT jstring JNICALL
Java_com_softbankrobotics_python_PythonKt_initCPython(
        JNIEnv *env, jclass /*clazz*/, jstring path);

/**
 * Retrieves the current Python exception and translates it in a JNI exception.
 * In JNI, the exception is not directly thrown from C++.
 * Instead, the C++ should return a null result after having the JNI exception is set.
 * @param jni
 */
JNIEXPORT void forward_python_exception_to_jni(JNIEnv *jni);

/** Make sure we dereference some newly produced reference of a Python result. */
#define MANAGE_RESULT(result) \
    auto _ ## result ## _ref = make_disposable([=]{ Py_DecRef(result); })

/** Check Python result and forward Python exception if absent (we assume there is always). */
#define CONFIRM_RESULT_OR_THROW(jni, result) \
    if (!result) { forward_python_exception_to_jni(jni); return nullptr; }

/** Combo: check Python result or throw, and manage the reference of a Python result. */
#define MANAGE_RESULT_OR_THROW(jni, result) \
    CONFIRM_RESULT_OR_THROW(jni, result); \
    MANAGE_RESULT(result)

#define FAST_DOWNWARD_ANDROID_PYTHON_JNI_H

#endif //FAST_DOWNWARD_ANDROID_PYTHON_JNI_H
