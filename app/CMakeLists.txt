# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

project("FastDownwardTranslate")

# Include Python to recompile it.
add_subdirectory(python-cmake-buildsystem)
include_directories(
        "python-cmake-buildsystem/Python-3.6.7/Include"
        "${CMAKE_CURRENT_BINARY_DIR}/python-cmake-buildsystem/bin")
install(EXPORT python-base DIRECTORY
        DESTINATION "lib")
set_target_properties(python
        PROPERTIES
        ARCHIVE_OUTPUT_DIRECTORY
        "${CMAKE_CURRENT_BINARY_DIR}/python-cmake-buildsystem/lib/python3.6"
        "${distribution_DIR}/gmath/lib/${ANDROID_ABI}")

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

target_link_libraries(native-lib python)