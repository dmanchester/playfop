package com.dmanchester.playfop.sapi.userguide

import com.dmanchester.playfop.api.ClasspathURIResolver
import com.dmanchester.playfop.sapi.PlayFop
import com.dmanchester.playfop.TestHelpers

import org.apache.fop.apps.FOUserAgent
import org.apache.xmlgraphics.util.MimeConstants
import org.specs2.mutable.Specification

import play.api.test.WithApplication

class UserGuideCodeSamplesSpec extends Specification {

  "This code sample" should {
    "render the XSL-FO as PNG" in {

// BEGIN Simple Scala 'process' sample
// IMPORTANT: If following line is changed, UserGuide.scalatex must be changed
// in kind!
val png: Array[Byte] = PlayFop.process(
  views.xml.someTemplate.render("Hello world."),
  MimeConstants.MIME_PNG
)
// END Simple Scala 'process' sample

      png.length must beGreaterThan(3000)  // If PNG generated correctly, it should be a little larger than 3K.
    }
  }

  "This code sample" should {
    "render the XSL-FO as PDF, applying the FOP configuration and the FOUserAgent block" in {

// BEGIN Complex Scala 'process' sample
// IMPORTANT: If following line is changed, UserGuide.scalatex must be changed
// in kind!
val fopConfigPDFAutoDetectFonts =
    <fop version="1.0">
      <renderers>
        <renderer mime="application/pdf">
          <fonts>
            <auto-detect/>
          </fonts>
        </renderer>
      </renderers>
    </fop>

val foUserAgentBlock = { foUserAgent: FOUserAgent =>
  foUserAgent.setAuthor("PlayFOP Sample Code")
}

val pdf: Array[Byte] = PlayFop.process(
  views.xml.someTemplate.render("Hello again."),
  MimeConstants.MIME_PDF,
  fopConfigPDFAutoDetectFonts,
  foUserAgentBlock
)
// END Complex Scala 'process' sample

      TestHelpers.textFromPDFBytes(pdf) must beEqualTo("Hello again.")  // for readability of code sample, we don't declare a constant for this String or the next one
      TestHelpers.authorFromPDFBytes(pdf) must beEqualTo("PlayFOP Sample Code")
      // We don't have an assertion to confirm the FOP configuration XML was
      // applied. The XML is sufficiently simple that it's unclear what to
      // assert on.
    }
  }

  "This code sample" should {
    "render the image-containing XSL-FO as PDF" in new WithApplication {

// BEGIN Scala ClasspathURIResolver sample
// IMPORTANT: If following line is changed, UserGuide.scalatex must be changed
// in kind!
val classpathURIResolver = new ClasspathURIResolver()
val imageURI = classpathURIResolver.createHref("emoji_u1f309_bridge.svg")

val foUserAgentBlock = { foUserAgent: FOUserAgent =>
  foUserAgent.setURIResolver(classpathURIResolver)
}

val pdf: Array[Byte] = PlayFop.process(
  views.xml.someTemplateWithAnImage.render(imageURI),
  MimeConstants.MIME_PDF,
  foUserAgentBlock
)
// END Scala ClasspathURIResolver sample

      pdf.length must beGreaterThan(6000)  // If PDF generated correctly, it should be a little larger than 6K.
    }
  }
}
