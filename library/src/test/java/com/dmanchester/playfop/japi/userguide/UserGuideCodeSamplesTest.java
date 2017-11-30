package com.dmanchester.playfop.japi.userguide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.fop.apps.FOUserAgent;
import org.apache.xmlgraphics.util.MimeConstants;
import org.junit.Test;

import com.dmanchester.playfop.TestHelpers;
import com.dmanchester.playfop.japi.FOUserAgentBlock;
import com.dmanchester.playfop.japi.PlayFop;
import com.dmanchester.playfop.japi.ProcessOptions;
import com.dmanchester.playfop.jinternal.PlayFopImpl;

public class UserGuideCodeSamplesTest {

    private PlayFop playFop = new PlayFopImpl();

    @Test
    public void testSimpleCodeSample() {

// BEGIN Simple Java 'process' sample
// IMPORTANT: If following line is changed, UserGuide.scalatex must be changed
// in kind!
byte[] png = playFop.process(
    views.xml.someTemplate.render("Hello world."),
    MimeConstants.MIME_PNG
);
// END Simple Java 'process' sample

        assertTrue(png.length > 3000);  // If PNG generated correctly, it should be a little larger than 3K.
    }

    @Test
    public void testComplexCodeSample() {

// BEGIN Complex Java 'process' sample
// IMPORTANT: If following line is changed, UserGuide.scalatex must be changed
// in kind!
FOUserAgentBlock myFOUserAgentBlock = new FOUserAgentBlock() {
    @Override
    public void withFOUserAgent(FOUserAgent foUserAgent) {
        foUserAgent.setAuthor("PlayFOP Sample Code");
    }
};

ProcessOptions processOptions = new ProcessOptions.Builder().
        autoDetectFontsForPDF(true).foUserAgentBlock(myFOUserAgentBlock).build();

byte[] pdf = playFop.process(
    views.xml.someTemplate.render("Hello again."),
    MimeConstants.MIME_PDF,
    processOptions
);
// END Complex Java 'process' sample

        assertEquals("Hello again.", TestHelpers.textFromPDFBytes(pdf));  // for readability of code sample, we don't declare a constant for this String or the next one
        assertEquals("PlayFOP Sample Code", TestHelpers.authorFromPDFBytes(pdf));
        // We don't bother with an assertion to confirm autoDetectFontsForPDF
        // was used. The main tests verify that behavior.
    }
}
