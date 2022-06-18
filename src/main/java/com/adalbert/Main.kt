package com.adalbert

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

private const val disableOverrideCheck = false

private val projectName = { language: String -> "jmh-$language" }
private val alphabet = ('a' .. 'z').union('A' .. 'Z').union('0' .. '9')
private val compileCommand = listOf("C:\\Program Files\\Maven\\bin\\mvn.cmd", "clean", "install")
private val listBenchmarksCommand = listOf("java", "-jar", "target\\benchmarks.jar", "-l")
private val supportedLanguages = listOf("java", "scala")

private val baseCodePath = Paths.get("C:\\Users\\wojci\\source\\master-thesis\\generated\\multiOperationalOwn\\Run_2022-05-07_11-27-23\\")
private val baseOutcomesPath = Paths.get("C:\\Users\\wojci\\source\\master-thesis\\measurements\\raw")

fun deleteFilesNotDirectories(path: Path) {
    Files.list(path).forEach {
        val file = it.toFile()
        if (file.isDirectory)
            deleteFilesNotDirectories(it)
        file.delete() || throw IllegalStateException("Couldn't delete $it!")
    }
}

fun main () {
    val baseCodeString = baseCodePath.toString()
    val finalOutputsPath = if (baseCodeString.contains("polya", true)) baseOutcomesPath.add("polya")
        else if (baseCodeString.contains("single", true)) baseOutcomesPath.add("single")
        else if (baseCodeString.contains("types", true)) baseOutcomesPath.add("types")
        else baseOutcomesPath.add("own")

    if (!disableOverrideCheck) {
        val sequence = (0 until 5).map { alphabet.random() }.joinToString("")
        println("About to override the $finalOutputsPath directory!")
        println("If you are sure about the override, enter this sequence: '$sequence'")
        print("Input: ")
        val consoleInput = readLine()
        if (consoleInput != sequence)
            println("Operation cancelled!").apply { return }
        println("Override accepted!")
        deleteFilesNotDirectories(finalOutputsPath)
    }

    supportedLanguages.forEach { language ->
        val projectDir = baseCodePath.add(projectName(language)).toFile()
        val processBuilder = ProcessBuilder().directory(projectDir)
        val compileStream = processBuilder.command(compileCommand).startWaitAndRedirect(90)
//        print(String(compileStream.readNBytes(compileStream.available())))
        val listingStream = processBuilder.command(listBenchmarksCommand).startWaitAndRedirect(10)
        val benchmarksRaw = String(listingStream.readAllBytes())
        val benchmarks = benchmarksRaw
            .substringAfterLast("Benchmarks:").trim().lines()
            .map { it.substringBeforeLast('.').substringAfterLast('.') }
            .map { it.substringAfter('_').substringBefore('_') }
            .toSet()
        benchmarks.forEach {
            val absoluteProjectPath = projectDir.absolutePath.replace("\\", "/")
            val absoluteOutcomePath = finalOutputsPath.add("$it.txt").toString().replace("\\", "/")
            println("java -jar $absoluteProjectPath/target/benchmarks.jar ${it}_ -prof gc >> $absoluteOutcomePath 2>&1")
        }
    }

}