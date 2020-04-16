package com.softbankrobotics.fastdownwardplanner

import android.content.Context
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

internal var isPythonInitialized = false

/** Initialization of CPython. */
internal external fun initCPython(pythonHome: String): String

/** Executes an arbitrary Python snippet using CPython. Returns the result converted to a string. */
internal external fun execCPython(script: String): String

/** Initializes the global Python interpreted. */
fun initializePython(context: Context): String {

    if (isPythonInitialized)
        throw java.lang.RuntimeException("Python is already initialized")

    val pythonDir = File(context.filesDir.path + "/python")
    if (pythonDir.exists()) {
        pythonDir.walkBottomUp().forEach { it.delete() }
    }
    pythonDir.mkdir()
    if(!pythonDir.isDirectory) {
        throw RuntimeException("could not prepare Python directory `$pythonDir`")
    }

    unzip(context.assets.open("python-modules.zip"), pythonDir)
    if(!File(pythonDir.path + "/encodings").exists()
        || !File(pythonDir.path + "/site.py").exists()) {
        throw RuntimeException("incomplete Python installation in `$pythonDir`")
    }

    System.loadLibrary("native-lib")
    val dynamicLibrariesDir = File("${pythonDir.path}/lib-dynload")
    dynamicLibrariesDir.list()!!.forEach {
        System.loadLibrary("${dynamicLibrariesDir.path}/$it")
    }
    val sysPaths = initCPython(pythonDir.path)
    val sysPath = sysPaths.removePrefix("['").removeSuffix("']")
    assert(sysPath == pythonDir.path)
    isPythonInitialized = true
    return sysPath
}

/** Unzips the given data to the existing destination directory. */
internal fun unzip(data: InputStream, destination: File) {
    if (!destination.isDirectory)
        throw RuntimeException("destination is not a valid directory")

    ZipInputStream(data).use { zip ->
        var entry: ZipEntry?
        while (true) {
            entry = zip.nextEntry
            if (entry == null) break;

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

/* Runs the desired Python script. */
fun execPython(script: String): String {
    if (!isPythonInitialized)
        throw java.lang.RuntimeException("Python has not been initialized yet")
    return execCPython(script)
}
