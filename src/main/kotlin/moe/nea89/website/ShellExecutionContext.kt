package moe.nea89.website

import kotlinx.browser.window
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.*
import kotlin.time.Duration
import kotlin.time.DurationUnit

class ShellExecutionContext(
    val console: KConsole,
    val name: String,
    val args: List<String>,
) {

    suspend fun wait(duration: Duration) {
        suspendCancellableCoroutine<Unit> {
            window.setTimeout({
                it.resume(Unit)
            }, timeout = duration.toInt(DurationUnit.MILLISECONDS))
        }
    }

    suspend fun exit(): Nothing {
        suspendCancellableCoroutine<Unit> {
            it.cancel()
            console.state = KConsole.ConsoleState.SHELLPROMPT
            console.rerender()
        }
        throw RuntimeException("THIs shOULDNT EXIST")
    }

    companion object {
        fun run(
            console: KConsole, command: Command, name: String, args: List<String>
        ) {
            console.state = KConsole.ConsoleState.IN_PROGRAM
            val se = ShellExecutionContext(console, name, args)
            command.runner.createCoroutine(se, object : Continuation<Unit> {
                override val context: CoroutineContext
                    get() = EmptyCoroutineContext

                override fun resumeWith(result: Result<Unit>) {
                    console.state = KConsole.ConsoleState.SHELLPROMPT
                    console.rerender()
                }
            }).resume(Unit)
        }
    }
}
