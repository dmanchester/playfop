package com.dmanchester.playfop.api

import scala.xml.Node
import scala.xml.Utility

import play.twirl.api.XmlFormat

/** Miscellaneous methods that may be useful in formatting text before it is
  * included in XSL-FO and rendered by Apache FOP.
  */
object Formatters {

  private val NBSP = "\u00A0"  // non-breaking space
  private val CRLF = "\r\n"
  private val LF = "\n"

  /** Preserves "regular" spaces (U+0020) by replacing them with no-break
    * ones (U+00A0).
    *
    * @param text
    * @return `text`, reflecting the replacements
    */
  def preserveSpaces(text: String): String = {
    text.replace(" ", NBSP)
  }

  /** Preserves newlines for use in [[https://www.playframework.com/documentation/2.6.x/ScalaTemplates Twirl]]
    * XML. Wraps each newline-terminated run of characters within `text` in
    * `<fo:block>`...`</fo:block>`. If there are standalone newlines, and if
    * they are not at the end of `text`, represents them with an `<fo:block>`
    * that renders as a blank line.
    *
    * Disregards newlines at the end of `text`.
    *
    * @param text
    * @return a Twirl `[[https://www.playframework.com/documentation/2.6.x/api/scala/index.html#play.twirl.api.Xml Xml]]`
    * instance reflecting the newline wrapping
    */
  def preserveNewlinesForTwirlXml(text: String): play.twirl.api.Xml = {

    preserveNewlines(text, XmlFormat.empty, { (foBlocks: play.twirl.api.Xml, blockValue) =>

      XmlFormat.fill(
          foBlocks ::
          XmlFormat.raw("<fo:block>") ::
          XmlFormat.escape(blockValue) ::
          XmlFormat.raw("</fo:block>") :: Nil)
    })
  }

  /** Preserves newlines for use in [[https://github.com/scala/scala-xml scala-xml]]
    * types. Wraps each newline-terminated run of characters within `text` in
    * `<fo:block>`...`</fo:block>`. If there are standalone newlines, and if
    * they are not at the end of `text`, represents them with an `<fo:block>`
    * that renders as a blank line.
    *
    * Disregards newlines at the end of `text`.
    *
    * @param text
    * @return a sequence of `Node` reflecting the newline wrapping
    */
  def preserveNewlinesForScalaXml(text: String): Seq[Node] = {

    preserveNewlines(text, Seq.empty[Node], { (foBlocks: Seq[Node], blockValue) =>

      foBlocks :+ <fo:block>{blockValue}</fo:block>
    })
  }

  /** Preserves newlines for use in `String`-based XML. XML-escapes `text` and
    * wraps each newline-terminated run of characters in
    * `<fo:block>`...`</fo:block>`. If there are standalone newlines, and if
    * they are not at the end of `text`, represents them with an `<fo:block>`
    * that renders as a blank line.
    *
    * Disregards newlines at the end of `text`.
    *
    * @param text
    * @return an XML-escaped `String` reflecting the newline wrapping
    */
  def preserveNewlinesForStringXml(text: String): String = {

    preserveNewlines(text, "" /* empty string */, { (foBlocks: String, blockValue) =>

      foBlocks + "<fo:block>" + Utility.escape(blockValue) + "</fo:block>"
    })
  }

  private def preserveNewlines[T](text: String, emptyFOBlocks: T, appendFOBlock: ((T, String) => T)): T = {

    val paragraphs = text.split(CRLF + "|" + LF)

    paragraphs.foldLeft(emptyFOBlocks) { (foBlocks, paragraph) =>

      val blockValue = if (paragraph.isEmpty())
        NBSP
      else
        paragraph

      appendFOBlock(foBlocks, blockValue)
    }
  }
}
