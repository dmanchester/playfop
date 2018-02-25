package com.dmanchester.playfop.sapi.userguide

import org.apache.fop.apps.FOUserAgent
import org.apache.xmlgraphics.util.MimeConstants
import org.specs2.mutable.Specification

import com.dmanchester.playfop.TestHelpers
import com.dmanchester.playfop.playFopBlock

class UserGuideCodeSamplesSpec extends Specification {

  "This code sample" should {
    "render the XSL-FO as PNG" in new playFopBlock {

// BEGIN Simple Scala processing sample
// IMPORTANT: If following line is changed, UserGuide.scalatex must be changed
// in kind!
val png: Array[Byte] = playFop.processTwirlXml(
  views.xml.someTwirlTemplate.render("Hello world."),
  MimeConstants.MIME_PNG
)
// END Simple Scala processing sample

      png.length must beGreaterThan(3000)  // If PNG generated correctly, it should be a little larger than 3K.
    }
  }

  "This code sample" should {
    "render the XSL-FO as PDF, auto-detecting fonts and applying the FOUserAgent block" in new playFopBlock {

      val xslfo = TestHelpers.wrapInStringXmlDocument("Hello again.")

// BEGIN Complex Scala processing sample
// IMPORTANT: If following line is changed, UserGuide.scalatex must be changed
// in kind!
val myFOUserAgentBlock = { foUserAgent: FOUserAgent =>
  foUserAgent.setAuthor("PlayFOP Sample Code")
}

val pdf: Array[Byte] = playFop.processStringXml(
  xslfo,
  MimeConstants.MIME_PDF,
  autoDetectFontsForPDF = true,
  foUserAgentBlock = myFOUserAgentBlock
)
// END Complex Scala processing sample

      TestHelpers.textFromPDFBytes(pdf) must beEqualTo("Hello again.")  // for readability of code sample, we don't declare a constant for this String or the next one
      TestHelpers.authorFromPDFBytes(pdf) must beEqualTo("PlayFOP Sample Code")
      // We don't bother with an assertion to confirm autoDetectFontsForPDF was
      // used. The main tests verify that behavior.
    }
  }

  "This code sample" should {
    "render the XSL-FO as PDF, preserving whitespace" in new playFopBlock {

      val text = "H e l l o\n\n w o r l d"

// BEGIN Formatters sample
// IMPORTANT: If following line is changed, UserGuide.scalatex must be changed
// in kind!
import com.dmanchester.playfop.api.Formatters

val formattedText = Formatters.preserveNewlinesForScalaXml(
  Formatters.preserveSpaces(text)
)

val xslfo =
<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <fo:layout-master-set>
    <fo:simple-page-master master-name="label">
      <fo:region-body region-name="xsl-region-body"/>
    </fo:simple-page-master>
  </fo:layout-master-set>
  <fo:page-sequence master-reference="label">
    <fo:flow flow-name="xsl-region-body">
      <fo:block font-family="Courier" font-size="24pt">{formattedText}</fo:block>
    </fo:flow>
  </fo:page-sequence>
</fo:root>
// END Formatters sample

      val pdf: Array[Byte] = playFop.processScalaXml(
        xslfo,
        MimeConstants.MIME_PDF
      )

      TestHelpers.textFromPDFBytes(pdf) must beEqualTo("H e l l o\n \n w o r l d")  // second line (which is blank) renders with an extraneous space, which is acceptable
    }
  }
}
