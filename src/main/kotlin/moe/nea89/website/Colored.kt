package moe.nea89.website

import kotlinx.css.*

enum class CustomColor(val color: Color) {
    BLACK(Color("#282a39")),
    RED(Color("#ff4473")),
    BLUE(Color("#00fefc")),
    PURPLE(Color("#6064fe")),
    GREEN(Color("#4ce080")),
    WHITE(Color("#efefef")),
}

data class ColoredElement(
    val color: CustomColor,
    val text: String
)

fun red(text: String) = ColoredElement(CustomColor.RED, text)
fun blue(text: String) = ColoredElement(CustomColor.BLUE, text)
fun purple(text: String) = ColoredElement(CustomColor.PURPLE, text)
fun green(text: String) = ColoredElement(CustomColor.GREEN, text)

