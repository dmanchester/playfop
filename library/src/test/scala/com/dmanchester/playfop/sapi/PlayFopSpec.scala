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
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.xmlgraphics.util.MimeConstants
import org.specs2.mutable.Specification

import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.stream.StreamSource
import play.twirl.api.Xml
import play.twirl.api.XmlFormat

class PlayFopSpec extends Specification {

  val PdfText = "Hello there"
  val PdfVersion = "1.5"
  val PdfAuthor = "PlayFopSpec"

  val Xslfo = TestHelpers.wrapInXslfoDocument(PdfText)

  val FopConfig = TestHelpers.fopConfig(PdfVersion)

  val FOUserAgentBlock = { foUserAgent: FOUserAgent =>
    foUserAgent.setAuthor(PdfAuthor)
  }

  "process(xslfo, outputFormat)" should {
    "render the XSL-FO in the chosen format" in {

      val pdfBytes = PlayFop.process(Xslfo, MimeConstants.MIME_PDF)

      TestHelpers.textFromPDFBytes(pdfBytes) must beEqualTo(PdfText)
    }
  }

  "process(xslfo, outputFormat, fopConfig)" should {
    "render the XSL-FO in the chosen format, applying the FOP configuration" in {

      val pdfBytes = PlayFop.process(Xslfo, MimeConstants.MIME_PDF, FopConfig)

      TestHelpers.textFromPDFBytes(pdfBytes) must beEqualTo(PdfText)
      TestHelpers.versionFromPDFBytes(pdfBytes) must beEqualTo(PdfVersion)
    }
  }

  "process(xslfo, outputFormat, foUserAgentBlock)" should {
    "render the XSL-FO in the chosen format, applying the FOUserAgent block" in {

      val pdfBytes = PlayFop.process(Xslfo, MimeConstants.MIME_PDF, FOUserAgentBlock)

      TestHelpers.textFromPDFBytes(pdfBytes) must beEqualTo(PdfText)
      TestHelpers.authorFromPDFBytes(pdfBytes) must beEqualTo(PdfAuthor)
    }
  }

  "process(xslfo, outputFormat, fopConfig, foUserAgentBlock)" should {
    "render the XSL-FO in the chosen format, applying the FOP configuration and the FOUserAgent block" in {

      val pdfBytes = PlayFop.process(Xslfo, MimeConstants.MIME_PDF, FopConfig, FOUserAgentBlock)

      TestHelpers.textFromPDFBytes(pdfBytes) must beEqualTo(PdfText)
      TestHelpers.versionFromPDFBytes(pdfBytes) must beEqualTo(PdfVersion)
      TestHelpers.authorFromPDFBytes(pdfBytes) must beEqualTo(PdfAuthor)
    }
  }

  "process(xslfo, outputFormat, Some(fopConfig), Some(foUserAgentBlock))" should {
    "render the XSL-FO in the chosen format, applying the FOP configuration and the FOUserAgent block" in {

      val pdfBytes = PlayFop.process(Xslfo, MimeConstants.MIME_PDF, Some(FopConfig), Some(FOUserAgentBlock))

      TestHelpers.textFromPDFBytes(pdfBytes) must beEqualTo(PdfText)
      TestHelpers.versionFromPDFBytes(pdfBytes) must beEqualTo(PdfVersion)
      TestHelpers.authorFromPDFBytes(pdfBytes) must beEqualTo(PdfAuthor)
    }
  }

  "newFop(outputFormat, output)" should {
    "obtain an Fop for the output format" in {

      val output = new ByteArrayOutputStream()
      val fop = PlayFop.newFop(MimeConstants.MIME_PDF, output)
      process(Xslfo, fop)

      TestHelpers.textFromPDFBytes(output.toByteArray()) must beEqualTo(PdfText)
    }
  }

  "newFop(outputFormat, fopConfig, output)" should {
    "obtain an Fop for the output format, applying the FOP configuration" in {

      val output = new ByteArrayOutputStream()
      val fop = PlayFop.newFop(MimeConstants.MIME_PDF, output, FopConfig)
      process(Xslfo, fop)

      val outputAsByteArray = output.toByteArray()
      TestHelpers.textFromPDFBytes(outputAsByteArray) must beEqualTo(PdfText)
      TestHelpers.versionFromPDFBytes(outputAsByteArray) must beEqualTo(PdfVersion)
    }
  }

  "newFop(outputFormat, foUserAgentBlock, output)" should {
    "obtain an Fop for the output format, applying the FOUserAgent block" in {

      val output = new ByteArrayOutputStream()
      val fop = PlayFop.newFop(MimeConstants.MIME_PDF, output, FOUserAgentBlock)
      process(Xslfo, fop)

      val outputAsByteArray = output.toByteArray()
      TestHelpers.textFromPDFBytes(outputAsByteArray) must beEqualTo(PdfText)
      TestHelpers.authorFromPDFBytes(outputAsByteArray) must beEqualTo(PdfAuthor)
    }
  }

  "newFop(outputFormat, fopConfig, foUserAgentBlock, output)" should {
    "obtain an Fop for the output format, applying the FOP configuration and the FOUserAgent block" in {

      val output = new ByteArrayOutputStream()
      val fop = PlayFop.newFop(MimeConstants.MIME_PDF, output, FopConfig, FOUserAgentBlock)
      process(Xslfo, fop)

      val outputAsByteArray = output.toByteArray()
      TestHelpers.textFromPDFBytes(outputAsByteArray) must beEqualTo(PdfText)
      TestHelpers.versionFromPDFBytes(outputAsByteArray) must beEqualTo(PdfVersion)
      TestHelpers.authorFromPDFBytes(outputAsByteArray) must beEqualTo(PdfAuthor)
    }
  }

  "newFop(outputFormat, Some(fopConfig), Some(foUserAgentBlock), output)" should {
    "obtain an Fop for the output format, applying the FOP configuration and the FOUserAgent block" in {

      val output = new ByteArrayOutputStream()
      val fop = PlayFop.newFop(MimeConstants.MIME_PDF, output, Some(FopConfig), Some(FOUserAgentBlock))
      process(Xslfo, fop)

      val outputAsByteArray = output.toByteArray()
      TestHelpers.textFromPDFBytes(outputAsByteArray) must beEqualTo(PdfText)
      TestHelpers.versionFromPDFBytes(outputAsByteArray) must beEqualTo(PdfVersion)
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
}