package com.adalbert

import java.nio.file.Files
import java.nio.file.Paths

private const val disableOverrideCheck = true

private val projectName = { language: String -> "jmh-$language" }
private val alphabet = ('a' .. 'z').union('A' .. 'Z').union('0' .. '9')
private val outputsPath = Paths.get("C:\\Users\\wojci\\source\\master-thesis\\measurements\\raw")
private val compileCommand = listOf("C:\\Program Files\\Maven\\bin\\mvn.cmd", "clean", "install")
private val listBenchmarksCommand = listOf("java", "-jar", "target\\benchmarks.jar", "-l")
private val supportedLanguages = listOf("java", "scala")

private val baseCodePath = Paths.get("C:\\Users\\wojci\\source\\master-thesis\\generated\\multiOperationalPolya\\Run_2022-04-02_20-24-45\\")
private val baseOutcomesPath = Paths.get("C:\\Users\\wojci\\source\\master-thesis\\measurements\\raw")

fun main () {
    if (!disableOverrideCheck) {
        val sequence = (0 until 5).map { alphabet.random() }.joinToString("")
        println("If you are sure about the override, enter this sequence: '$sequence'")
        print("Input: ")
        val consoleInput = readLine()
        if (consoleInput != sequence)
            println("Operation cancelled!").apply { return }
        else println("Override accepted!")
    }

    Files.list(outputsPath).forEach {
        val wasDeleted = it.toFile().deleteRecursively()
        if (!wasDeleted) throw IllegalStateException("Couldn't delete $it")
    }

    println("chcp 65001")
    supportedLanguages.forEach { language ->
        val projectDir = baseCodePath.add(projectName(language)).toFile()
        val processBuilder = ProcessBuilder().directory(projectDir)
        val compileStream = processBuilder.command(compileCommand).startWaitAndRedirect(10)
//        print(String(compileStream.readNBytes(compileStream.available())))
        val listingStream = processBuilder.command(listBenchmarksCommand).startWaitAndRedirect(3)
        val benchmarksRaw = String(listingStream.readNBytes(listingStream.available()))
        val benchmarks = benchmarksRaw
            .substringAfterLast("Benchmarks:").trim().lines()
            .map { it.substringBeforeLast('.').substringAfterLast('.') }
            .map { it.substringAfter('_').substringBefore('_') }
            .toSet()
        benchmarks.forEach {
            val absoluteProjectPath = projectDir.absolutePath.replace("\\", "/")
            val absoluteOutcomePath = baseOutcomesPath.add("$it.txt").toString().replace("\\", "/")
            println("java -jar $absoluteProjectPath/target/benchmarks.jar $it -prof gc >> $absoluteOutcomePath 2>&1")
        }
    }

}