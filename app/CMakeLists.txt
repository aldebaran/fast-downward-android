# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

project("FastDownwardAndroid")

# Fast Downward uses both Python and C++ to perform, respectively,
# the translation of the PDDL into SAS, and the search for a plan from an SAS problem.
# Here we bring them together into a JNI lib for Android.

# Translate
# ====================================================================
# Include Python to recompile it.
# Required CMake arguments are passed down from Gradle's invocation of CMake.
# TODO: let the Python version be an option.
add_subdirectory(python-cmake-buildsystem)
include_directories(
        "${CMAKE_CURRENT_BINARY_DIR}/Python-3.6.7/Include"
        "${CMAKE_CURRENT_BINARY_DIR}/python-cmake-buildsystem/bin")
set(PYTHON_IN_CMAKE_CURRENT_BINARY_DIR "${CMAKE_CURRENT_BINARY_DIR}/python-cmake-buildsystem/lib/python3.6")

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

# Search Plan
# =====================================================================
add_subdirectory("fast-downward/src")
include_directories("fast-downward/src/search")

# JNI wrapper for Android
# =====================================================================
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

target_link_libraries(native-lib python fast-downward)

add_dependencies(native-lib zip-python-modules-to-android-asset fast-downward)