package moe.nea89.website

import kotlinext.js.require
import kotlinx.browser.document
import kotlinx.html.dom.append
import kotlinx.html.js.div
import styled.injectGlobal

fun main() {
    require("@fontsource/comic-mono/index.css")
    injectGlobal(Styles.global)
    val root = document.body!!.append.div()
    val console = KConsole.createFor(root)
    console.registerCommand(object : Command {
        override val name: String = "dick"
        override val aliases: Set<String> = setOf("cock")
        override fun run(console: KConsole, name: String, args: List<String>) {
            console.addMultilineText("Hehe")
        }
    })
    console.registerCommand(object : Command {
        override val name: String = "booob"
        override val aliases: Set<String> = setOf("boob")
        override fun run(console: KConsole, name: String, args: List<String>) {
            console.addMultilineText(boobs)
        }
    })
}