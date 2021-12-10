package moe.nea89.website

import kotlinx.css.*
import styled.StyleSheet


object Styles : StyleSheet("Styles") {
    val consoleClass = "Console"

    val bgColor = CustomColor.BLACK.color
    val fgColor = CustomColor.WHITE.color
    val comicMono = "\"Comic Mono\", monospace"

    val global by css {
        "*" {
            padding(0.px)
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
            pre {
                fontFamily = comicMono
            }
        }
    }
}