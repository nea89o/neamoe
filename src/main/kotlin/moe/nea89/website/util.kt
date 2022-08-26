package moe.nea89.website

fun <T> dyn(init: T.() -> Unit): dynamic = js("{}").also(init)



