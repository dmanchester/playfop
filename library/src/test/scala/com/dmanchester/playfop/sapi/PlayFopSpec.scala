package com.dmanchester.playfop.sapi

import com.dmanchester.playfop.TestHelpers

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.StringReader
import java.io.StringWriter

import scala.xml.Elem
import scala.xml.XML

import org.apache.fop.apps.FOUserAgent
import org.apache.fop.apps.Fop
import org.apache.fop.fo.FOTreeBuilder
import org.apache.xmlgraphics.util.MimeConstants
import org.specs2.mutable.Specification

import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.stream.StreamSource

import play.twirl.api.Xml
import play.twirl.api.XmlFormat

import scala.collection.JavaConverters._

class PlayFopSpec extends Specification {

  val PdfText = "Hello there"
  val PdfAuthor = "PlayFopSpec"

  val FOUserAgentBlock = { foUserAgent: FOUserAgent =>
    foUserAgent.setAuthor(PdfAuthor)
  }

  "process(xslfo, outputFormat)" should {
    "render the XSL-FO in the chosen format" in {

      val xslfo = TestHelpers.wrapInXslfoDocument(PdfText)
      val pdfBytes = PlayFop.process(xslfo, MimeConstants.MIME_PDF)

      TestHelpers.textFromPDFBytes(pdfBytes) must beEqualTo(PdfText)
    }
  }

  "process(xslfo, outputFormat, autoDetectFontsForPDF)" should {
    "render the XSL-FO with the chosen font" in {

      // To confirm a specific font is used, we choose one outside the PDF
      // format's base 14 fonts. (If we were to choose a base font, it could
      // potentially get used by default, even if process() weren't rendering
      // the XSL-FO correctly.)

      val fontFamily = chooseFontFamilyOutsideBase14WithSingleWordName()
      val xslfo = TestHelpers.wrapInXslfoDocument(PdfText, Some(fontFamily))

      val pdfBytes = PlayFop.process(xslfo, MimeConstants.MIME_PDF, autoDetectFontsForPDF = true)

      TestHelpers.fontsFromPDFBytes(pdfBytes) must containMatch(fontFamily)
    }
  }

  "process(xslfo, outputFormat, foUserAgentBlock)" should {
    "render the XSL-FO in the chosen format, applying the FOUserAgent block" in {

      val xslfo = TestHelpers.wrapInXslfoDocument(PdfText)
      val pdfBytes = PlayFop.process(xslfo, MimeConstants.MIME_PDF, foUserAgentBlock = FOUserAgentBlock)

      TestHelpers.textFromPDFBytes(pdfBytes) must beEqualTo(PdfText)
      TestHelpers.authorFromPDFBytes(pdfBytes) must beEqualTo(PdfAuthor)
    }
  }

  "newFop(outputFormat, output)" should {
    "obtain an Fop for the output format" in {

      val output = new ByteArrayOutputStream()
      val fop = PlayFop.newFop(MimeConstants.MIME_PDF, output)
      val xslfo = TestHelpers.wrapInXslfoDocument(PdfText)

      process(xslfo, fop)

      TestHelpers.textFromPDFBytes(output.toByteArray()) must beEqualTo(PdfText)
    }
  }

  "newFop(outputFormat, fopConfig, autoDetectFontsForPDF)" should {
    "obtain an Fop that respects the font choice" in {

      val output = new ByteArrayOutputStream()
      val fop = PlayFop.newFop(MimeConstants.MIME_PDF, output, autoDetectFontsForPDF = true)
      val fontFamily = chooseFontFamilyOutsideBase14WithSingleWordName()
      val xslfo = TestHelpers.wrapInXslfoDocument(PdfText, Some(fontFamily))

      process(xslfo, fop)

      TestHelpers.fontsFromPDFBytes(output.toByteArray()) must containMatch(fontFamily)
    }
  }

  "newFop(outputFormat, output, foUserAgentBlock)" should {
    "obtain an Fop for the output format, applying the FOUserAgent block" in {

      val output = new ByteArrayOutputStream()
      val fop = PlayFop.newFop(MimeConstants.MIME_PDF, output, foUserAgentBlock = FOUserAgentBlock)
      val xslfo = TestHelpers.wrapInXslfoDocument(PdfText)

      process(xslfo, fop)

      val outputAsByteArray = output.toByteArray()
      TestHelpers.textFromPDFBytes(outputAsByteArray) must beEqualTo(PdfText)
      TestHelpers.authorFromPDFBytes(outputAsByteArray) must beEqualTo(PdfAuthor)
    }
  }

  /** Process the XSL-FO with the Fop. Mutates the Fop with the result.
    *
    * Due to an Fop's mutability, should only be invoked *once* on an Fop!
    *
    * @param xslfo the XSL-FO to process
    * @param fop the Fop with which to process the XSL-FO
    */
  private def process(xslfo: Xml, fop: Fop) = {

    val transformer = TransformerFactory.newInstance().newTransformer()

    val source = new StreamSource(new StringReader(xslfo.body))

    val result = new SAXResult(fop.getDefaultHandler())

    transformer.transform(source, result)
  }

  private def chooseFontFamilyOutsideBase14WithSingleWordName() = {

    val fontFamilies = getFontFamilies()

    fontFamilies.find { fontFamily =>
      "Times|Courier|Helvetica|Symbol|Zapf".r.findFirstIn(fontFamily).isEmpty &&
        "^\\w*$".r.findFirstIn(fontFamily).isDefined
    }.get
    // It would be highly unusual for there to be no font families that meet
    // both criteria. So, the previous line throwing an Exception in the "None"
    // case is actually desirable.
  }

  private def getFontFamilies() = {

    val fop = PlayFop.newFop(MimeConstants.MIME_PDF, new ByteArrayOutputStream(), autoDetectFontsForPDF = true)

    val fontInfo = fop.getDefaultHandler().asInstanceOf[FOTreeBuilder].getEventHandler().getFontInfo()

    val typefaces = fontInfo.getFonts().values().asScala

    typefaces.map(_.getFullName()).toSeq
  }
}