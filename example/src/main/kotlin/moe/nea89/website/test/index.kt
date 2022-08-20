package moe.nea89.website.test

import kotlinext.js.require
import kotlinx.browser.document
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.img
import kotlinx.html.js.a
import kotlinx.html.js.div
import kotlinx.html.js.p
import moe.nea89.website.*
import styled.injectGlobal
import kotlin.time.Duration.Companion.milliseconds

val defaultFileSystem = fileSystem {
    "etc" {
        "passwd" text "hunter2"
    }
    "home/nea" {
        "todo" text """
                | - git gud
                | - finish this website
                | - convince the general public that comic sans is a viable font
            """.trimMargin()
        "moisturized" image require("images/moisturized.jpg")
        "download" download require("images/me.jpeg")
    }
    "flag" text "CTF{12345abcdefghijklmonp3.1.4.1.5.9.2.8}"
}

fun main() {

    require("@fontsource/comic-mono/index.css")
    injectGlobal(Styles.global)
    val root = document.body!!.append.div()
    val console = KConsole.createFor(root, fileSystem = defaultFileSystem)
    console.addLine("Starting up terminal.")
    console.PS1 = ">"
    console.rerender()
    console.registerCommand(command("cwd", "pwd") {
        val fa = requireFileAccessor()
        console.addLine(fa.currentDir.joinToString(separator = "/", prefix = "/"))
    })
    console.registerCommand(command("cd") {
        val fa = requireFileAccessor()
        val path = args.singleOrNull()
        if (path == null) {
            console.addLine("Usage: cd <directory>")
            return@command
        }
        val error = fa.cd(path)
        if (error != null) {
            console.addLine("cd: ${error.name}")
        }
    })
    console.registerCommand(command("ls") {
        val fa = requireFileAccessor()
        val path = when (args.size) {
            0 -> "."
            1 -> args[0]
            else -> {
                console.addLine("Usage: ls [directory or file]")
                return@command
            }
        }
        val file = fa.resolve(path)
        if (file == null) {
            console.addLine("ls: Could not find file or directory")
            return@command
        }
        when (file) {
            is KFile.Directory -> {
                val longestName = file.files.keys.maxOf { it.length }
                file.files.forEach { (name, file) ->
                    wait(200.milliseconds)
                    console.addLine(
                        name + " ".repeat(longestName + 1 - name.length) + file.fileType
                    )
                    console.rerender()
                }
            }

            else -> console.addLine("ls: is a ${file.fileType}")
        }
    })
    console.registerCommand(command("color") {
        console.addLine("This is a ", red("red"), " word: ", green("1.0"), " ", blue("BLUUEEE"))
    })
    console.registerCommand(command("cat") {
        val fa = requireFileAccessor()
        val path = when (args.size) {
            1 -> args[0]
            else -> {
                console.addLine("Usage: cat [directory or file]")
                return@command
            }
        }
        val file = fa.resolve(path)
        if (file == null) {
            console.addLine("cat: Could not find file or directory")
            return@command
        }
        when (file) {
            is KFile.Directory -> console.addLine("cat: Is a directory")
            is KFile.Text -> console.addMultilineText(file.text)
            is KFile.Image -> console.addLine(document.create.p {
                img(src = file.url)
            })

            is KFile.Download -> {
                val link = document.create.a(file.url)
                link.download = file.name.last()
                document.body!!.append(link)
                link.click()
                link.remove()
                console.addLine("Download started")
            }
        }
    })
    console.registerCommand(command("dick", "cock") {
        console.addMultilineText("Hehe")
    })
    console.registerCommand(command("boob", "booob") {
        console.addMultilineText(boobs)
    })
}