package moe.nea89.website

import kotlinx.css.*
import styled.StyleSheet


object Styles : StyleSheet("Styles") {
    val consoleClass = "Console"

    val bgColor = Color("#123456")
    val fgColor = Color("#efefef")
    val comicMono = "\"Comic Mono\", monospace"

    val global by css {
        root {
            margin(0.px)
            boxSizing = BoxSizing.borderBox
        }
        body {
            width = 100.pct
            height = 100.pct
            backgroundColor = bgColor
            color = fgColor
            fontFamily = comicMono
        }
        ".$consoleClass" {
            width = 100.pct
            height = 100.pct
        }
    }
}