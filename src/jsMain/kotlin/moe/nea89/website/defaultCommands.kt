package moe.nea89.website

import kotlinx.browser.document
import kotlinx.html.dom.create
import kotlinx.html.img
import kotlinx.html.js.a
import kotlinx.html.js.onLoadFunction
import kotlinx.html.js.p
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

fun defaultCwdCommand(name: String, vararg names: String) = command(name, *names) {
    val fa = requireFileAccessor()
    console.addLine(fa.currentDir.joinToString(separator = "/", prefix = "/"))
}

fun defaultCdCommand(name: String, vararg names: String) = command(name, *names) {
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
}

fun defaultCatCommand(name: String, vararg names: String) = command(name, *names) {
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
            img(src = file.url) {
                this.onLoadFunction = { console.scrollDown() }
            }
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
}


fun defaultLsCommand(name: String, vararg names: String, delayBetweenLines: Duration = 200.milliseconds) =
    command(name, *names) {
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
                    wait(delayBetweenLines)
                    console.addLine(
                        name + " ".repeat(longestName + 1 - name.length) + file.fileType
                    )
                    console.rerender()
                }
            }

            else -> console.addLine("ls: is a ${file.fileType}")
        }
    }

