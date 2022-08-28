package moe.nea89.website

import kotlinx.css.*
import kotlinx.css.properties.IterationCount
import kotlinx.css.properties.Timing
import kotlinx.css.properties.s
import styled.StyleSheet
import styled.animation


object Styles : StyleSheet("DefaultConsoleStyles") {
    val consoleClass = "Console"
    val mobileFocusInput = "mobileFocusInput"
    val promptClass = "prompt"

    val bgColor = CustomColor.BLACK.color
    val fgColor = CustomColor.WHITE.color
    val monospacedFont = "monospace"

    val global by css {
        "*" {
            padding(0.px)
            margin(0.px)
            boxSizing = BoxSizing.borderBox
        }

        ".$mobileFocusInput" {
            width = 0.px
            height = 0.px
            opacity = 0
        }
        ".$promptClass" {
            width = LinearDimension.fitContent
            borderRightColor = fgColor
            borderRightWidth = 2.px
            paddingRight = 2.px
            borderRightStyle = BorderStyle.solid
            animation(1.s, Timing.stepStart, iterationCount = IterationCount.infinite) {
                0 {
                    borderRightStyle = BorderStyle.solid
                }
                50 {
                    borderRightStyle = BorderStyle.none
                }
            }
        }

        ".$consoleClass" {
            width = 100.pct
            height = 100.pct
            backgroundColor = bgColor
            color = fgColor
            fontFamily = monospacedFont
            width = 100.pct
            height = 100.pct
            fontFamily = monospacedFont
        }
    }
}