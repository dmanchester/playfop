package com.dmanchester.playfop.internal_j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.fo.FOTreeBuilder;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.Typeface;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.xmlgraphics.util.MimeConstants;
import org.junit.Test;

import com.dmanchester.playfop.api_j.FOUserAgentBlock;
import com.dmanchester.playfop.api_j.PlayFop;
import com.dmanchester.playfop.api_j.ProcessOptions;

import play.twirl.api.Xml;
import play.twirl.api.XmlFormat;

public class PlayFopImplTest {

    private static final String PDF_TEXT = "Hello there";
    private static final String PDF_AUTHOR = "PlayFopTest";

    private static final Pattern BASE_14_FONT = Pattern.compile("Times|Courier|Helvetica|Symbol|Zapf");
    private static final Pattern NO_WHITESPACE = Pattern.compile("^\\w*$");

    private static final FOUserAgentBlock FO_USER_AGENT_BLOCK = new FOUserAgentBlock() {

        @Override
        public void withFOUserAgent(FOUserAgent foUserAgent) {
            foUserAgent.setAuthor(PDF_AUTHOR);
        }
    };

    private PlayFop playFop = new PlayFopImpl();

    @Test
    public void testProcess_xslfo_outputFormat() throws IOException {

        byte[] pdfBytes = playFop.process(wrapInXslfoDocument(PDF_TEXT), MimeConstants.MIME_PDF);
        checkText(pdfBytes, PDF_TEXT);
    }

    @Test
    public void testProcess_xslfo_outputFormat_autoDetectFontsForPDF() throws IOException {

        String fontFamily = chooseFontFamilyOutsideBase14WithSingleWordName();

        ProcessOptions processOptions = new ProcessOptions.Builder().
                autoDetectFontsForPDF(true).build();
        byte[] pdfBytes = playFop.process(wrapInXslfoDocument(PDF_TEXT, fontFamily), MimeConstants.MIME_PDF, processOptions);

        checkForFontFamily(pdfBytes, fontFamily);
    }

    @Test
    public void testProcess_xslfo_outputFormat_foUserAgentBlock() throws IOException {

        ProcessOptions processOptions = new ProcessOptions.Builder().
                foUserAgentBlock(FO_USER_AGENT_BLOCK).build();
        byte[] pdfBytes = playFop.process(wrapInXslfoDocument(PDF_TEXT), MimeConstants.MIME_PDF, processOptions);

        checkText(pdfBytes, PDF_TEXT);
        checkForAuthorFromFOUserAgentBlock(pdfBytes, PDF_AUTHOR);
    }

    @Test
    public void testNewFop_outputFormat_output() throws IOException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Fop fop = playFop.newFop(MimeConstants.MIME_PDF, output);
        process(wrapInXslfoDocument(PDF_TEXT), fop);

        checkText(output.toByteArray(), PDF_TEXT);
    }

    @Test
    public void testNewFop_outputFormat_output_autoDetectFontsForPDF() throws IOException {

        String fontFamily = chooseFontFamilyOutsideBase14WithSingleWordName();

        ProcessOptions processOptions = new ProcessOptions.Builder().
                autoDetectFontsForPDF(true).build();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Fop fop = playFop.newFop(MimeConstants.MIME_PDF, output, processOptions);
        process(wrapInXslfoDocument(PDF_TEXT, fontFamily), fop);

        checkForFontFamily(output.toByteArray(), fontFamily);
    }

    @Test
    public void testNewFop_outputFormat_output_foUserAgentBlock() throws IOException {

        ProcessOptions processOptions = new ProcessOptions.Builder().
                foUserAgentBlock(FO_USER_AGENT_BLOCK).build();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Fop fop = playFop.newFop(MimeConstants.MIME_PDF, output, processOptions);
        process(wrapInXslfoDocument(PDF_TEXT), fop);

        byte[] pdfBytes = output.toByteArray();
        checkText(pdfBytes, PDF_TEXT);
        checkForAuthorFromFOUserAgentBlock(pdfBytes, PDF_AUTHOR);
    }

    private Xml wrapInXslfoDocument(String text) {
        return wrapInXslfoDocument(text, null);
    }

    private Xml wrapInXslfoDocument(String text, String fontFamily) {

        String foBlockStartTag = (fontFamily == null) ? "<fo:block>" : "<fo:block font-family=\"" + fontFamily + "\">";
        String foBlock = foBlockStartTag + text + "</fo:block>";

        return XmlFormat.raw(
                "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">" +
                "    <fo:layout-master-set>" +
                "        <fo:simple-page-master master-name=\"label\">" +
                "            <fo:region-body region-name=\"xsl-region-body\"/>" +
                "        </fo:simple-page-master>" +
                "    </fo:layout-master-set>" +
                "    <fo:page-sequence master-reference=\"label\">" +
                "        <fo:flow flow-name=\"xsl-region-body\">" +
                             foBlock +
                "        </fo:flow>" +
                "    </fo:page-sequence>" +
                "</fo:root>");
    }

    private String chooseFontFamilyOutsideBase14WithSingleWordName() {

        List<String> fontFamilies = getFontFamilies();

        return IterableUtils.find(fontFamilies, new Predicate<String>() {

            @Override
            public boolean evaluate(String fontFamily) {

                boolean base14font = BASE_14_FONT.matcher(fontFamily).find();
                boolean noWhitespace = NO_WHITESPACE.matcher(fontFamily).find();

                return !base14font && noWhitespace;
            }
        });
    }

    private List<String> getFontFamilies() {

        ProcessOptions processOptions = new ProcessOptions.Builder().
                autoDetectFontsForPDF(true).build();

        Fop fop = playFop.newFop(MimeConstants.MIME_PDF, new ByteArrayOutputStream(), processOptions);

        FontInfo fontInfo;
        try {
            fontInfo = ((FOTreeBuilder)fop.getDefaultHandler()).getEventHandler().getFontInfo();
        } catch (FOPException ex) {
            throw new RuntimeException(ex);
        }

        Collection<Typeface> typefaces = fontInfo.getFonts().values();

        List<String> fontNames = new ArrayList<String>(typefaces.size());
        for (Typeface typeface : typefaces) {
            fontNames.add(typeface.getFullName());
        }

        return fontNames;
    }

    private void checkText(byte[] pdfBytes, String text) throws IOException {

        try (PDDocument pdDocument = toPDDocument(pdfBytes)) {

            String pdfText;
            try {
                pdfText = new PDFTextStripper().getText(pdDocument).trim();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            assertEquals(text, pdfText);
        }
    }

    private void checkForAuthorFromFOUserAgentBlock(byte[] pdfBytes, String author) throws IOException {

        try (PDDocument pdDocument = toPDDocument(pdfBytes)) {

            String pdfAuthor = pdDocument.getDocumentInformation().getAuthor();
            assertEquals(author, pdfAuthor);
        }
    }

    private void checkForFontFamily(byte[] pdfBytes, final String fontFamily) throws IOException {

        try (PDDocument pdDocument = toPDDocument(pdfBytes)) {

            PDPageTree pdPageTree = pdDocument.getDocumentCatalog().getPages();

            Set<String> fonts = new HashSet<>();

            for (PDPage pdPage : pdPageTree) {

                PDResources pdResources = pdPage.getResources();

                for (COSName fontName : pdResources.getFontNames()) {
                    try {
                        fonts.add(pdResources.getFont(fontName).getName());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            boolean fontFound = IterableUtils.matchesAny(fonts, new Predicate<String>() {
                @Override
                public boolean evaluate(String font) {
                    return font.contains(fontFamily);
                }
            });

            assertTrue(fontFound);
        }
    }

    private PDDocument toPDDocument(byte[] pdfBytes) throws IOException {

        return PDDocument.load(new ByteArrayInputStream(pdfBytes));
    }

    private void process(Xml xslfo, Fop fop) {

        try {

            Transformer transformer = TransformerFactory.newInstance().newTransformer();

            Source source = new StreamSource(new StringReader(xslfo.body()));

            Result result = new SAXResult(fop.getDefaultHandler());

            transformer.transform(source, result);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
