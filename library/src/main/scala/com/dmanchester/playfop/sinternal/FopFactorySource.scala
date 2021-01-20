package com.dmanchester.playfop.sinternal

import java.io.ByteArrayInputStream
import java.io.File
import java.io.StringWriter

import scala.xml.Elem
import scala.xml.XML

import org.apache.fop.configuration.DefaultConfigurationBuilder
import org.apache.fop.apps.FopFactory
import org.apache.fop.apps.FopFactoryBuilder
import org.slf4j.LoggerFactory

/** A source of `FopFactory` instances. Caches them for performance.
  *
  * Instances of this class are thread-safe, but there are open questions around
  * the thread safety of `FopFactory` instances themselves. For more
  * information, see {@link com.dmanchester.playfop.sinternal.PlayFopImpl} and
  * the PlayFOP User Guide.
  */
class FopFactorySource {

  private val fopFactoryCache = scala.collection.mutable.Map.empty[Option[Elem], FopFactory]

  private val logger = LoggerFactory.getLogger(this.getClass())

  /** Gets an `FopFactory`, optionally configured with XML.
    *
    * @param fopConfigXml the configuration XML
    * @return the `FopFactory`
    */
  def get(fopConfigXml: Option[Elem]): FopFactory = {

    logger.debug("Checking cache for an appropriate FopFactory instance...")

    val (fopFactory, fromCache) = fopFactoryCache.synchronized {
      fopFactoryCache.get(fopConfigXml).map {
        (_, true)
      } getOrElse {
        val newFopFactory = createFopFactory(fopConfigXml)
        fopFactoryCache += (fopConfigXml -> newFopFactory)
        (newFopFactory, false)
      }
    }

    // Do logging (and associated I/O) *outside* synchronized block.
    if (fromCache) {
      logger.debug("...instance found.")
    } else {
      logger.debug("...no instance found. Instance created.")
    }

    fopFactory
  }

  private def createFopFactory(fopConfigXml: Option[Elem]): FopFactory = {

    val fopFactoryBuilder = new FopFactoryBuilder(new File(".").toURI() /* defaultBaseURI */)

    fopConfigXml.foreach { fopConfigXml =>

      val stringWriter = new StringWriter()
      XML.write(stringWriter, fopConfigXml, "utf-8", true /* xmlDecl */, null /* doctype */)
      val inputStream = new ByteArrayInputStream(stringWriter.toString().getBytes("utf-8"))

      // DefaultConfigurationBuilder instances not documented as thread-safe, so
      // obtain a new one each time.
      val fopConfig = new DefaultConfigurationBuilder().build(inputStream)

      fopFactoryBuilder.setConfiguration(fopConfig)
    }

    fopFactoryBuilder.build()
  }
}