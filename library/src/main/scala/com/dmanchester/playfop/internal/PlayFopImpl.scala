package com.dmanchester.playfop.internal

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.StringReader

import scala.xml.Elem

import org.apache.fop.apps.FOUserAgent
import org.apache.fop.apps.Fop
import org.slf4j.LoggerFactory

import com.dmanchester.playfop.sapi.PlayFop

import javax.inject.Singleton
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.stream.StreamSource
import play.twirl.api.Xml

@Singleton
class PlayFopImpl extends PlayFop {

  private val fopFactorySource = new FopFactorySource()

  private val logger = LoggerFactory.getLogger(this.getClass())

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
