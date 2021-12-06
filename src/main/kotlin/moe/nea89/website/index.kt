package moe.nea89.website

import kotlinx.browser.document
import react.dom.render
import kotlinext.js.require

fun main() {
    require("@fontsource/comic-mono/index.css")
    render(document.getElementById("root") ?: throw RuntimeException("Could not find root element")) { App() }
}