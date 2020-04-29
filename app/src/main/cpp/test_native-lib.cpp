#include <stdio.h>
#include <cstring>
#include <fstream>
#include <sstream>
#include <iostream>
#include <jni.h>
#include "native-lib.h"

#ifdef ANDROID
using JNIEnvInit = JNIEnv;
#else // On other platforms, initialization takes a void**
using JNIEnvInit = void;
#endif

int main(int argc, char** argv) {

    // Checking arguments.
    if (argc != 3) {
        std::cout << "Usage: " << argv[0] << " PYTHONPATH PDDL" << std::endl;
        throw std::invalid_argument("incorrect number of arguments");
    }
    auto pythonpath = argv[1];
    std::cout << "PYTHONPATH=" << pythonpath << std::endl;
    auto pddl_path = argv[2];

    // Initialization of Java.
    // From https://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/invocation.html
    JavaVM *jvm;       /* denotes a Java VM */
    JNIEnv *env;       /* pointer to native method interface */
    JavaVMInitArgs vm_args; /* JDK/JRE 6 VM initialization arguments */
    JavaVMOption* options = new JavaVMOption[1];
    options[0].optionString = const_cast<char*>("-Djava.class.path=/usr/lib/java");
    vm_args.version = JNI_VERSION_1_6;
    vm_args.nOptions = 1;
    vm_args.options = options;
    vm_args.ignoreUnrecognized = false;
    /* load and initialize a Java VM, return a JNI interface
     * pointer in env */
    JNI_CreateJavaVM(&jvm, (JNIEnvInit**)&env, &vm_args);
    delete[] options;

    // Initialization of Python.
    jstring j_effective_pythonpath =
        Java_com_softbankrobotics_pddlsandbox_PlanningKt_initCPython(
            env, nullptr, env->NewStringUTF(pythonpath));

    const char* effective_pythonpath = env->GetStringUTFChars(j_effective_pythonpath, 0);
    std::cout << "Effective PYTHONPATHs=" << effective_pythonpath << std::endl;

    if (!strstr(effective_pythonpath, pythonpath))
        throw std::runtime_error("discrepancy of proposed and effective PYTHONPATHs");

    // Extracting the PDDL from the provided file.
    std::ifstream pddl_file;
    pddl_file.open(pddl_path);
    std::stringstream pddl_ss;
    pddl_ss << pddl_file.rdbuf();
    auto pddl = pddl_ss.str();
    std::cout << "PDDL:" << std::endl << pddl << std::endl;

    // The file should exhibit a domain, and then a problem.
    size_t split_pos = pddl.find("(define (problem");
    std::string domain = pddl.substr(0, split_pos);
    std::string problem = pddl.substr(split_pos);

    // Searching for a plan.
    std::cout << "Domain:" << std::endl << domain << std::endl;
    std::cout << "Problem:" << std::endl << problem << std::endl;
    auto sas = Java_com_softbankrobotics_pddlsandbox_PlanningKt_translatePDDLToSAS(
            env, nullptr, env->NewStringUTF(domain.c_str()), env->NewStringUTF(problem.c_str()));
    if (!sas) {
        throw std::runtime_error("Failed to translate PDDL to SAS");
    }
    std::cout << "SAS:" << std::endl << env->GetStringUTFChars(sas, nullptr) << std::endl;

    auto plan = Java_com_softbankrobotics_pddlsandbox_PlanningKt_searchPlanFromSAS(
            env, nullptr, sas, env->NewStringUTF("astar(add())"));
    std::cout << "Plan:" << std::endl << env->GetStringUTFChars(plan, nullptr) << std::endl;

    // Clean-up.
    env->ReleaseStringUTFChars(sas, env->GetStringUTFChars(sas, nullptr));
    env->ReleaseStringUTFChars(plan, env->GetStringUTFChars(plan, nullptr));

    jvm->DestroyJavaVM();
    return EXIT_SUCCESS;
}
