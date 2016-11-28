package com.dmanchester.playfop.api

import java.io.File

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.specs2.mutable.BeforeAfter
import org.specs2.mutable.Specification

import javax.xml.transform.Source
import javax.xml.transform.TransformerException
import javax.xml.transform.stream.StreamSource

import play.api.test.WithApplication

class ClasspathURIResolverSpec extends Specification {

  private val ResourceName = "ClasspathURIResolverSpec_resource.txt"  // located in src/test/resources
  private val TempFilePrefix = "playfop"
  private val TempFileContents = "Temp file from PlayFOP ClasspathURIResolverSpec."

  "createHref() and resolve()" should {

    "handle PlayFOP's URI scheme" in new WithApplication {

      // Load contents of the resource into memory. (To be compared with what
      // the resolver loads.)
      val resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(ResourceName)
      if (resourceAsStream == null) {
        throw new NullPointerException(s"Could not find resource '$ResourceName'!")
      }
      val resourceContents = IOUtils.toString(resourceAsStream, "utf-8")

      val resolver = new ClasspathURIResolver()

      val resourceURI = resolver.createHref(ResourceName)
      val resourceSource = resolver.resolve(resourceURI, null /* base */)

      sourceToString(resourceSource) must beEqualTo(resourceContents)
    }
  }

  "resolve()" should {

    "handle URI schemes beyond PlayFOP's" in new TempFileReliantExample {

      val resolver = new ClasspathURIResolver()

      val tempFileURI = tempFile.toURI().toString()  // has "file:" scheme
      val tempFileSource = resolver.resolve(tempFileURI, null /* base */)

      sourceToString(tempFileSource) must beEqualTo(TempFileContents)
    }
  }

  private def sourceToString(source: Source) = {
    // The only way to read a javax.xml.transform.Source without downcasting
    // seems to be Transformer.transform(Source, Result). However, if the Source
    // represents something other than XML (as it can in the Apache FOP API for
    // FOURIResolver), transform() fails on, "Content is not allowed in prolog."
    //
    // So, we downcast the Source to a StreamSource and access the byte stream
    // it contains.
    IOUtils.toString(source.asInstanceOf[StreamSource].getInputStream, "utf-8")
  }
  
  trait TempFileReliantExample extends BeforeAfter {
    protected val tempFile = File.createTempFile(TempFilePrefix, null /* suffix; null triggers ".tmp" default */)
    def before = FileUtils.writeStringToFile(tempFile, TempFileContents, "utf-8")
    def after = tempFile.delete
  }
}