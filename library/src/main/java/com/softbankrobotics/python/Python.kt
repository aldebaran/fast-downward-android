package com.softbankrobotics.python

import android.content.Context
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

val pythonIsInitialized: Boolean get() = mutablePythonIsInitialized
private var mutablePythonIsInitialized = false
lateinit var pythonSysPath: String

/**
 * Helper for tests that cannot tell if they are run along with other tests.
 * If you call this from production code, you are probably doing it wrong.
 */
fun ensurePythonInitialized(context: Context): String {
    return if (pythonIsInitialized)
        pythonSysPath
    else
        initializePython(context)
}

fun initializePython(context: Context): String {

    if (pythonIsInitialized)
        throw RuntimeException("Python is already initialized")

    val pythonDir = File(context.filesDir.path + "/python")
    if (pythonDir.exists()) {
        pythonDir.walkBottomUp().forEach { it.delete() }
    }
    pythonDir.mkdir()
    if (!pythonDir.isDirectory) {
        throw RuntimeException("could not prepare Python directory `$pythonDir`")
    }

    unzip(context.assets.open("python-modules.zip"), pythonDir)
    if (!File(pythonDir.path + "/encodings").exists()
        || !File(pythonDir.path + "/site.py").exists()
    ) {
        throw RuntimeException("incomplete Python installation in `$pythonDir`")
    }

    System.loadLibrary("python-jni")
    val sysPaths = initCPython(pythonDir.path)
    val sysPath = sysPaths.removePrefix("['").removeSuffix("']")
    if (sysPath != pythonDir.path) {
        throw IllegalStateException("PYTHONPATH could not be set properly")
    }
    pythonSysPath = sysPath
    mutablePythonIsInitialized = true
    return sysPath
}

/**
 * Initialization of CPython.
 * @param pythonHome Where the Python modules are to be found. Becomes the PYTHONPATH.
 * @return The effective PYTHONPATH. Should be equivalent to the parameter pythonHome.
 */
private external fun initCPython(pythonHome: String): String

/** Unzips the given data to the existing destination directory. */
internal fun unzip(data: InputStream, destination: File) {
    if (!destination.isDirectory)
        throw RuntimeException("destination is not a valid directory")

    ZipInputStream(data).use { zip ->
        var entry: ZipEntry?
        while (true) {
            entry = zip.nextEntry
            if (entry == null) break

            val destFile = File(destination.path + "/" + entry.name)
            if (entry.isDirectory) {
                destFile.mkdir()
                if (!destFile.isDirectory)
                    throw RuntimeException("directory \"${destFile.path}\" could not be created")
            } else {
                if (!destFile.createNewFile())
                    throw RuntimeException("file \"${destFile.path}\" could not be created")
                destFile.outputStream().use { zip.copyTo(it) }
            }
        }
    }
}
