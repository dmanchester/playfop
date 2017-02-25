package com.dmanchester.playfop.sapi

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.StringReader

import scala.xml.Elem

import org.apache.fop.apps.FOUserAgent
import org.apache.fop.apps.Fop
import org.apache.xmlgraphics.io.ResourceResolver
import org.slf4j.LoggerFactory

import com.dmanchester.playfop.internal.FopFactorySource

import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.stream.StreamSource
import play.twirl.api.Xml

/** The primary entry point into PlayFOP for Scala applications.
  */
object PlayFop {

  private val fopFactorySource = new FopFactorySource()

  private val logger = LoggerFactory.getLogger(this.getClass())

  /** Processes XSL-FO with Apache FOP, optionally auto-detecting fonts (for PDF
    * output) and/or applying a code block to the `FOUserAgent`. Generates
    * output in the specified format.
    *
    * @param xslfo the XSL-FO to process
    * @param outputFormat the format to generate
    * @param autoDetectFontsForPDF whether to auto-detect fonts
    * @param foUserAgentBlock the code block for the `FOUserAgent`
    * @return the Apache FOP output
    */
  def process[U](xslfo: Xml, outputFormat: String,
      autoDetectFontsForPDF: Boolean = false,
      foUserAgentBlock: (FOUserAgent => U) = {_: FOUserAgent => }): Array[Byte] = {

    logger.info("Rendering XSL-FO...")
    if (logger.isTraceEnabled()) {
      logger.trace(s"XSL-FO:\n$xslfo")
    }
    
    val output = new ByteArrayOutputStream()
    val fop = newFop(outputFormat, output, autoDetectFontsForPDF, foUserAgentBlock)

    val transformer = TransformerFactory.newInstance().newTransformer()

    val source = new StreamSource(new StringReader(xslfo.body))

    val result = new SAXResult(fop.getDefaultHandler())

    transformer.transform(source, result)

    val byteArray = output.toByteArray()

    logger.info(s"...XSL-FO rendered. ${byteArray.length} bytes produced.")

    byteArray
  }

  /** Creates a new `Fop` instance, optionally auto-detecting fonts (for PDF
    * output) and/or applying a code block to the `FOUserAgent`. Sets up the
    * `Fop` to save output to the supplied `OutputStream` in the supplied format.
    *
    * '''Note:''' The `newFop` method is offered primarily for client code to
    * interrogate the Apache FOP environment (for example, to determine
    * available fonts). Code wishing to process XSL-FO with Apache FOP should
    * rely on the `process` method instead.
    *
    * @param outputFormat the format the `Fop` should generate
    * @param output the `OutputStream` to which the `Fop` should save output
    * @param autoDetectFontsForPDF whether to auto-detect fonts
    * @param foUserAgentBlock the code block for the `Fop`'s `FOUserAgent`
    * @return the `Fop`
    */
  def newFop[U](outputFormat: String, output: OutputStream,
      autoDetectFontsForPDF: Boolean = false,
      foUserAgentBlock: (FOUserAgent => U) = {_: FOUserAgent => }): Fop = {

    val fopConfigXml: Option[Elem] = if (autoDetectFontsForPDF) {
      Some(
          <fop version="1.0">
            <renderers>
              <renderer mime="application/pdf">
                <fonts>
                  <auto-detect/>
                </fonts>
              </renderer>
            </renderers>
          </fop>
      )
    } else {
      None
    }

    val fopFactory = fopFactorySource.get(fopConfigXml)

    val foUserAgent = fopFactory.newFOUserAgent()
    foUserAgentBlock(foUserAgent)

    fopFactory.newFop(outputFormat, foUserAgent, output)
  }
}
