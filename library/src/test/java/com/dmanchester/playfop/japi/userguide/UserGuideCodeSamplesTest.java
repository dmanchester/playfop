package com.dmanchester.playfop.japi.userguide;

//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//import org.apache.fop.apps.FOUserAgent;
//import org.apache.xmlgraphics.util.MimeConstants;
//import org.junit.Test;
//
//import com.dmanchester.playfop.TestHelpers;
//import com.dmanchester.playfop.api.ClasspathURIResolver;
//import com.dmanchester.playfop.japi.FOUserAgentBlock;
//import com.dmanchester.playfop.japi.PlayFop;

public class UserGuideCodeSamplesTest {

//    @Test
//    public void testSimpleCodeSample() {
//
//// BEGIN Simple Java 'process' sample
//// IMPORTANT: If following line is changed, UserGuide.scalatex must be changed
//// in kind!
//byte[] png = PlayFop.process(
//    views.xml.someTemplate.render("Hello world."),
//    MimeConstants.MIME_PNG
//);
//// END Simple Java 'process' sample
//
//        assertTrue(png.length > 3000);  // If PNG generated correctly, it should be a little larger than 3K.
//    }
//
//    @Test
//    public void testComplexCodeSample() {
//
//// BEGIN Complex Java 'process' sample
//// IMPORTANT: If following line is changed, UserGuide.scalatex must be changed
//// in kind!
//String fopConfigPDFAutoDetectFonts =
//    "<fop version=\"1.0\">" +
//    "    <renderers>" +
//    "        <renderer mime=\"application/pdf\">" +
//    "            <fonts>" +
//    "                <auto-detect/>" +
//    "            </fonts>" +
//    "        </renderer>" +
//    "    </renderers>" +
//    "</fop>";
//
//FOUserAgentBlock foUserAgentBlock = new FOUserAgentBlock() {
//    @Override
//    public void withFOUserAgent(FOUserAgent foUserAgent) {
//        foUserAgent.setAuthor("PlayFOP Sample Code");
//    }
//};
//
//byte[] pdf = PlayFop.process(
//    views.xml.someTemplate.render("Hello again."),
//    MimeConstants.MIME_PDF,
//    fopConfigPDFAutoDetectFonts,
//    foUserAgentBlock
//);
//// END Complex Java 'process' sample
//
//        assertEquals("Hello again.", TestHelpers.textFromPDFBytes(pdf));  // for readability of code sample, we don't declare a constant for this String or the next one
//        assertEquals("PlayFOP Sample Code", TestHelpers.authorFromPDFBytes(pdf));
//        // We don't have an assertion to confirm the FOP configuration XML was
//        // applied. The XML is sufficiently simple that it's unclear what to
//        // assert on.
//    }
//
//    @Test
//    public void testSampleWithImage() {
//
//// BEGIN Java ClasspathURIResolver sample
//// IMPORTANT: If following line is changed, UserGuide.scalatex must be changed
//// in kind!
//final ClasspathURIResolver classpathURIResolver = new ClasspathURIResolver();
//String imageURI = classpathURIResolver.createHref("emoji_u1f309_bridge.svg");
//
//FOUserAgentBlock foUserAgentBlock = new FOUserAgentBlock() {
//    @Override
//    public void withFOUserAgent(FOUserAgent foUserAgent) {
//        foUserAgent.setURIResolver(classpathURIResolver);
//    }
//};
//
//byte[] pdf = PlayFop.process(
//    views.xml.someTemplateWithAnImage.render(imageURI),
//    MimeConstants.MIME_PDF,
//    foUserAgentBlock
//);
//// END Java ClasspathURIResolver sample
//
//        assertTrue(pdf.length > 6000);  // If PDF generated correctly, it should be a little larger than 6K.
//    }
}
