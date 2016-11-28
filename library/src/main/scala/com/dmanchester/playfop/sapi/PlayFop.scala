package com.dmanchester.playfop.sapi

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.StringReader

import scala.xml.Elem

import org.apache.fop.apps.FOUserAgent
import org.apache.fop.apps.Fop
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

  private val Logger = LoggerFactory.getLogger(this.getClass())

  /** Processes XSL-FO with Apache FOP. Generates output in the specified
    * format.
    *
    * @param xslfo the XSL-FO to process
    * @param outputFormat the format to generate
    * @return the Apache FOP output
    */
  def process(xslfo: Xml, outputFormat: String): Array[Byte] = {
    process(xslfo, outputFormat, None, None)
  }

  /** Processes XSL-FO with Apache FOP, configuring FOP with the configuration
    * XML. Generates output in the specified format.
    *
    * @param xslfo the XSL-FO to process
    * @param outputFormat the format to generate
    * @param fopConfig the configuration XML for FOP
    * @return the Apache FOP output
    */
  def process(xslfo: Xml, outputFormat: String, fopConfig: Elem): Array[Byte] = {
    process(xslfo, outputFormat, Some(fopConfig), None)
  }

  /** Processes XSL-FO with Apache FOP, configuring the `FOUserAgent` with the
    * code block. Generates output in the specified format.
    *
    * @param xslfo the XSL-FO to process
    * @param outputFormat the format to generate
    * @param foUserAgentBlock the code block for the `FOUserAgent`
    * @return the Apache FOP output
    */
  def process(xslfo: Xml, outputFormat: String, foUserAgentBlock: (FOUserAgent => Unit)): Array[Byte] = {
    process(xslfo, outputFormat, None, Some(foUserAgentBlock))
  }

  /** Processes XSL-FO with Apache FOP, configuring FOP with the configuration
    * XML and the `FOUserAgent` with the code block. Generates output in the
    * specified format.
    *
    * @param xslfo the XSL-FO to process
    * @param outputFormat the format to generate
    * @param fopConfig the configuration XML for FOP
    * @param foUserAgentBlock the code block for the `FOUserAgent`
    * @return the Apache FOP output
    */
  def process(xslfo: Xml, outputFormat: String, fopConfig: Elem, foUserAgentBlock: (FOUserAgent => Unit)): Array[Byte] = {
    process(xslfo, outputFormat, Some(fopConfig), Some(foUserAgentBlock))
  }

  /** Processes XSL-FO with Apache FOP, optionally configuring FOP with
   *  configuration XML and the `FOUserAgent` with a code block. Generates
   *  output in the specified format.
    *
    * '''Note:''' This variant of `process()`, with some `Option` arguments, is
    * primarily intended for PlayFOP's internal use, but external use is also
    * supported.
    *
    * @param xslfo the XSL-FO to process
    * @param outputFormat the format to generate
    * @param fopConfig the configuration XML for FOP
    * @param foUserAgentBlock the code block for the `FOUserAgent`
    * @return the Apache FOP output
    */
  def process(xslfo: Xml, outputFormat: String, fopConfig: Option[Elem], foUserAgentBlock: Option[(FOUserAgent => Unit)]): Array[Byte] = {

    Logger.info("Rendering XSL-FO...")
    if (Logger.isTraceEnabled()) {
      Logger.trace(s"XSL-FO:\n$xslfo")
    }
    
    val output = new ByteArrayOutputStream()
    val fop = newFop(outputFormat, output, fopConfig, foUserAgentBlock)
    
    val transformer = TransformerFactory.newInstance().newTransformer()

    val source = new StreamSource(new StringReader(xslfo.body))

    val result = new SAXResult(fop.getDefaultHandler())

    transformer.transform(source, result)

    val byteArray = output.toByteArray()
    
    Logger.info(s"...XSL-FO rendered. ${byteArray.length} bytes produced.")

    byteArray
  }
  
  /** Creates a new `Fop` instance. Sets it up to save output to the supplied
    * `OutputStream` in the supplied format.
    *
    * '''Note:''' The `newFop` methods are offered primarily for client code to
    * interrogate the Apache FOP environment (for example, to determine
    * available fonts). Code wishing to process XSL-FO with Apache FOP should
    * rely on a `process` method instead.
    *
    * @param outputFormat the format the `Fop` should generate
    * @param output the `OutputStream` to which the `Fop` should save output
    * @return the `Fop`
    */
  def newFop(outputFormat: String, output: OutputStream): Fop = {
    newFop(outputFormat, output, None, None)
  }

  /** Creates a new `Fop` instance, configuring it with the configuration XML.
    * Sets up the `Fop` to save output to the supplied `OutputStream` in the
    * supplied format.
    *
    * '''Note:''' The `newFop` methods are offered primarily for client code to
    * interrogate the Apache FOP environment (for example, to determine
    * available fonts). Code wishing to process XSL-FO with Apache FOP should
    * rely on a `process` method instead.
    *
    * @param outputFormat the format the `Fop` should generate
    * @param output the `OutputStream` to which the `Fop` should save output
    * @param fopConfig the configuration XML for the `Fop`
    * @return the `Fop`
    */
  def newFop(outputFormat: String, output: OutputStream, fopConfig: Elem): Fop = {
    newFop(outputFormat, output, Some(fopConfig), None)
  }

  /** Creates a new `Fop` instance, configuring its `FOUserAgent` with the code
    * block. Sets up the `Fop` to save output to the supplied `OutputStream` in
    * the supplied format.
    *
    * '''Note:''' The `newFop` methods are offered primarily for client code to
    * interrogate the Apache FOP environment (for example, to determine
    * available fonts). Code wishing to process XSL-FO with Apache FOP should
    * rely on a `process` method instead.
    *
    * @param outputFormat the format the `Fop` should generate
    * @param output the `OutputStream` to which the `Fop` should save output
    * @param foUserAgentBlock the code block for the `Fop`'s `FOUserAgent`
    * @return the `Fop`
    */
  def newFop(outputFormat: String, output: OutputStream, foUserAgentBlock: (FOUserAgent => Unit)): Fop = {
    newFop(outputFormat, output, None, Some(foUserAgentBlock))
  }

  /** Creates a new `Fop` instance, configuring the `Fop` with the configuration
    * XML and its `FOUserAgent` with the code block. Sets up the `Fop` to save
    * output to the supplied `OutputStream` in the supplied format.
    * 
    * '''Note:''' The `newFop` methods are offered primarily for client code to
    * interrogate the Apache FOP environment (for example, to determine
    * available fonts). Code wishing to process XSL-FO with Apache FOP should
    * rely on a `process` method instead.
    *
    * @param outputFormat the format the `Fop` should generate
    * @param output the `OutputStream` to which the `Fop` should save output
    * @param fopConfig the configuration XML for the `Fop`
    * @param foUserAgentBlock the code block for the `Fop`'s `FOUserAgent`
    * @return the `Fop`
    */
  def newFop(outputFormat: String, output: OutputStream, fopConfig: Elem, foUserAgentBlock: (FOUserAgent => Unit)): Fop = {
    newFop(outputFormat, output, Some(fopConfig), Some(foUserAgentBlock))
  }

  /** Creates a new `Fop` instance, optionally configuring the `Fop` with
    * configuration XML and its `FOUserAgent` with a code block. Sets up the
    * `Fop` to save output to the supplied `OutputStream` in the supplied format.
    *
    * '''Note:''' The `newFop` methods are offered primarily for client code to
    * interrogate the Apache FOP environment (for example, to determine
    * available fonts). Code wishing to process XSL-FO with Apache FOP should
    * rely on a `process` method instead.
    *
    * '''Note:''' This variant of `newFop`, with some `Option` arguments, is
    * primarily intended for PlayFOP's internal use, but external use is also
    * supported.
    *
    * @param outputFormat the format the `Fop` should generate
    * @param output the `OutputStream` to which the `Fop` should save output
    * @param fopConfig the configuration XML for the `Fop`
    * @param foUserAgentBlock the code block for the `Fop`'s `FOUserAgent`
    * @return the `Fop`
    */
  def newFop(outputFormat: String, output: OutputStream, fopConfig: Option[Elem], foUserAgentBlock: Option[(FOUserAgent => Unit)]): Fop = {

    val fopFactory = fopConfig.map {
      fopFactorySource.getWithCustomConfig(_)
    }.getOrElse {
      fopFactorySource.getWithDefaultConfig
    }

    val foUserAgent = fopFactory.newFOUserAgent()
    foUserAgentBlock.foreach { block =>
      block(foUserAgent)
    }
    
    fopFactory.newFop(outputFormat, foUserAgent, output)
  }
}
