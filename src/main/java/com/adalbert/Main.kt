package com.adalbert

import java.nio.file.Files
import java.nio.file.Paths

private const val disableOverrideCheck = false

private val projectName = { language: String -> "jmh-$language" }
private val alphabet = ('a' .. 'z').union('A' .. 'Z').union('0' .. '9')
private val outputsPath = Paths.get("C:\\Users\\wojci\\source\\master-thesis\\measurements\\raw")
private val compileCommand = listOf("C:\\Program Files\\Maven\\bin\\mvn.cmd", "clean", "install")
private val listBenchmarksCommand = listOf("java", "-jar", "target\\benchmarks.jar", "-l")
private val supportedLanguages = listOf("java", "scala")

private val baseCodePath = Paths.get("C:\\Users\\wojci\\source\\master-thesis\\generated\\multiOperationalPolya\\Run_2022-04-10_17-51-33\\")
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

    supportedLanguages.forEach { language ->
        val projectDir = baseCodePath.add(projectName(language)).toFile()
        val processBuilder = ProcessBuilder().directory(projectDir)
        val compileStream = processBuilder.command(compileCommand).startWaitAndRedirect(40)
//        print(String(compileStream.readNBytes(compileStream.available())))
        val listingStream = processBuilder.command(listBenchmarksCommand).startWaitAndRedirect(5)
        val benchmarksRaw = String(listingStream.readAllBytes())
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