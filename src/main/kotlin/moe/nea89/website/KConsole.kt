package moe.nea89.website

import kotlinx.browser.document
import kotlinx.dom.addClass
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.js.p
import kotlinx.html.js.pre
import kotlinx.html.js.span
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLParagraphElement
import org.w3c.dom.HTMLPreElement
import org.w3c.dom.events.KeyboardEvent
import kotlin.collections.set

class KConsole(
    private val root: HTMLElement,
    private val text: HTMLPreElement,
    private val prompt: HTMLElement,
    fileSystem: KFileSystem?,
) {

    val fileAccessor = fileSystem?.let { FileAccessor(it) }

    companion object {
        val shlexRegex =
            """"([^"\\]+|\\.)+"|([^ "'\\]+|\\.)+|'([^'\\]+|\\.)+'""".toRegex()

        fun createFor(element: HTMLElement, fileSystem: KFileSystem? = null): KConsole {
            val text = element.append.pre()
            val prompt = text.append.p()
            prompt.addClass(Styles.promptClass)
            element.classList.add(Styles.consoleClass)
            val console = KConsole(element, text, prompt, fileSystem)
            document.body!!.onkeydown = console::keydown
            console.addLine("Starting up terminal.")
            console.rerender()
            return console
        }
    }

    enum class ConsoleState {
        SHELLPROMPT,
        IN_PROGRAM
    }

    var state = ConsoleState.SHELLPROMPT

    var input: String = ""

    fun addLines(newLines: List<String>) {
        newLines.forEach { addLine(it) }
    }

    fun addMultilineText(text: String) {
        addLines(text.split("\n"))
    }

    fun addLine(vararg elements: Any) {
        addLine(document.create.p().apply {
            elements.forEach {
                when (it) {
                    is HTMLElement -> append(it)
                    is ColoredElement -> append(document.create.span().also { el ->
                        el.style.color = it.color.color.toString()
                        el.append(it.text)
                    })
                    is String -> append(it)
                    else -> throw RuntimeException("Unknown element")
                }
            }
        })
    }

    private fun addLine(element: HTMLParagraphElement) {
        text.insertBefore(element, prompt)
    }

    fun rerender() {
        if (state == KConsole.ConsoleState.SHELLPROMPT) {
            prompt.innerText = "${'$'} $input"
        } else {
            prompt.innerText = ""
        }
    }

    fun scrollDown() {
        text.lastElementChild?.scrollIntoView()
    }

    fun registerCommand(command: Command) {
        command.aliases.forEach {
            commands[it] = command
        }
        commands[command.name] = command
    }

    val commands = mutableMapOf<String, Command>()

    fun executeCommand(commandLine: String) {
        val parts = shlex(commandLine)
        if (parts == null) {
            addLine("Syntax Error")
            return
        }
        if (parts.isEmpty()) {
            return
        }
        val command = parts[0]
        println("Running command: $command")
        val arguments = parts.drop(1)
        val commandThing = commands[command]
        if (commandThing == null) {
            addLine("Unknown command")
            return
        }
        ShellExecutionContext.run(this, commandThing, command, arguments)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun shlex(command: String): List<String>? {
        var i = 0
        val parts = mutableListOf<String>()
        while (i < command.length) {
            val match = shlexRegex.matchAt(command, i)
            if (match == null) {
                println("Could not shlex: $command")
                return null
            }
            // TODO: Proper string unescaping
            parts.add(match.groupValues.drop(1).firstOrNull { it != "" } ?: "")
            i += match.value.length
            while (command[i] == ' ' && i < command.length)
                i++
        }
        return parts
    }

    fun keydown(event: KeyboardEvent) {
        if (event.altKey || event.ctrlKey || event.metaKey) return
        if (event.isComposing || event.keyCode == 229) return
        if (state != ConsoleState.SHELLPROMPT) return
        when (event.key) {
            "Enter" -> {
                val toExecute = input
                addLine("${'$'} $toExecute")
                input = ""
                executeCommand(toExecute)
            }
            "Backspace" -> input = input.substring(0, input.length - 1)
            else ->
                if (event.key.length == 1 || event.key.any { it !in 'a'..'z' && it !in 'A'..'Z' })
                    input += event.key
        }
        event.preventDefault()
        rerender()
        scrollDown()
    }
}
