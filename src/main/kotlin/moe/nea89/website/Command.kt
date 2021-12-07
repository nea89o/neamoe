package moe.nea89.website

interface Command {
    val name: String
    val aliases: Set<String>
    fun run(console: KConsole, name: String, args: List<String>)
}

data class CommandContext(val console: KConsole, val name: String, val args: List<String>)

fun command(name: String, vararg aliases: String, block: CommandContext. () -> Unit) = object : Command {
    override val name: String = name
    override val aliases: Set<String> = aliases.toSet()

    override fun run(console: KConsole, name: String, args: List<String>) = block(CommandContext(console, name, args))
}