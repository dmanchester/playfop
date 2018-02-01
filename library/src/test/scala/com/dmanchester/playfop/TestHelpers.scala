package com.dmanchester.playfop

import java.io.ByteArrayInputStream
import java.io.StringWriter

import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import scala.xml.Node
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

  def wrapInTwirlXmlDocument(text: String, fontFamily: Option[String] = None): Xml = {

    toTwirlXml(wrapInScalaXmlDocument(text, fontFamily))
  }

  def wrapInScalaXmlDocument(text: String, fontFamily: Option[String] = None): Node = {

    val textAsXMLText = Text(text)
    val fontFamilyAsXMLText = fontFamily.map { Text(_) }

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
  }

  def wrapInStringXmlDocument(text: String, fontFamily: Option[String] = None): String = {

    toStringXml(wrapInScalaXmlDocument(text, fontFamily))
  }

  private def toTwirlXml(xml: Node): Xml = {
    XmlFormat.raw(toStringXml(xml))
  }

  private def toStringXml(xml: Node): String = {
    val stringWriter = new StringWriter()
    XML.write(stringWriter, xml, "utf-8", true /* xmlDecl */, null /* doctype */)
    stringWriter.toString()
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