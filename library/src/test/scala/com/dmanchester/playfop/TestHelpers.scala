package com.dmanchester.playfop

import java.io.ByteArrayInputStream
import java.io.StringWriter

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.util.PDFTextStripper

import scala.xml.Elem
import scala.xml.Node
import scala.xml.Text
import scala.xml.XML

import play.twirl.api.Xml
import play.twirl.api.XmlFormat

/** Helper methods for use in automated tests.
  */
object TestHelpers {

  /** Wraps text in a simple XSL-FO document, returning it as Twirl XML.
    *
    * @param text the text to wrap
    */
  def wrapInXslfoDocument(text: String): Xml = wrapInXslfoDocument(Text(text))

  /** Wraps an XML Node in a simple XSL-FO document, returning it as Twirl XML.
    *
    * @param node the Node to wrap
    */
  def wrapInXslfoDocument(node: Node): Xml = toTwirlXml(
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <fo:layout-master-set>
        <fo:simple-page-master master-name="label">
          <fo:region-body region-name="xsl-region-body"/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="label">
        <fo:flow flow-name="xsl-region-body">
          <fo:block>{node}</fo:block>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  )

  private def toTwirlXml(xml: Elem): play.twirl.api.Xml = {
    val stringWriter = new StringWriter()
    XML.write(stringWriter, xml, "utf-8", true /* xmlDecl */, null /* doctype */)
    XmlFormat.raw(stringWriter.toString())
  }

  def textFromPDFBytes(pdfBytes: Array[Byte]) = {
    val pdDocument = toPDDocument(pdfBytes)
    new PDFTextStripper().getText(pdDocument).trim()
  }

  def versionFromPDFBytes(pdfBytes: Array[Byte]) = {
    val pdDocument = toPDDocument(pdfBytes)
    pdDocument.getDocument().getVersion().toString()
    // Previous line's retrieval of version is an alternative to
    // pdDocument.getDocumentCatalog().getVersion(), which oddly did not work.
  }

  def authorFromPDFBytes(pdfBytes: Array[Byte]) = {
    val pdDocument = toPDDocument(pdfBytes)
    pdDocument.getDocumentInformation().getAuthor()
  }

  private def toPDDocument(pdfBytes: Array[Byte]) = PDDocument.load(new ByteArrayInputStream(pdfBytes))
}