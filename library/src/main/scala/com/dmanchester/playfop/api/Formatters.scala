package com.dmanchester.playfop.api

import play.twirl.api.Xml
import play.twirl.api.XmlFormat

/** Miscellaneous methods that may be useful in formatting text before it is
  * included in an XSL-FO template and rendered by Apache FOP.
  */
object Formatters {

  private def NBSP = '\u00A0'  // non-breaking space
  private def NBSPAsXml = "&#xa0;"
  private def CRLF = "\r\n"
  private def LF = "\n"

  /** Replaces each "regular" space (U+0020) with a no-break one (U+00A0).
    *
    * @param text
    * @return `text`, reflecting the replacements
    */
  def makeSpacesNonBreaking(text: String): String = {
    text.replace(' ', NBSP);
  }

  /** Wraps each newline-terminated run of characters within `text` in
    * `<fo:block>`...`</fo:block>`. If there are standalone newlines, and if
    * they are not at the end of `text`, represents them with an `<fo:block>`
    * that renders as a blank line.
    *
    * Disregards newlines at the end of `text`.
    *
    * @param text
    * @return a Play Twirl `[[https://www.playframework.com/documentation/2.6.x/api/scala/index.html#play.twirl.api.Xml Xml]]`
    * instance reflecting the newline wrapping
    */
  def makeNewlinesIntoFOBlocks(text: String): play.twirl.api.Xml = {

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