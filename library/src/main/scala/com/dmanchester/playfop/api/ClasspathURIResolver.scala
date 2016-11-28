package com.dmanchester.playfop.api

import java.io.ByteArrayInputStream

import scala.util.matching.Regex

import org.apache.commons.io.IOUtils
import org.apache.fop.apps.FOURIResolver
import org.slf4j.LoggerFactory

import javax.xml.transform.Source
import javax.xml.transform.TransformerException
import javax.xml.transform.stream.StreamSource
import play.api.Play.current

/** Supplements Apache FOP's default `FOURIResolver` with support for loading
  * resources from a Play application's classpath.
  * 
  * While the `ClasspathURIResolver` code is thread-safe, the thread safety of
  * its superclass is not documented. Thus, it is best not to share a
  * `ClasspathURIResolver` instance across threads.
  */
class ClasspathURIResolver extends FOURIResolver {

  private val Scheme = "playfop"
  private val URIPattern: Regex = (Scheme + ":(.*)").r  // captures resource path in group #1

  private val Logger = LoggerFactory.getLogger(this.getClass())

  /** Creates an href/URI for a resource by prefixing its path with the
    * "`playfop:`" scheme.
    *
    * @param path the path to the resource
    * @return the href/URI
    */
  def createHref(path: String): String = Scheme + ":" + path

  /** Resolves a resource's href/URI such that Apache FOP can incorporate the
    * resource into an XSL-FO document.
    * 
    * In addition to the URI schemes handled by Apache FOP's `FOURIResolver`,
    * supports the "`playfop:`" scheme for loading resources from an application's
    * classpath.
    *
    * @param href the resource's href/URI
    * @param base generally, `null`
    * @return a `Source` that packages the resource
    */
  override def resolve(href: String, base: String): Source = {

    Logger.debug(s"href (URI): '$href'")

    val source = URIPattern.findFirstMatchIn(href).map { patternMatch =>
      Logger.debug("Processing URI.")
      doResolve(patternMatch.group(1))
    } getOrElse {
      // href has a scheme other than what we process
      Logger.debug("Delegating URI processing to FOURIResolver.")
      super.resolve(href, base)
    }

    source
  }

  private def doResolve(path: String): Source = {
    val bytes = loadBytes(path)
    new StreamSource(new ByteArrayInputStream(bytes))
  }

  private def loadBytes(path: String): Array[Byte] = {

    val inputStream = play.api.Play.resourceAsStream(path).getOrElse {
      throw new NullPointerException(s"Could not find resource '$path'!")
    }

    try {
      IOUtils.toByteArray(inputStream)
      // After reviewing http://stackoverflow.com/questions/7598135/how-to-read-a-file-as-a-byte-array-in-scala,
      // decided to stick with IOUtils.
    } finally {
      IOUtils.closeQuietly(inputStream)
    }
  }
}
