package com.adalbert

import java.io.File
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

fun Path.add(fragment: String): Path {
    return Paths.get(this.toString(), fragment)
}

fun ProcessBuilder.startWaitAndRedirect(timeout: Int): InputStream {
    val process = this.start()
    process.waitFor(timeout.toLong(), TimeUnit.SECONDS)
    return process.inputStream
}

fun ProcessBuilder.startAndWait(): Int {
    return this.start().waitFor()
}