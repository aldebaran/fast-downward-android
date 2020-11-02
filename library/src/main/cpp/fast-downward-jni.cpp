#include "fast-downward-jni.h"
#include <streambuf>
#include <string>
#include <jni.h>
#include <Python.h>
#include <planner.h>
#include "disposable.hpp"
#include "python-jni.h"

extern "C"
JNIEXPORT jstring JNICALL
Java_com_softbankrobotics_fastdownward_FastDownwardKt_translatePDDLToSAS(
        JNIEnv *env, jclass /*clazz*/, jstring j_domain, jstring j_problem) {

    // Convert the passed string arguments to C strings.
    const char *c_domain = env->GetStringUTFChars(j_domain, nullptr);
    const char *c_problem = env->GetStringUTFChars(j_problem, nullptr);

    // Make sure we drop a reference to the passed arguments after they are used.
    auto string_args_references = make_disposable([&] {
        env->ReleaseStringUTFChars(j_domain, c_domain);
        env->ReleaseStringUTFChars(j_problem, c_problem);
    });

    // Finding the translation function written in Python.
    PyObject *translate_module = PyImport_ImportModule("translate");
    MANAGE_RESULT_OR_THROW(env, translate_module);
    PyObject *translate_function = PyObject_GetAttrString(translate_module,
                                                          "translate_from_strings");
    MANAGE_RESULT_OR_THROW(env, translate_function);

    // Preparing arguments by converting them to Python objects.
    PyObject *translate_args = PyTuple_New(2);
    MANAGE_RESULT_OR_THROW(env, translate_args);

    PyObject *py_domain = PyUnicode_DecodeLocale(c_domain, "surrogateescape");
    CONFIRM_RESULT_OR_THROW(env, py_domain);
    PyTuple_SetItem(translate_args, 0, py_domain);

    PyObject *py_problem = PyUnicode_DecodeLocale(c_problem, "surrogateescape");
    CONFIRM_RESULT_OR_THROW(env, py_problem);
    PyTuple_SetItem(translate_args, 1, py_problem);

    // Performing the translation.
    PyObject *py_sas = PyObject_Call(translate_function, translate_args, nullptr);
    MANAGE_RESULT_OR_THROW(env, py_sas);

    // Copying the result, JNI handles the memory.
    Py_ssize_t size = 0;
    char *c_sas = PyUnicode_AsUTF8AndSize(py_sas, &size);
    CONFIRM_RESULT_OR_THROW(env, c_sas);
    char *c_sas_copy = new char[size + 1]; // +1 to accommodate for the null terminator
    strcpy(c_sas_copy, c_sas);
    return env->NewStringUTF(c_sas_copy);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_softbankrobotics_fastdownward_FastDownwardKt_searchPlanFromSAS(
        JNIEnv *env, jclass /*clazz*/, jstring sas_problem, jstring search_strategy) {

    // Convert the passed string arguments to C strings.
    const char *sas = env->GetStringUTFChars(sas_problem, nullptr);
    const char *strategy = env->GetStringUTFChars(search_strategy, nullptr);

    // Make sure we drop a reference to the passed arguments after they are used.
    auto string_args_references = make_disposable([&] {
        env->ReleaseStringUTFChars(sas_problem, sas);
        env->ReleaseStringUTFChars(search_strategy, strategy);
    });

    // Perform the planning.
    try {
        std::string plan = plan_from_sas(sas, strategy);
        return env->NewStringUTF(plan.c_str());
    } catch (const std::exception &e) {
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"), e.what());
        return nullptr;
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_softbankrobotics_fastdownward_FastDownwardKt_searchPlanFastDownward(
        JNIEnv *env, jclass clazz, jstring domain, jstring problem, jstring strategy) {
    // TODO: can be optimized by avoiding conversion between C++ and Java.
    auto sas = Java_com_softbankrobotics_fastdownward_FastDownwardKt_translatePDDLToSAS(
            env, clazz, domain, problem);
    if (!sas) {
        // A JNI error should already be propagating.
        return nullptr;
    }
    return Java_com_softbankrobotics_fastdownward_FastDownwardKt_searchPlanFromSAS(
            env, clazz, sas, strategy);
}
