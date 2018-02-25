package com.dmanchester.playfop.sinternal

import java.io.ByteArrayOutputStream
import java.io.StringReader

import scala.collection.JavaConverters.collectionAsScalaIterableConverter

import org.apache.fop.apps.FOUserAgent
import org.apache.fop.apps.Fop
import org.apache.fop.fo.FOTreeBuilder
import org.apache.xmlgraphics.util.MimeConstants
import org.specs2.mutable.Specification

import com.dmanchester.playfop.TestHelpers
import com.dmanchester.playfop.sapi.PlayFop
import com.dmanchester.playfop.playFopBlock

import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.stream.StreamSource
import play.twirl.api.Xml

class PlayFopImplSpec extends Specification {

  private val PdfText = "Hello there"
  private val PdfAuthor = "PlayFopSpec"

  private val FOUserAgentBlock = { foUserAgent: FOUserAgent =>
    foUserAgent.setAuthor(PdfAuthor)
  }

  "processTwirlXml(xslfo, outputFormat)" should {
    "render the XSL-FO in the chosen format" in new playFopBlock {

      val xslfo = TestHelpers.wrapInTwirlXmlDocument(PdfText)
      val pdfBytes = playFop.processTwirlXml(xslfo, MimeConstants.MIME_PDF)

      TestHelpers.textFromPDFBytes(pdfBytes) must beEqualTo(PdfText)
    }
  }

  "processTwirlXml(xslfo, outputFormat, autoDetectFontsForPDF)" should {
    "render the XSL-FO with the chosen font" in new playFopBlock {

      // To confirm a specific font is used, we choose one outside the PDF
      // format's base 14 fonts. (If we were to choose a base font, it could
      // potentially get used by default, even if process() weren't rendering
      // the XSL-FO correctly.)

      val fontFamily = chooseFontFamilyOutsideBase14WithSingleWordName(playFop)
      val xslfo = TestHelpers.wrapInTwirlXmlDocument(PdfText, Some(fontFamily))

      val pdfBytes = playFop.processTwirlXml(xslfo, MimeConstants.MIME_PDF, autoDetectFontsForPDF = true)

      TestHelpers.fontsFromPDFBytes(pdfBytes) must containMatch(fontFamily)
    }
  }

  "processTwirlXml(xslfo, outputFormat, foUserAgentBlock)" should {
    "render the XSL-FO in the chosen format, applying the FOUserAgent block" in new playFopBlock {

      val xslfo = TestHelpers.wrapInTwirlXmlDocument(PdfText)
      val pdfBytes = playFop.processTwirlXml(xslfo, MimeConstants.MIME_PDF, foUserAgentBlock = FOUserAgentBlock)

      TestHelpers.textFromPDFBytes(pdfBytes) must beEqualTo(PdfText)
      TestHelpers.authorFromPDFBytes(pdfBytes) must beEqualTo(PdfAuthor)
    }
  }

  "processScalaXml(xslfo, outputFormat)" should {
    "render the XSL-FO in the chosen format" in new playFopBlock {

      val xslfo = TestHelpers.wrapInScalaXmlDocument(PdfText)
      val pdfBytes = playFop.processScalaXml(xslfo, MimeConstants.MIME_PDF)

      TestHelpers.textFromPDFBytes(pdfBytes) must beEqualTo(PdfText)
    }
  }

  "processScalaXml(xslfo, outputFormat, autoDetectFontsForPDF)" should {
    "render the XSL-FO with the chosen font" in new playFopBlock {

      // To confirm a specific font is used, we choose one outside the PDF
      // format's base 14 fonts. (If we were to choose a base font, it could
      // potentially get used by default, even if process() weren't rendering
      // the XSL-FO correctly.)

      val fontFamily = chooseFontFamilyOutsideBase14WithSingleWordName(playFop)
      val xslfo = TestHelpers.wrapInScalaXmlDocument(PdfText, Some(fontFamily))

      val pdfBytes = playFop.processScalaXml(xslfo, MimeConstants.MIME_PDF, autoDetectFontsForPDF = true)

      TestHelpers.fontsFromPDFBytes(pdfBytes) must containMatch(fontFamily)
    }
  }

  "processScalaXml(xslfo, outputFormat, foUserAgentBlock)" should {
    "render the XSL-FO in the chosen format, applying the FOUserAgent block" in new playFopBlock {

      val xslfo = TestHelpers.wrapInScalaXmlDocument(PdfText)
      val pdfBytes = playFop.processScalaXml(xslfo, MimeConstants.MIME_PDF, foUserAgentBlock = FOUserAgentBlock)

      TestHelpers.textFromPDFBytes(pdfBytes) must beEqualTo(PdfText)
      TestHelpers.authorFromPDFBytes(pdfBytes) must beEqualTo(PdfAuthor)
    }
  }

  "processStringXml(xslfo, outputFormat)" should {
    "render the XSL-FO in the chosen format" in new playFopBlock {

      val xslfo = TestHelpers.wrapInStringXmlDocument(PdfText)
      val pdfBytes = playFop.processStringXml(xslfo, MimeConstants.MIME_PDF)

      TestHelpers.textFromPDFBytes(pdfBytes) must beEqualTo(PdfText)
    }
  }

  "processStringXml(xslfo, outputFormat, autoDetectFontsForPDF)" should {
    "render the XSL-FO with the chosen font" in new playFopBlock {

      // To confirm a specific font is used, we choose one outside the PDF
      // format's base 14 fonts. (If we were to choose a base font, it could
      // potentially get used by default, even if process() weren't rendering
      // the XSL-FO correctly.)

      val fontFamily = chooseFontFamilyOutsideBase14WithSingleWordName(playFop)
      val xslfo = TestHelpers.wrapInStringXmlDocument(PdfText, Some(fontFamily))

      val pdfBytes = playFop.processStringXml(xslfo, MimeConstants.MIME_PDF, autoDetectFontsForPDF = true)

      TestHelpers.fontsFromPDFBytes(pdfBytes) must containMatch(fontFamily)
    }
  }

  "processStringXml(xslfo, outputFormat, foUserAgentBlock)" should {
    "render the XSL-FO in the chosen format, applying the FOUserAgent block" in new playFopBlock {

      val xslfo = TestHelpers.wrapInStringXmlDocument(PdfText)
      val pdfBytes = playFop.processStringXml(xslfo, MimeConstants.MIME_PDF, foUserAgentBlock = FOUserAgentBlock)

      TestHelpers.textFromPDFBytes(pdfBytes) must beEqualTo(PdfText)
      TestHelpers.authorFromPDFBytes(pdfBytes) must beEqualTo(PdfAuthor)
    }
  }

  "newFop(outputFormat, output)" should {
    "obtain an Fop for the output format" in new playFopBlock {

      val output = new ByteArrayOutputStream()
      val fop = playFop.newFop(MimeConstants.MIME_PDF, output)
      val xslfo = TestHelpers.wrapInTwirlXmlDocument(PdfText)

      process(xslfo, fop)

      TestHelpers.textFromPDFBytes(output.toByteArray()) must beEqualTo(PdfText)
    }
  }

  "newFop(outputFormat, fopConfig, autoDetectFontsForPDF)" should {
    "obtain an Fop that respects the font choice" in new playFopBlock {

      val output = new ByteArrayOutputStream()
      val fop = playFop.newFop(MimeConstants.MIME_PDF, output, autoDetectFontsForPDF = true)
      val fontFamily = chooseFontFamilyOutsideBase14WithSingleWordName(playFop)
      val xslfo = TestHelpers.wrapInTwirlXmlDocument(PdfText, Some(fontFamily))

      process(xslfo, fop)

      TestHelpers.fontsFromPDFBytes(output.toByteArray()) must containMatch(fontFamily)
    }
  }

  "newFop(outputFormat, output, foUserAgentBlock)" should {
    "obtain an Fop for the output format, applying the FOUserAgent block" in new playFopBlock {

      val output = new ByteArrayOutputStream()
      val fop = playFop.newFop(MimeConstants.MIME_PDF, output, foUserAgentBlock = FOUserAgentBlock)
      val xslfo = TestHelpers.wrapInTwirlXmlDocument(PdfText)

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

  private def chooseFontFamilyOutsideBase14WithSingleWordName(playFop: PlayFop) = {

    val fontFamilies = getFontFamilies(playFop)

    fontFamilies.find { fontFamily =>
      "Times|Courier|Helvetica|Symbol|Zapf".r.findFirstIn(fontFamily).isEmpty &&
        "^\\w*$".r.findFirstIn(fontFamily).isDefined
    }.get
    // It would be highly unusual for there to be no font families that meet
    // both criteria. So, the previous line throwing an Exception in the "None"
    // case is actually desirable.
  }

  private def getFontFamilies(playFop: PlayFop) = {

    val fop = playFop.newFop(MimeConstants.MIME_PDF, new ByteArrayOutputStream(), autoDetectFontsForPDF = true)

    val fontInfo = fop.getDefaultHandler().asInstanceOf[FOTreeBuilder].getEventHandler().getFontInfo()

    val typefaces = fontInfo.getFonts().values().asScala

    typefaces.map(_.getFullName()).toSeq
  }
}