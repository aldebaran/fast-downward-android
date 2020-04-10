#include <iostream>
#include <list>
#include <string>
#include <jni.h>
#include <Python.h>

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
Java_com_softbankrobotics_fastdownwardplanner_NativeKt_searchPlanFromSAS(
        JNIEnv *env, jclass /*clazz*/, jstring sas_problem, jstring search_strategy) {

    // Convert the passed string arguments to C strings.
    const char* sas = env->GetStringUTFChars(sas_problem, 0);
    const char* strategy = env->GetStringUTFChars(search_strategy, 0);

    // Make sure we drop a reference to the passed arguments after they are used.
    auto string_args_references = make_disposable([&] {
        env->ReleaseStringUTFChars(sas_problem, sas);
        env->ReleaseStringUTFChars(search_strategy, strategy);
    });

    // Perform the planning.
    try {
        std::string plan = "salut monde!";//plan_from_sas(sas, strategy);
        return env->NewStringUTF(plan.c_str());
    } catch (const std::exception& e) {
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"), e.what());
        return nullptr;
    }
}

//extern "C"
//JNIEXPORT jstring JNICALL
//Java_com_softbankrobotics_fastdownwardplanner_NativeKt_translatePDDLtoSAS(
//        JNIEnv *env, jclass /*clazz*/, jstring pddl) {
//
//    Py_Initialize();
//    PyRun_SimpleString("from time import time,ctime\n"
//                       "print 'Today is',ctime(time())\n");
//    Py_Finalize();
//}

extern "C"
JNIEXPORT void JNICALL
Java_com_softbankrobotics_fastdownwardplanner_NativeKt_helloPython(
        JNIEnv *env, jclass /*clazz*/) {

    Py_Initialize();
    PyRun_SimpleString("print('Hello, world!')");
    Py_Finalize();
}