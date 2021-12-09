package moe.nea89.website

data class Command(
    val name: String,
    val aliases: Set<String>,
    val runner: suspend ShellExecutionContext.() -> Unit,
)


fun command(name: String, vararg aliases: String, block: suspend ShellExecutionContext. () -> Unit) =
    Command(name, aliases.toSet(), block)