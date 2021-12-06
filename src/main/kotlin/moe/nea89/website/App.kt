package moe.nea89.website

import com.bnorm.react.RFunction
import react.RBuilder
import react.dom.li
import react.dom.nav
import react.dom.ul


@RFunction
fun RBuilder.App() {
    Navigation()
}

@RFunction
fun RBuilder.Navigation() {
    nav {
        ul {
            li { +"Hehe" }
            li { +"Hihi" }
            li { +"Hoho" }
            li { +"Haha" }
            li { +"Huhu" }
        }
    }
}
