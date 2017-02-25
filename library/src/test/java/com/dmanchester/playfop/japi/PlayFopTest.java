package com.dmanchester.playfop.japi;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.xmlgraphics.util.MimeConstants;
import org.junit.Test;

import play.twirl.api.Xml;
import play.twirl.api.XmlFormat;

public class PlayFopTest {

    private static final String PDF_TEXT = "Hello there";
    private static final String PDF_AUTHOR = "PlayFopTest";
    
    private static final Xml XSLFO = XmlFormat.raw(
        "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">" +
        "    <fo:layout-master-set>" +
        "        <fo:simple-page-master master-name=\"label\">" +
        "            <fo:region-body region-name=\"xsl-region-body\"/>" +
        "        </fo:simple-page-master>" +
        "    </fo:layout-master-set>" +
        "    <fo:page-sequence master-reference=\"label\">" +
        "        <fo:flow flow-name=\"xsl-region-body\">" +
        "            <fo:block>" + PDF_TEXT + "</fo:block>" +
        "        </fo:flow>" +
        "    </fo:page-sequence>" +
        "</fo:root>");

    private static final FOUserAgentBlock FO_USER_AGENT_BLOCK = new FOUserAgentBlock() {
        
        @Override
        public void withFOUserAgent(FOUserAgent foUserAgent) {
            foUserAgent.setAuthor(PDF_AUTHOR);
        }
    };
    
    @Test
    public void testProcess_xslfo_outputFormat() {

        byte[] pdfBytes = PlayFop.process(XSLFO, MimeConstants.MIME_PDF);
        PDDocument pdDocument = toPDDocument(pdfBytes);

        checkText(pdDocument);
    }

    @Test
    public void testProcess_xslfo_outputFormat_autoDetectFontsForPDF_foUserAgentBlock() {

        ProcessOptions processOptions = new ProcessOptions.Builder().
                autoDetectFontsForPDF(true).foUserAgentBlock(FO_USER_AGENT_BLOCK).build();
        byte[] pdfBytes = PlayFop.process(XSLFO, MimeConstants.MIME_PDF, processOptions);
        PDDocument pdDocument = toPDDocument(pdfBytes);

        checkText(pdDocument);
        checkForAuthorFromFOUserAgentBlock(pdDocument);

        // TODO How to validate that autoDetectFontsForPDF was consulted?
    }

    @Test
    public void testNewFop_outputFormat_output() {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Fop fop = PlayFop.newFop(MimeConstants.MIME_PDF, output);
        process(XSLFO, fop);
        PDDocument pdDocument = toPDDocument(output.toByteArray());

        checkText(pdDocument);
    }

    @Test
    public void testNewFop_outputFormat_output_autoDetectFontsForPDF_foUserAgentBlock() {

        ProcessOptions processOptions = new ProcessOptions.Builder().
                autoDetectFontsForPDF(true).foUserAgentBlock(FO_USER_AGENT_BLOCK).build();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Fop fop = PlayFop.newFop(MimeConstants.MIME_PDF, output, processOptions);
        process(XSLFO, fop);
        PDDocument pdDocument = toPDDocument(output.toByteArray());

        checkText(pdDocument);
        checkForAuthorFromFOUserAgentBlock(pdDocument);

        // TODO How to validate that autoDetectFontsForPDF was consulted?
    }

    private PDDocument toPDDocument(byte[] pdfBytes) {

        try {
            return PDDocument.load(new ByteArrayInputStream(pdfBytes));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkText(PDDocument pdDocument) {

        String pdfText;
        try {
            pdfText = new PDFTextStripper().getText(pdDocument).trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertEquals(PDF_TEXT, pdfText);
    }

    private void checkForAuthorFromFOUserAgentBlock(PDDocument pdDocument) {
        String pdfAuthor = pdDocument.getDocumentInformation().getAuthor();
        assertEquals(PDF_AUTHOR, pdfAuthor);
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
