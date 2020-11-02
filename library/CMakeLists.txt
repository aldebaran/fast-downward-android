# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

project("FastDownwardAndroid")

# Fast Downward uses both Python and C++ to perform, respectively,
# the translation of the PDDL into SAS, and the search for a plan from an SAS problem.
# Here we bring them together into a JNI lib for Android.

# Translation using Python.
# ====================================================================
# Include Python to recompile it, but only the shared library.
# Required CMake arguments are passed down from Gradle's invocation of CMake.
set(BUILD_LIBPYTHON_SHARED ON CACHE BOOL "Also produce a shared library" FORCE)
set(PYTHON_VERSION_MAJOR_MINOR "3.6")
set(PYTHON_VERSION "${PYTHON_VERSION_MAJOR_MINOR}.7")
add_subdirectory(python-cmake-buildsystem)
include_directories(
        "${CMAKE_CURRENT_BINARY_DIR}/Python-${PYTHON_VERSION}/Include"
        "${CMAKE_CURRENT_BINARY_DIR}/python-cmake-buildsystem/bin")
set(PYTHON_IN_CMAKE_CURRENT_BINARY_DIR "${CMAKE_CURRENT_BINARY_DIR}/python-cmake-buildsystem/lib/python${PYTHON_VERSION_MAJOR_MINOR}")

# Copy fast-downward's files in the Python output directory.
# TODO: if we can make site-packages work, move them there instead.
add_custom_target(move-fast-downward-translate-to-python-modules ALL
        COMMAND
        ${CMAKE_COMMAND} -E copy_directory
        "${CMAKE_CURRENT_SOURCE_DIR}/fast-downward/src/translate"
        "${PYTHON_IN_CMAKE_CURRENT_BINARY_DIR}")
add_dependencies(move-fast-downward-translate-to-python-modules python)

# Force the copy of the Python modules as a requirement for building Python.
# The files are put in an archive in the assets directory,
# so that they can be unarchived in the app's private directory at runtime.
# This way Python can use classical filesystem operations to load its base modules.
find_program(ZIP_FOUND zip)
if (NOT ZIP_FOUND)
    message(FATAL_ERROR "No such command-line tool `zip`.")
endif ()

# TODO: have Gradle tell CMake where to put that zip.
set(ASSETS_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/src/main/assets")
add_custom_target(ensure-assets-directory ALL
        COMMAND ${CMAKE_COMMAND} -E make_directory ${ASSETS_DIRECTORY})

set(PYTHON_MODULES_ZIP_IN_ASSETS "${ASSETS_DIRECTORY}/python-modules.zip")
add_custom_target(remove-previous-python-modules-to-android-asset ALL
        COMMAND
        ${CMAKE_COMMAND} -E remove "${PYTHON_MODULES_ZIP_IN_ASSETS}")

add_custom_target(zip-python-modules-to-android-asset ALL
        COMMAND
        zip -r "${PYTHON_MODULES_ZIP_IN_ASSETS}" "*" -x \\*.so -x \\*.exe -x \\*.whl
        WORKING_DIRECTORY
        "${PYTHON_IN_CMAKE_CURRENT_BINARY_DIR}")

add_dependencies(
        zip-python-modules-to-android-asset

        python
        move-fast-downward-translate-to-python-modules
        remove-previous-python-modules-to-android-asset
        ensure-assets-directory
    )

# Searching Plan using C++.
# =====================================================================
add_subdirectory("fast-downward/src")
include_directories("fast-downward/src/search")

# JNI wrapper for Android.
# =====================================================================
# It is added as a dependency automatically on Android only.
if (NOT ANDROID)
    find_package(JNI REQUIRED)
    message(STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
    message(STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")
    include_directories(${JNI_INCLUDE_DIRS})
endif ()

# Double-check Android log option consistency.
if (COUT_TO_ANDROID_LOG)
    if (NOT ANDROID)
        message(WARNING "COUT_TO_ANDROID_LOG is ON but ANDROID is OFF, option ignored")
        set(COUT_TO_ANDROID_LOG OFF)
    endif ()
endif ()

# A shared library wrapping Python in JNI.
add_library(
        python-jni
        SHARED
        "src/main/cpp/python-jni.cpp")

target_link_libraries(python-jni PRIVATE libpython-shared)
target_link_libraries(python-jni PUBLIC ${JNI_LIBRARIES})
if (COUT_TO_ANDROID_LOG)
    target_compile_definitions(python-jni PRIVATE COUT_TO_ANDROID_LOG)
    target_link_libraries(python-jni PRIVATE log)
endif ()
add_dependencies(python-jni python zip-python-modules-to-android-asset)

# A shared library wrapping Fast Downward in JNI.
add_library(
        fast-downward-jni
        SHARED
        "src/main/cpp/fast-downward-jni.cpp")

target_link_libraries(fast-downward-jni PRIVATE libpython-shared python-jni fast-downward)
target_link_libraries(fast-downward-jni PUBLIC ${JNI_LIBRARIES})
if (COUT_TO_ANDROID_LOG)
    target_compile_definitions(fast-downward-jni PRIVATE COUT_TO_ANDROID_LOG)
    target_link_libraries(fast-downward-jni PRIVATE log)
endif ()
add_dependencies(fast-downward-jni fast-downward)

# Testing.
#=====================================================================
# Only for other platforms than Android, to check for memory issues.
if (NOT ANDROID)
    enable_testing()
    add_executable(test_fast-downward-jni "src/main/cpp/test_fast-downward-jni.cpp")
    target_link_libraries(test_fast-downward-jni PRIVATE fast-downward-jni python-jni libpython-shared)
    add_dependencies(test_fast-downward-jni fast-downward-jni python-jni libpython-shared)

    set(RAW_RESOURCES_DIR "${CMAKE_CURRENT_SOURCE_DIR}/src/main/res/raw")
    add_test(NAME test_fast-downward-jni
            COMMAND test_fast-downward-jni
            "${PYTHON_IN_CMAKE_CURRENT_BINARY_DIR}"
            "${RAW_RESOURCES_DIR}/simple.pddl")
endif ()
