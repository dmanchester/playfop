package com.dmanchester.playfop.api

import play.twirl.api.XmlFormat

/** Miscellaneous methods that may be useful in formatting text before it is
  * included in an XSL-FO template and rendered by Apache FOP.
  */
object Formatters {

  private val NBSP = '\u00A0'  // non-breaking space
  private val NBSPAsXml = "&#xa0;"
  private val CRLF = "\r\n"
  private val LF = "\n"

  /** Preserves "regular" spaces (U+0020) by replacing them with no-break
    * ones (U+00A0).
    *
    * @param text
    * @return `text`, reflecting the replacements
    */
  def preserveSpaces(text: String): String = {
    text.replace(' ', NBSP)
  }

  /** Preserves newlines by wrapping each newline-terminated run of characters
    * within `text` in `<fo:block>`...`</fo:block>`. If there are standalone
    * newlines, and if they are not at the end of `text`, represents them with
    * an `<fo:block>` that renders as a blank line.
    *
    * Disregards newlines at the end of `text`.
    *
    * @param text
    * @return a Play Twirl `[[https://www.playframework.com/documentation/2.6.x/api/scala/index.html#play.twirl.api.Xml Xml]]`
    * instance reflecting the newline wrapping
    */
  def preserveNewlinesForTwirlXml(text: String): play.twirl.api.Xml = {

    val paragraphs = text.split(CRLF + "|" + LF)

    paragraphs.foldLeft(XmlFormat.empty) { (foBlocks, paragraph) =>

      val blockValue = if (paragraph.isEmpty())
        XmlFormat.raw(NBSPAsXml)
      else
        XmlFormat.escape(paragraph)

      XmlFormat.fill(foBlocks :: XmlFormat.raw("<fo:block>") :: blockValue :: XmlFormat.raw("</fo:block>") :: Nil)
    }
  }
}
