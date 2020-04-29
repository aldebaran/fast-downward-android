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
endif()

# TODO: have Gradle tell CMake where to put that zip.
set(PYTHON_MODULES_ZIP_IN_ASSETS "${CMAKE_CURRENT_SOURCE_DIR}/src/main/assets/python-modules.zip")
add_custom_target(remove-previous-python-modules-to-android-asset ALL
        COMMAND
            ${CMAKE_COMMAND} -E remove "${PYTHON_MODULES_ZIP_IN_ASSETS}")

add_custom_target(zip-python-modules-to-android-asset ALL
        COMMAND
            zip -r "${PYTHON_MODULES_ZIP_IN_ASSETS}" "*" -x \\*.so
        WORKING_DIRECTORY
            "${PYTHON_IN_CMAKE_CURRENT_BINARY_DIR}")

add_dependencies(
        zip-python-modules-to-android-asset

        python
        move-fast-downward-translate-to-python-modules
        remove-previous-python-modules-to-android-asset)

# Searching Plan using C++.
# =====================================================================
add_subdirectory("fast-downward/src")
include_directories("fast-downward/src/search")

# JNI wrapper for Android.
# =====================================================================
# It is added as a dependency automatically on Android only.
if (NOT ANDROID)
    find_package(JNI REQUIRED)
    message (STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
    message (STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")
    include_directories(${JNI_INCLUDE_DIRS})
endif()

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add_library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

add_library( # Specifies the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             "src/main/cpp/native-lib.cpp")

target_link_libraries(native-lib PRIVATE libpython-shared fast-downward)
target_link_libraries(native-lib PUBLIC ${JNI_LIBRARIES})
add_dependencies(native-lib python fast-downward zip-python-modules-to-android-asset)

# Testing.
#=====================================================================
# Only for other platforms than Android, to check for memory issues.
if(NOT ANDROID)
  enable_testing()
  add_executable(test_native-lib "src/main/cpp/test_native-lib.cpp")
  target_link_libraries(test_native-lib PRIVATE native-lib)
  add_dependencies(test_native-lib native-lib)

  set(RAW_RESOURCES_DIR "${CMAKE_CURRENT_SOURCE_DIR}/src/main/res/raw")
  add_test(NAME test_native-lib
          COMMAND test_native-lib
              "${PYTHON_IN_CMAKE_CURRENT_BINARY_DIR}"
              "${RAW_RESOURCES_DIR}/fast_downward_2.pddl")
endif()
