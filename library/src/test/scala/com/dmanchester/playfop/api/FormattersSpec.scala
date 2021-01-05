package com.dmanchester.playfop.api

import org.specs2.mutable.Specification

class FormattersSpec extends Specification {

  private val NBSP = "\u00A0"  // non-breaking space
  private val CRLF = "\r\n"
  private val LF = "\n"

  "preserveSpaces" should {
    "replace spaces with NBSPs" in {
      Formatters.preserveSpaces(" Hello there  ") must
        beEqualTo(NBSP + "Hello" + NBSP + "there" + NBSP + NBSP)
    }
  }

  "preserveNewlinesForTwirlXml" should {
    """place each CRLF-terminated run of characters in <fo:block>...</fo:block>,
      |represent non-trailing standalone CRLFs as <fo:block>NBSP</fo:block>,
      |disregard trailing CRLFs, and properly represent XML special
      |characters""".stripMargin in {
      Formatters.preserveNewlinesForTwirlXml(
        CRLF + "1 < 2" + CRLF + "4 > 3" + CRLF + CRLF + "5 = 5" + CRLF + CRLF
      ).body must beEqualTo(
        "<fo:block>\u00A0</fo:block><fo:block>1 &lt; 2</fo:block>" +
        "<fo:block>4 &gt; 3</fo:block><fo:block>\u00A0</fo:block>" +
        "<fo:block>5 = 5</fo:block>"
      )
    }

    "handle LFs the same as CRLFs" in {
      Formatters.preserveNewlinesForTwirlXml(
        "para 1" + LF + "para 2"
      ).body must beEqualTo(
        "<fo:block>para 1</fo:block><fo:block>para 2</fo:block>"
      )
    }

    "consider empty-string input as a standalone newline" in {
      Formatters.preserveNewlinesForTwirlXml(
        ""
      ).body must beEqualTo(
        "<fo:block>\u00A0</fo:block>"
      )
    }
  }

  "preserveNewlinesForScalaXml" should {
    """place each CRLF-terminated run of characters in <fo:block>...</fo:block>,
      |represent non-trailing standalone CRLFs as <fo:block>NBSP</fo:block>,
      |disregard trailing CRLFs, and properly represent XML special
      |characters""".stripMargin in {
      Formatters.preserveNewlinesForScalaXml(
        CRLF + "1 < 2" + CRLF + "4 > 3" + CRLF + CRLF + "5 = 5" + CRLF + CRLF
      ) must beEqualTo(
        Seq(
          <fo:block>{NBSP}</fo:block>,
          <fo:block>{"1 < 2"}</fo:block>,  // element value written as expression because "<" requires escaping
          <fo:block>{"4 > 3"}</fo:block>,  // same with ">"
          <fo:block>{NBSP}</fo:block>,
          <fo:block>5 = 5</fo:block>
        )
      )
    }

    "handle LFs the same as CRLFs" in {
      Formatters.preserveNewlinesForScalaXml(
        "para 1" + LF + "para 2"
      ) must beEqualTo(
        Seq(
          <fo:block>para 1</fo:block>,
          <fo:block>para 2</fo:block>
        )
      )
    }

    "consider empty-string input as a standalone newline" in {
      Formatters.preserveNewlinesForScalaXml(
        ""
      ) must beEqualTo(
        Seq(
          <fo:block>{NBSP}</fo:block>
        )
      )
    }
  }

  "preserveNewlinesForStringXml" should {
    """place each CRLF-terminated run of characters in <fo:block>...</fo:block>,
      |represent non-trailing standalone CRLFs as <fo:block>NBSP</fo:block>,
      |disregard trailing CRLFs, and properly represent XML special
      |characters""".stripMargin in {
      Formatters.preserveNewlinesForStringXml(
        CRLF + "1 < 2" + CRLF + "4 > 3" + CRLF + CRLF + "5 = 5" + CRLF + CRLF
      ) must beEqualTo(
        "<fo:block>\u00A0</fo:block><fo:block>1 &lt; 2</fo:block>" +
        "<fo:block>4 &gt; 3</fo:block><fo:block>\u00A0</fo:block>" +
        "<fo:block>5 = 5</fo:block>"
      )
    }

    "handle LFs the same as CRLFs" in {
      Formatters.preserveNewlinesForStringXml(
        "para 1" + LF + "para 2"
      ) must beEqualTo(
        "<fo:block>para 1</fo:block><fo:block>para 2</fo:block>"
      )
    }

    "consider empty-string input as a standalone newline" in {
      Formatters.preserveNewlinesForStringXml(
        ""
      ) must beEqualTo(
        "<fo:block>\u00A0</fo:block>"
      )
    }
  }
}