package moe.nea89.website

interface Command {
    val name: String
    val aliases: Set<String>
    fun run(console: KConsole, name: String, args: List<String>)
}