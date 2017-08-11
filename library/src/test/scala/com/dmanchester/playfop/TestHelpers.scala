package com.dmanchester.playfop

import java.io.ByteArrayInputStream
import java.io.StringWriter

import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import scala.xml.Elem
import scala.xml.Text
import scala.xml.XML

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

import play.twirl.api.Xml
import play.twirl.api.XmlFormat
import resource.managed

/** Helper methods for use in automated tests.
  */
object TestHelpers {

  /** Wraps text in a simple XSL-FO document, optionally applying a font.
    * Returns the document as Twirl XML.
    *
    * @param text the text to wrap
    * @fontFamily the font to apply (optional)
    */
  def wrapInXslfoDocument(text: String, fontFamily: Option[String] = None): Xml = {

    val textAsXMLText = Text(text)
    val fontFamilyAsXMLText = fontFamily.map { Text(_) }

    toTwirlXml(
      <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
        <fo:layout-master-set>
          <fo:simple-page-master master-name="label">
            <fo:region-body region-name="xsl-region-body"/>
          </fo:simple-page-master>
        </fo:layout-master-set>
        <fo:page-sequence master-reference="label">
          <fo:flow flow-name="xsl-region-body">
            <fo:block font-family={fontFamilyAsXMLText}>{textAsXMLText}</fo:block>
          </fo:flow>
        </fo:page-sequence>
      </fo:root>
    )
  }

  private def toTwirlXml(xml: Elem): Xml = {
    val stringWriter = new StringWriter()
    XML.write(stringWriter, xml, "utf-8", true /* xmlDecl */, null /* doctype */)
    XmlFormat.raw(stringWriter.toString())
  }

  def textFromPDFBytes(pdfBytes: Array[Byte]) = {

    managed(toPDDocument(pdfBytes)) acquireAndGet { pdDocument =>

      new PDFTextStripper().getText(pdDocument).trim()
    }
  }

  def authorFromPDFBytes(pdfBytes: Array[Byte]) = {

    managed(toPDDocument(pdfBytes)) acquireAndGet { pdDocument =>

      pdDocument.getDocumentInformation().getAuthor()
    }
  }

  def fontsFromPDFBytes(pdfBytes: Array[Byte]) = {

    managed(toPDDocument(pdfBytes)) acquireAndGet { pdDocument =>

      val pdPageTree = pdDocument.getDocumentCatalog().getPages()

      pdPageTree.asScala.foldLeft(Set.empty[String]) { case(fontsSet, pdPage) =>
        val pdResources = pdPage.getResources()
        val fonts = pdResources.getFontNames().asScala.map { pdResources.getFont(_).getName() }
        fontsSet ++ fonts
      }
    }
  }

  private def toPDDocument(pdfBytes: Array[Byte]) = PDDocument.load(new ByteArrayInputStream(pdfBytes))
}