package moe.nea89.website

import kotlinx.css.*

enum class CustomColor(val color: Color) {
    RED(Color("#ff0022")),
    BLUE(Color("#3344ff")),
    PURPLE(Color("#DA2EC2")),
    GREEN(Color("#68DA2E"))
}

data class ColoredElement(
    val color: CustomColor,
    val text: String
)

fun red(text: String) = ColoredElement(CustomColor.RED, text)
fun blue(text: String) = ColoredElement(CustomColor.BLUE, text)
fun purple(text: String) = ColoredElement(CustomColor.PURPLE, text)
fun green(text: String) = ColoredElement(CustomColor.GREEN, text)

