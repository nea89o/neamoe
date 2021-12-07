package moe.nea89.website

import kotlinx.browser.document
import kotlinx.html.dom.append
import kotlinx.html.js.pre
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLPreElement
import org.w3c.dom.events.KeyboardEvent

class KConsole(private val root: HTMLElement, private val text: HTMLPreElement) {

    companion object {
        val shlexRegex =
            """"([^"\\]+|\\.)+"|([^ "'\\]+|\\.)+|'([^'\\]+|\\.)+'""".toRegex()

        fun createFor(element: HTMLElement): KConsole {
            val text = element.append.pre()
            element.classList.add(Styles.consoleClass)
            val console = KConsole(element, text)
            document.body!!.onkeydown = console::keydown
            console.addLine("Starting up terminal.")
            console.rerender()
            return console
        }
    }

    val lines = mutableListOf<String>()

    var input: String = ""

    fun addLines(newLines: List<String>) {
        lines.addAll(newLines)
    }

    fun addMultilineText(text: String) {
        addLines(text.split("\n"))
    }

    fun addLine(line: String) {
        lines.add(line)
        scrollDown()
    }

    fun rerender() {
        val view = lines.joinToString(separator = "\n") + "\n${'$'} $input"
        text.innerText = view
    }

    fun registerCommand(command: Command) {
        command.aliases.forEach {
            commands[it] = command
        }
        commands[command.name] = command
    }

    val commands = mutableMapOf<String, Command>()

    fun scrollDown() {} // TODO scroooooll

    fun executeCommand(command: String) {
        val parts = shlex(command)
        if (parts.isNullOrEmpty()) {
            addLine("Syntax Error")
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
        commandThing.run(this, command, arguments)
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
        rerender()
    }
}
