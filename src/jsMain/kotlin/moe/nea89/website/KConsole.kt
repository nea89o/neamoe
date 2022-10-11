package moe.nea89.website

import kotlinx.browser.window
import kotlinx.browser.document
import kotlinx.dom.addClass
import kotlinx.html.InputType
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.js.input
import kotlinx.html.js.p
import kotlinx.html.js.pre
import kotlinx.html.js.span
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLParagraphElement
import org.w3c.dom.HTMLPreElement
import org.w3c.dom.events.EventType
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.addEventHandler
import styled.injectGlobal
import kotlin.collections.set


class KConsole(
    val root: HTMLElement,
    val text: HTMLPreElement,
    val prompt: HTMLElement,
    fileSystem: KFileSystem?,
) {


    private lateinit var uninjectKeyHandler: () -> Unit
    val fileAccessor = fileSystem?.let { FileAccessor(it) }
    var wholecommand = ""
    var currenthistory = 0
    var commandHistory : Array<String> = emptyArray()
    var PS1: KConsole.() -> String = { "$" }
    private lateinit var mobileInput: HTMLInputElement

    companion object {

        init {
            injectGlobal(Styles.global)
        }

        val shlexRegex =
            """"([^"\\]+|\\.)+"|([^ "'\\]+|\\.)+|'([^'\\]+|\\.)+'""".toRegex()

        fun createFor(element: HTMLElement, fileSystem: KFileSystem? = null): KConsole {
            val text = element.append.pre()
            val prompt = text.append.p()
            prompt.addClass(Styles.promptClass)
            element.classList.add(Styles.consoleClass)
            val console = KConsole(element, text, prompt, fileSystem)
            console.uninjectKeyHandler =
                document.body!!.addEventHandler(EventType("keydown"), console::keydown)
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
    var justHandledInput = false
    fun openMobileKeyboardOnTap() {
        uninjectKeyHandler()
        mobileInput = this.root.append.input(InputType.text)
        mobileInput.classList.add(Styles.mobileFocusInput)
        mobileInput.onkeyup = this::keydown
        mobileInput.oninput = {
            input += it.data
            mobileInput.value = ""
            justHandledInput = true
            rerender()
            scrollDown()
        }
        root.onclick = {
            mobileInput.focus()
        }
    }

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
            prompt.innerText = "${PS1.invoke(this)} $input"
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
        wholecommand = ""
        println("whole command:")
        for (element in parts){
            wholecommand += element + " "
        }
        println(wholecommand)
        commandHistory += wholecommand

        val arguments = parts.drop(1)
        val commandThing = commands[command]
        if (commandThing == null) {
            addLine("Unknown command")
            return
        }
        ShellExecutionContext.run(this, commandThing, command, arguments)
        scrollDown()
    }

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

    fun handleSubmit() {
        currenthistory = 0
        val toExecute = input
        addLine("${PS1.invoke(this)} $toExecute")
        input = ""
        executeCommand(toExecute)
    }

    fun keydown(event: KeyboardEvent) {
        if (event.altKey || event.metaKey) return
        if (event.ctrlKey) {
            handleControlDown(event)
            return
        }
        if (event.keyCode == 38 || event.keyCode == 40) {
            handleArrowKeys(event)
        }
        if (event.isComposing) return
        if (state != ConsoleState.SHELLPROMPT) return
        if (justHandledInput) {
            justHandledInput = false
            return
        }
        val toHandle = if (event.keyCode == 229) {
            val x = (mobileInput.selectionStart ?: 1) - 1
            val v = mobileInput.value
            addLine("X: $x, V: $v")
            if (x < 0 || x >= v.length)
                return
            mobileInput.value = ""
            v[x]
        } else event.key
        when (toHandle) {
            "Enter" -> {
                handleSubmit()
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


    fun handleControlDown(event: KeyboardEvent){
        if (event.key == "v"){
            event.preventDefault()
            window.navigator.clipboard.readText().then {
                input += it
                rerender()
                scrollDown()
            }
        }
    }
    fun handleArrowKeys(event: KeyboardEvent){
        if (event.keyCode == 40) {
            if (commandHistory.isEmpty() || currenthistory > commandHistory.size || currenthistory == 0){
                return
            }
            input = commandHistory[commandHistory.size-currenthistory]
            currenthistory -= 1
        }
        if (event.keyCode == 38) {
            if (commandHistory.isEmpty() || currenthistory == commandHistory.size){
                return
            }
            input = commandHistory[commandHistory.size-1-currenthistory]
            currenthistory += 1
        }
    }
}
