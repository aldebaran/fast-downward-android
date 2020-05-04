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
    if (argc < 3 || argc > 4) {
        std::cout << "Usage: " << argv[0] << " PYTHONPATH PDDL_FILE [PROFILING]" << std::endl;
        throw std::invalid_argument("incorrect number of arguments");
    }
    auto pythonpath = argv[1];
    auto pddl_path = argv[2];
    bool is_profiling = false;
    if (argc == 4) {
      is_profiling = atoi(argv[3]);
    }

#define LOG if (!is_profiling) std::cout

    LOG << "PYTHONPATH=" << pythonpath << std::endl;

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
    LOG << "Effective PYTHONPATHs=" << effective_pythonpath << std::endl;

    if (!strstr(effective_pythonpath, pythonpath))
        throw std::runtime_error("discrepancy of proposed and effective PYTHONPATHs");

    env->ReleaseStringUTFChars(j_effective_pythonpath, effective_pythonpath);

    // Extracting the PDDL from the provided file.
    std::ifstream pddl_file;
    pddl_file.open(pddl_path);
    std::stringstream pddl_ss;
    pddl_ss << pddl_file.rdbuf();
    auto pddl = pddl_ss.str();
    LOG << "PDDL:" << std::endl << pddl << std::endl;

    // The file should exhibit a domain, and then a problem.
    size_t split_pos = pddl.find("(define (problem");
    std::string domain = pddl.substr(0, split_pos);
    std::string problem = pddl.substr(split_pos);

    unsigned repeats = 1;
    if (is_profiling)
      repeats = 100;

    for (unsigned i = 0; i < repeats; ++i) {
      // Searching for a plan.
      LOG << "Domain:" << std::endl << domain << std::endl;
      LOG << "Problem:" << std::endl << problem << std::endl;
      auto j_sas = Java_com_softbankrobotics_pddlsandbox_PlanningKt_translatePDDLToSAS(
              env, nullptr, env->NewStringUTF(domain.c_str()), env->NewStringUTF(problem.c_str()));
      if (!j_sas) {
          throw std::runtime_error("Failed to translate PDDL to SAS");
      }
      auto c_sas = env->GetStringUTFChars(j_sas, nullptr);
      LOG << "SAS:" << std::endl << c_sas << std::endl;

      auto j_plan = Java_com_softbankrobotics_pddlsandbox_PlanningKt_searchPlanFromSAS(
              env, nullptr, j_sas, env->NewStringUTF("astar(add())"));
      auto c_plan = env->GetStringUTFChars(j_plan, nullptr);
      LOG << "Plan:" << std::endl << env->GetStringUTFChars(j_plan, nullptr) << std::endl;

      // Clean-up.
      env->ReleaseStringUTFChars(j_plan, c_plan);
      env->ReleaseStringUTFChars(j_sas, c_sas);
    }

    jvm->DestroyJavaVM();
    return EXIT_SUCCESS;
}
