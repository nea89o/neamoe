package moe.nea89.website.test

import kotlinext.js.require
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.css.*
import kotlinx.html.dom.append
import kotlinx.html.js.div
import moe.nea89.website.*
import styled.injectGlobal

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
    val root = document.body!!.append.div()
    val console = KConsole.createFor(root, fileSystem = defaultFileSystem)
    console.text.id = "myconsole"

    val mobileNavigators = listOf(
        "webos",
        "android",
        "iphone",
        "ipad",
        "ipod",
        "blackberry",
        "iemobile",
        "opera mini"
    )

    fun isMobileBrowser() : Boolean{
        return js("'ontouchstart' in document.documentElement") as Boolean
    }

    if (window.location.search == "mobile" || (window.location.search != "desktop" && isMobileBrowser())) {
        console.openMobileKeyboardOnTap()
    }
    console.fileAccessor!!.cd("home/nea")
    injectGlobal {
        body {
            backgroundColor = Styles.bgColor.lighten(30)
        }
        ".${Styles.consoleClass}" {
            margin(LinearDimension.auto)
            fontFamily = "\"Comic Mono\", monospace"
            width = 50.vw
            height = 50.vh
            marginTop = 25.vh
            boxSizing = BoxSizing.borderBox
            backgroundClip = BackgroundClip.contentBox
            overflowY = Overflow.scroll
        }
    }
    console.addLine("Starting up terminal.")
    console.PS1 = { "${this.fileAccessor?.currentDir?.joinToString("/", "/") ?: ""} >" }
    console.rerender()
    console.registerCommand(defaultCwdCommand("cwd", "pwd"))
    console.registerCommand(defaultCdCommand("cd"))
    console.registerCommand(defaultLsCommand("ls"))
    console.registerCommand(command("color") {
        console.addLine("This is a ", red("red"), " word: ", green("1.0"), " ", blue("BLUUEEE"))
    })
    console.registerCommand(defaultCatCommand("cat"))
    console.registerCommand(command("dick", "cock") {
        console.addMultilineText("Hehe")
    })
    console.registerCommand(command("boob", "booob") {
        console.addMultilineText(boobs)
    })
}