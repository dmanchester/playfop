package com.dmanchester.playfop.internal

import java.io.ByteArrayInputStream
import java.io.StringWriter

import scala.xml.Elem
import scala.xml.XML

import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder
import org.apache.fop.apps.FopFactory
import org.slf4j.LoggerFactory

/** A source of `FopFactory` instances. Caches them for performance.
  */
class FopFactorySource {

  private val defaultConfigCache = new MutableRef[FopFactory]
  private val customConfigCache = scala.collection.mutable.Map.empty[Elem, FopFactory]

  private val Logger = LoggerFactory.getLogger(this.getClass())

  
  /** Gets an `FopFactory` having its default configuration.
    *  
    * @return the `FopFactory`
    */
  def getWithDefaultConfig: FopFactory = {

    Logger.debug("Checking cache for a default-configuration FopFactory instance...")

    val (fopFactory, fromCache) = defaultConfigCache.synchronized {
      
      defaultConfigCache.get.map {
        (_, true)
      } getOrElse {
        val newFopFactory = createWithDefaultConfig
        defaultConfigCache.set(newFopFactory)
        (newFopFactory, false)
      }
    }

    // Do logging (and associated I/O) *outside* synchronized block.
    if (fromCache) {
      Logger.debug("...default-configuration instance found.")
    } else {
      Logger.debug("...no default-configuration instance found. Instance created.")
    }

    fopFactory
  }

  /** Gets an `FopFactory` having a custom configuration.
    *
    * @param fopConfigXml the configuration XML
    * @return the `FopFactory`
    */
  def getWithCustomConfig(fopConfigXml: Elem): FopFactory = {

    Logger.debug("Checking cache for an FopFactory instance with configuration XML...")

    val (fopFactory, fromCache) = customConfigCache.synchronized {
      customConfigCache.get(fopConfigXml).map {
        (_, true)
      } getOrElse {
        val newFopFactory = createWithCustomConfig(fopConfigXml)
        customConfigCache += (fopConfigXml -> newFopFactory)
        (newFopFactory, false)
      }
    }

    // Do logging (and associated I/O) *outside* synchronized block.
    if (fromCache) {
      Logger.debug("...instance with configuration XML found.")
    } else {
      Logger.debug("...no instance with configuration XML found. Instance created.")
    }

    fopFactory
  }

  private def createWithDefaultConfig: FopFactory = {
    FopFactory.newInstance()
  }

  private def createWithCustomConfig(fopConfigXml: Elem): FopFactory = {

    val stringWriter = new StringWriter()
    XML.write(stringWriter, fopConfigXml, "utf-8", true /* xmlDecl */, null /* doctype */)
    val inputStream = new ByteArrayInputStream(stringWriter.toString().getBytes("utf-8"))

    // DefaultConfigurationBuilder instances not documented as thread-safe, so
    // obtain a new one each time.
    val fopConfig = new DefaultConfigurationBuilder().build(inputStream)

    val fopFactory = FopFactory.newInstance()
    fopFactory.setUserConfig(fopConfig)

    fopFactory
  }
}