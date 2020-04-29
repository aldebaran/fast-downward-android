#include "native-lib.h"
#include <string>
#include <jni.h>
#include <Python.h>
#include <planner.h>

namespace {
    /**
     * An object which would run a function when disposed.
     * Destroying performs the disposal function.
     * Useful to ensure a function is called when leaving a scope.
     * @tparam T A function compatible with the void() signature.
     */
    template <typename T>
    struct Disposable {
        Disposable(T&& disposal_function): _do_dispose(disposal_function), _was_disposed(false) {}
        ~Disposable() { dispose(); }
        void dispose() { if (!_was_disposed) { _do_dispose(); _was_disposed = true; } }

    private:
        T _do_dispose;
        bool _was_disposed;
    };

    template <typename T>
    Disposable<T> make_disposable(T&& disposal_function) {
        return Disposable<T>(std::forward<T>(disposal_function));
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_softbankrobotics_fastdownwardplanner_PythonKt_initCPython(
        JNIEnv *env, jclass /*clazz*/, jstring j_path) {

    const char* c_path = env->GetStringUTFChars(j_path, nullptr);
    auto c_path_reference = make_disposable([&] {
        env->ReleaseStringUTFChars(j_path, c_path);
    });
    wchar_t* c_w_path = Py_DecodeLocale(c_path, nullptr);
    Py_SetPythonHome(c_w_path);
    Py_SetPath(c_w_path);
    Py_NoSiteFlag = 1;
    Py_Initialize();

    auto sys = PyImport_ImportModule("sys");
    auto sys_path = PyObject_GetAttrString(sys, "path");
    auto sys_path_str = PyObject_Repr(sys_path);
    auto sys_path_cstr = PyUnicode_AsUTF8(sys_path_str);

    return env->NewStringUTF(sys_path_cstr);
}

/** Make sure we dereference some newly produced reference of a Python result. */
#define MANAGE_RESULT(result) \
    auto _ ## result ## _ref = make_disposable([=]{ Py_DecRef(result); })

/**
 * Retrieves the current Python exception and translates it in a JNI exception.
 * In JNI, the exception is not directly thrown from C++.
 * Instead, the C++ should return a null result after having the JNI exception is set.
 * @param jni
 */
void forward_python_exception_to_jni(JNIEnv* jni) {
    std::string error_message;
    if (PyErr_Occurred()) {
        // Retrieve the exception info.
        PyObject* type = nullptr;
        PyObject* value = nullptr;
        PyObject* traceback = nullptr;
        PyErr_Fetch(&type, &value, &traceback);
        PyErr_NormalizeException(&type, &value, &traceback);
        assert(value);

        if (traceback) {
            // When the traceback is available,
            // we can use the `traceback` module to get a nice report.
            PyObject* traceback_module = PyImport_ImportModule("traceback");
            assert(traceback_module);
            MANAGE_RESULT(traceback_module);
            PyObject* format_exception = PyObject_GetAttrString(traceback_module, "format_exception");
            assert(format_exception);
            MANAGE_RESULT(format_exception);

            // Format function takes the exception info as arguments.
            PyObject* format_exception_args = PyTuple_New(3);
            MANAGE_RESULT(format_exception_args);
            PyTuple_SetItem(format_exception_args, 0, type);
            PyTuple_SetItem(format_exception_args, 1, value);
            PyTuple_SetItem(format_exception_args, 2, traceback);

            PyObject* formatted_exception = PyObject_Call(format_exception, format_exception_args, nullptr);
            MANAGE_RESULT(formatted_exception);
            const char* c_formatted_exception = PyUnicode_AsUTF8(formatted_exception);
            if (c_formatted_exception) {
                error_message = c_formatted_exception;
            }
        }

        if (error_message.empty()) {
            // When the traceback is absent or if formatting exception went wrong,
            // the report consists in showing the exception.
            PyObject* value_str = PyObject_Str(value);
            MANAGE_RESULT(value_str);
            const char* c_value_str = PyUnicode_AsUTF8(value_str);
            if (c_value_str)
                error_message = c_value_str;
        }

        if (!traceback) {
            Py_DecRef(type);
            Py_DecRef(value);
            Py_DecRef(traceback);
        }

        if (error_message.empty()) {
            error_message = "unknown Python exception";
        }
    } else {
        error_message = "attempting to handle a Python exception that is absent";
    }

    jni->ThrowNew(jni->FindClass("java/lang/RuntimeException"), error_message.c_str());
}

/** Check Python result and forward Python exception if absent (we assume there is always). */
#define CONFIRM_RESULT_OR_THROW(jni, result) \
    if (!result) { forward_python_exception_to_jni(jni); return nullptr; }

/** Combo: check Python result or throw, and manage the reference of a Python result. */
#define MANAGE_RESULT_OR_THROW(jni, result) \
    CONFIRM_RESULT_OR_THROW(jni, result); \
    MANAGE_RESULT(result)

extern "C"
JNIEXPORT jstring JNICALL
Java_com_softbankrobotics_fastdownwardplanner_PythonKt_execCPython(
        JNIEnv *env, jclass clazz, jstring j_script) {

    // Convert the passed string arguments to C strings.
    const char* c_script = env->GetStringUTFChars(j_script, 0);

    // Make sure we drop a reference to the passed arguments after they are used.
    auto string_args_references = make_disposable([&] {
        env->ReleaseStringUTFChars(j_script, c_script);
    });

    static PyObject* globals = PyDict_New();

    PyObject* code = Py_CompileString(c_script, "<jni>", Py_eval_input);
    MANAGE_RESULT_OR_THROW(env, code);

    PyObject* result = PyEval_EvalCode(code, globals, globals);
    MANAGE_RESULT_OR_THROW(env, result);

    auto result_as_py_str = PyObject_Repr(result);
    MANAGE_RESULT_OR_THROW(env, result_as_py_str);

    auto result_as_c_str = PyUnicode_AsUTF8(result_as_py_str);
    return env->NewStringUTF(result_as_c_str);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_softbankrobotics_fastdownwardplanner_FastDownwardKt_translatePDDLToSAS(
        JNIEnv *env, jclass /*clazz*/, jstring j_domain, jstring j_problem) {

    // Convert the passed string arguments to C strings.
    const char* c_domain = env->GetStringUTFChars(j_domain, nullptr);
    const char* c_problem = env->GetStringUTFChars(j_problem, nullptr);

    // Make sure we drop a reference to the passed arguments after they are used.
    auto string_args_references = make_disposable([&] {
        env->ReleaseStringUTFChars(j_domain, c_domain);
        env->ReleaseStringUTFChars(j_problem, c_problem);
    });

    // Finding the translation function written in Python.
    PyObject* translate_module = PyImport_ImportModule("translate");
    MANAGE_RESULT_OR_THROW(env, translate_module);
    PyObject* translate_function = PyObject_GetAttrString(translate_module, "translate_from_strings");
    MANAGE_RESULT_OR_THROW(env, translate_function);

    // Preparing arguments by converting them to Python objects.
    PyObject* translate_args = PyTuple_New(2);
    MANAGE_RESULT_OR_THROW(env, translate_args);

    PyObject* py_domain = PyUnicode_DecodeFSDefault(c_domain);
    CONFIRM_RESULT_OR_THROW(env, py_domain);
    PyTuple_SetItem(translate_args, 0, py_domain);

    PyObject* py_problem = PyUnicode_DecodeFSDefault(c_problem);
    CONFIRM_RESULT_OR_THROW(env, py_problem);
    PyTuple_SetItem(translate_args, 1, py_problem);

    // Performing the translation.
    PyObject* py_sas = PyObject_Call(translate_function, translate_args, nullptr);
    MANAGE_RESULT_OR_THROW(env, py_sas);

    // Copying the result, JNI handles the memory.
    char* c_sas = PyUnicode_AsUTF8(py_sas);
    char* c_sas_copy = new char[strlen(c_sas) + 1]; // +1 to accomodate for the null terminator
    strcpy(c_sas_copy, c_sas);
    return env->NewStringUTF(c_sas_copy);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_softbankrobotics_fastdownwardplanner_FastDownwardKt_searchPlanFromSAS(
        JNIEnv *env, jclass /*clazz*/, jstring sas_problem, jstring search_strategy) {

    // Convert the passed string arguments to C strings.
    const char* sas = env->GetStringUTFChars(sas_problem, nullptr);
    const char* strategy = env->GetStringUTFChars(search_strategy, nullptr);

    // Make sure we drop a reference to the passed arguments after they are used.
    auto string_args_references = make_disposable([&] {
        env->ReleaseStringUTFChars(sas_problem, sas);
        env->ReleaseStringUTFChars(search_strategy, strategy);
    });

    // Perform the planning.
    try {
        std::string plan = plan_from_sas(sas, strategy);
        return env->NewStringUTF(plan.c_str());
    } catch (const std::exception& e) {
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"), e.what());
        return nullptr;
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_softbankrobotics_fastdownwardplanner_FastDownwardKt_searchPlan(
        JNIEnv *env, jclass clazz, jstring domain, jstring problem, jstring strategy) {
    // TODO: can be optimized by avoiding conversion between C++ and Java.
    auto sas = Java_com_softbankrobotics_fastdownwardplanner_FastDownwardKt_translatePDDLToSAS(env, clazz, domain, problem);
    if (!sas) {
        // A JNI error should already be propagating.
        return nullptr;
    }
    return Java_com_softbankrobotics_fastdownwardplanner_FastDownwardKt_searchPlanFromSAS(env, clazz, sas, strategy);
}
