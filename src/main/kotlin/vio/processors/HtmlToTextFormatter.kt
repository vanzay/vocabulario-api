package vio.processors

import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeVisitor

class HtmlToTextFormatter : NodeVisitor {

    private val accum = StringBuilder()

    override fun head(node: Node, depth: Int) {
        if (node is TextNode) {
            append(node.text())
        } else {
            val name = node.nodeName()
            if (name == "li") {
                append("\n * ")
            } else if (name == "dt") {
                append("  ")
            } else if (name == "hr") {
                append("\n_____\n")
            } else if (name in arrayOf("p", "h1", "h2", "h3", "h4", "h5", "tr")) {
                append("\n")
            }
        }
    }

    override fun tail(node: Node, depth: Int) {
        if (node.nodeName() in arrayOf("br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5")) {
            append("\n")
        }
    }

    private fun append(text: String) {
        if (text == " " &&
            (accum.isEmpty() || accum.substring(accum.length - 1) in arrayOf(" ", "\n"))
        ) {
            return
        }
        accum.append(text)
    }

    override fun toString(): String {
        return accum.toString()
    }
}
