//
// Created by victor.paleologue on 02/11/20.
//
#include "python-jni.h"
#include <iostream>
#include <streambuf>
#include <string>
#include <Python.h>
#include "disposable.hpp"

#ifdef COUT_TO_ANDROID_LOG
# include <android/log.h>
#endif // COUT_TO_ANDROID_LOG

namespace {
#ifdef COUT_TO_ANDROID_LOG
    // https://stackoverflow.com/questions/8870174/is-stdcout-usable-in-android-ndk
    class forward_to_android : public std::streambuf {
    public:
        enum {
            bufsize = 1024
        }; // ... or some other suitable buffer size
        forward_to_android() { this->setp(buffer, buffer + bufsize - 1); }

    private:
        int overflow(int c) override {
            if (c == traits_type::eof()) {
                *this->pptr() = traits_type::to_char_type(c);
                this->sbumpc();
            }
            return this->sync() ? traits_type::eof() : traits_type::not_eof(c);
        }

        int sync() override {
            int rc = 0;
            if (this->pbase() != this->pptr()) {
                char writebuf[bufsize + 1];
                memcpy(writebuf, this->pbase(), this->pptr() - this->pbase());
                writebuf[this->pptr() - this->pbase()] = '\0';

                rc = __android_log_write(ANDROID_LOG_DEBUG, "native", writebuf) > 0;
                this->setp(buffer, buffer + bufsize - 1);
            }
            return rc;
        }

        char buffer[bufsize];
    };
#endif // COUT_TO_ANDROID_LOG
}


extern "C"
JNIEXPORT jstring JNICALL
Java_com_softbankrobotics_python_PythonKt_initCPython(
        JNIEnv *env, jclass /*clazz*/, jstring j_path) {

#ifdef COUT_TO_ANDROID_LOG
    // Also initialize the forwarding of logs to Android.
    std::cout.rdbuf(new forward_to_android);
    // TODO: write this in a way that does not leak and does not conflict with other libraries
#endif // COUT_TO_ANDROID_LOG

    const char *c_path = env->GetStringUTFChars(j_path, nullptr);
    auto c_path_reference = make_disposable([&] {
        env->ReleaseStringUTFChars(j_path, c_path);
    });
    wchar_t *c_w_path = Py_DecodeLocale(c_path, nullptr);
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

/**
 * Retrieves the current Python exception and translates it in a JNI exception.
 * In JNI, the exception is not directly thrown from C++.
 * Instead, the C++ should return a null result after having the JNI exception is set.
 * @param jni
 */
void forward_python_exception_to_jni(JNIEnv *jni) {
    std::string error_message;
    if (PyErr_Occurred()) {
        // Retrieve the exception info.
        PyObject *type = nullptr;
        PyObject *value = nullptr;
        PyObject *traceback = nullptr;
        PyErr_Fetch(&type, &value, &traceback);
        PyErr_NormalizeException(&type, &value, &traceback);
        assert(value);

        if (traceback) {
            // When the traceback is available,
            // we can use the `traceback` module to get a nice report.
            PyObject *traceback_module = PyImport_ImportModule("traceback");
            assert(traceback_module);
            MANAGE_RESULT(traceback_module);
            PyObject *format_exception = PyObject_GetAttrString(traceback_module,
                                                                "format_exception");
            assert(format_exception);
            MANAGE_RESULT(format_exception);

            // Format function takes the exception info as arguments.
            PyObject *format_exception_args = PyTuple_New(3);
            MANAGE_RESULT(format_exception_args);
            PyTuple_SetItem(format_exception_args, 0, type);
            PyTuple_SetItem(format_exception_args, 1, value);
            PyTuple_SetItem(format_exception_args, 2, traceback);

            PyObject *formatted_exception = PyObject_Call(format_exception, format_exception_args,
                                                          nullptr);
            MANAGE_RESULT(formatted_exception);
            const char *c_formatted_exception = PyUnicode_AsUTF8(formatted_exception);
            if (c_formatted_exception) {
                error_message = c_formatted_exception;
            }
        }

        if (error_message.empty()) {
            // When the traceback is absent or if formatting exception went wrong,
            // the report consists in showing the exception.
            PyObject *value_str = PyObject_Str(value);
            MANAGE_RESULT(value_str);
            const char *c_value_str = PyUnicode_AsUTF8(value_str);
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
