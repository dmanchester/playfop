package com.dmanchester.playfop.api

import org.specs2.mutable.Specification

class FormattersSpec extends Specification {

  private def NBSP = '\u00A0'  // non-breaking space
  private def CRLF = "\r\n"
  private def LF = "\n"

  "makeSpacesNonBreaking" should {
    "replace spaces with NBSPs" in {
      Formatters.makeSpacesNonBreaking(" Hello there  ") must
        beEqualTo(NBSP + "Hello" + NBSP + "there" + NBSP + NBSP)
    }
  }

  "makeNewlinesIntoFOBlocks" should {
    """place each CRLF-terminated run of characters in <fo:block>...</fo:block>,
      |represent non-trailing standalone CRLFs as <fo:block>&#xa0;</fo:block>,
      |disregard trailing CRLFs, and properly represent XML special
      |characters""".stripMargin in {
      Formatters.makeNewlinesIntoFOBlocks(
        CRLF + "1 < 2" + CRLF + "4 > 3" + CRLF + CRLF + "5 = 5" + CRLF + CRLF
      ).body must beEqualTo(
        "<fo:block>&#xa0;</fo:block><fo:block>1 &lt; 2</fo:block>" +
        "<fo:block>4 &gt; 3</fo:block><fo:block>&#xa0;</fo:block>" +
        "<fo:block>5 = 5</fo:block>"
      )
    }

    "handle LFs the same as CRLFs" in {
      Formatters.makeNewlinesIntoFOBlocks(
        "para 1" + LF + "para 2"
      ).body must beEqualTo(
        "<fo:block>para 1</fo:block><fo:block>para 2</fo:block>"
      )
    }

    "consider empty-string input as a standalone newline" in {
      Formatters.makeNewlinesIntoFOBlocks(
        ""
      ).body must beEqualTo(
        "<fo:block>&#xa0;</fo:block>"
      )
    }
  }
}