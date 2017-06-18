package com.dmanchester.playfop

import java.io.ByteArrayInputStream
import java.io.StringWriter

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.util.PDFTextStripper

import scala.xml.Elem
import scala.xml.Node
import scala.xml.Text
import scala.xml.XML
import scala.collection.JavaConverters._

import play.twirl.api.Xml
import play.twirl.api.XmlFormat

/** Helper methods for use in automated tests.
  */
object TestHelpers {

  /** Wraps an XML Node in a simple XSL-FO document, optionally applying a font.
    * Returns the document as Twirl XML.
    *
    * @param node the Node to wrap
    * @fontFamily the font to apply (optional)
    */
  def wrapInXslfoDocument(node: Node, fontFamily: Option[String] = None): Xml = {
    val fontFamilyAsText = fontFamily.map { Text(_) }
    toTwirlXml(
      <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
        <fo:layout-master-set>
          <fo:simple-page-master master-name="label">
            <fo:region-body region-name="xsl-region-body"/>
          </fo:simple-page-master>
        </fo:layout-master-set>
        <fo:page-sequence master-reference="label">
          <fo:flow flow-name="xsl-region-body">
            <fo:block font-family={fontFamilyAsText}>{node}</fo:block>
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
    val pdDocument = toPDDocument(pdfBytes)
    new PDFTextStripper().getText(pdDocument).trim()
  }

  def authorFromPDFBytes(pdfBytes: Array[Byte]) = {
    val pdDocument = toPDDocument(pdfBytes)
    pdDocument.getDocumentInformation().getAuthor()
  }

  def fontsFromPDFBytes(pdfBytes: Array[Byte]) = {
    val pdDocument = toPDDocument(pdfBytes)
    val pdPages: java.util.List[PDPage] = pdDocument.getDocumentCatalog().getAllPages().asInstanceOf[java.util.List[PDPage]]

    pdPages.asScala.foldLeft(Set.empty[String]) { case(theSet, pdPage) =>
      val fonts = pdPage.getResources().getFonts().values().asScala.map { _.getBaseFont() }
      theSet ++ fonts
    }
  }

  private def toPDDocument(pdfBytes: Array[Byte]) = PDDocument.load(new ByteArrayInputStream(pdfBytes))
}