package com.dmanchester.playfop.japi;

import java.io.OutputStream;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;

import play.twirl.api.Xml;
import scala.Function1;
import scala.runtime.AbstractFunction1;
import scala.runtime.BoxedUnit;
import scala.xml.Elem;
import scala.xml.XML;

/**
 * The primary entry point into PlayFOP for Java applications.
 */
public class PlayFop {
    
    // A private constructor to prevent client code from instantiating this
    // class.
    private PlayFop() {}
    
    /**
     * Processes XSL-FO with Apache FOP. Generates output in the specified format.
     *
     * @param xslfo the XSL-FO to process
     * @param outputFormat the format to generate
     * @return the Apache FOP output
     */
    public static byte[] process(Xml xslfo, String outputFormat) {
        return com.dmanchester.playfop.sapi.PlayFop.process(xslfo, outputFormat);
    }
    
    /**
     * Processes XSL-FO with Apache FOP, configuring FOP with the configuration
     * XML. Generates output in the specified format.
     *
     * @param xslfo the XSL-FO to process
     * @param outputFormat the format to generate
     * @param fopConfig the configuration XML for FOP
     * @return the Apache FOP output
     */
    public static byte[] process(Xml xslfo, String outputFormat, String fopConfigXml) {
        return com.dmanchester.playfop.sapi.PlayFop.process(xslfo, outputFormat, toScalaXml(fopConfigXml));
    }
    
    /**
     * Processes XSL-FO with Apache FOP, configuring the <code>FOUserAgent</code> with the code block. Generates
     * output in the specified format.
     *
     * @param xslfo the XSL-FO to process
     * @param outputFormat the format to generate
     * @param foUserAgentBlock the code block for the `FOUserAgent`
     * @return the Apache FOP output
     */
    public static byte[] process(Xml xslfo, String outputFormat, FOUserAgentBlock foUserAgentBlock) {

        Function1<FOUserAgent, BoxedUnit> blockAsFunction = new BlockAsFunction(foUserAgentBlock);
        
        return com.dmanchester.playfop.sapi.PlayFop.process(xslfo, outputFormat, blockAsFunction);
    }

    /**
     * Processes XSL-FO with Apache FOP, configuring FOP with the configuration
     * XML and the <code>FOUserAgent</code> with the code block. Generates
     * output in the specified format.
     *
     * @param xslfo the XSL-FO to process
     * @param outputFormat the format to generate
     * @param fopConfig the configuration XML for FOP
     * @param foUserAgentBlock the code block for the `FOUserAgent`
     * @return the Apache FOP output
     */
    public static byte[] process(Xml xslfo, String outputFormat, String fopConfigXml, FOUserAgentBlock foUserAgentBlock) {

        Elem fopConfig = toScalaXml(fopConfigXml);
        Function1<FOUserAgent, BoxedUnit> blockAsFunction = new BlockAsFunction(foUserAgentBlock);
        
        return com.dmanchester.playfop.sapi.PlayFop.process(xslfo, outputFormat, fopConfig, blockAsFunction);
    }

    /**
     * Creates a new <code>Fop</code> instance. Sets it up to save output to the
     * supplied <code>OutputStream</code> in the supplied format.
     * <p/>
     * <b>Note:</b> The <code>newFop</code> methods are offered primarily for
     * client code to interrogate the Apache FOP environment (for example, to
     * determine available fonts). Code wishing to process XSL-FO with
     * Apache FOP should rely on a <code>process</code> method instead.
     *
     * @param outputFormat the format the <code>Fop</code> should generate
     * @param output the <code>OutputStream</code> to which the <code>Fop</code>
     *        should save output
     * @return the <code>Fop</code>
     */
    public static Fop newFop(String outputFormat, OutputStream output) {
        return com.dmanchester.playfop.sapi.PlayFop.newFop(outputFormat, output);
    }

    /**
     * Creates a new <code>Fop</code> instance, configuring it with the
     * configuration XML. Sets up the <code>Fop</code> to save output to the
     * supplied <code>OutputStream</code> in the supplied format.
     * <p/>
     * <b>Note:</b> The <code>newFop</code> methods are offered primarily for
     * client code to interrogate the Apache FOP environment (for example, to
     * determine available fonts). Code wishing to process XSL-FO with
     * Apache FOP should rely on a <code>process</code> method instead.
     *
     * @param outputFormat the format the <code>Fop</code> should generate
     * @param output the <code>OutputStream</code> to which the <code>Fop</code>
     *        should save output
     * @param fopConfig the configuration XML for the <code>Fop</code>
     * @return the <code>Fop</code>
     */
    public static Fop newFop(String outputFormat, OutputStream output, String fopConfigXml) {
        Elem fopConfig = toScalaXml(fopConfigXml);
        return com.dmanchester.playfop.sapi.PlayFop.newFop(outputFormat, output, fopConfig);
    }

    /**
     * Creates a new <code>Fop</code> instance, configuring its
     * <code>FOUserAgent</code> with the code block. Sets up the
     * <code>Fop</code> to save output to the supplied <code>OutputStream</code>
     * in the supplied format.
     * <p/>
     * <b>Note:</b> The <code>newFop</code> methods are offered primarily for
     * client code to interrogate the Apache FOP environment (for example, to
     * determine available fonts). Code wishing to process XSL-FO with
     * Apache FOP should rely on a <code>process</code> method instead.
     *
     * @param outputFormat the format the <code>Fop</code> should generate
     * @param output the <code>OutputStream</code> to which the <code>Fop</code>
     *        should save output
     * @param foUserAgentBlock the code block for the <code>Fop</code>'s
     *        <code>FOUserAgent</code>
     * @return the <code>Fop</code>
     */
    public static Fop newFop(String outputFormat, OutputStream output, FOUserAgentBlock foUserAgentBlock) {
        Function1<FOUserAgent, BoxedUnit> blockAsFunction = new BlockAsFunction(foUserAgentBlock);
        return com.dmanchester.playfop.sapi.PlayFop.newFop(outputFormat, output, blockAsFunction);
    }

    /**
     * Creates a new <code>Fop</code> instance, configuring the <code>Fop</code>
     * with the configuration XML and its <code>FOUserAgent</code> with the code
     * block. Sets up the <code>Fop</code> to save output to the supplied
     * <code>OutputStream</code> in the supplied format.
     * <p/>
     * <b>Note:</b> The <code>newFop</code> methods are offered primarily for
     * client code to interrogate the Apache FOP environment (for example, to
     * determine available fonts). Code wishing to process XSL-FO with
     * Apache FOP should rely on a <code>process</code> method instead.
     *
     * @param outputFormat the format the <code>Fop</code> should generate
     * @param output the <code>OutputStream</code> to which the <code>Fop</code>
     *        should save output
     * @param fopConfig the configuration XML for the <code>Fop</code>
     * @param foUserAgentBlock the code block for the <code>Fop</code>'s
     *        <code>FOUserAgent</code>
     * @return the <code>Fop</code>
     */
    public static Fop newFop(String outputFormat, OutputStream output, String fopConfigXml, FOUserAgentBlock foUserAgentBlock) {

        Elem fopConfig = toScalaXml(fopConfigXml);
        Function1<FOUserAgent, BoxedUnit> blockAsFunction = new BlockAsFunction(foUserAgentBlock);

        return com.dmanchester.playfop.sapi.PlayFop.newFop(outputFormat, output, fopConfig, blockAsFunction);
    }

    private static Elem toScalaXml(String xml) {
        return (Elem)XML.loadString(xml);
    }

    private static class BlockAsFunction extends AbstractFunction1<FOUserAgent, BoxedUnit> {

        private FOUserAgentBlock block;

        public BlockAsFunction(FOUserAgentBlock block) {
            this.block = block;
        }

        @Override
        public BoxedUnit apply(FOUserAgent foUserAgent) {
            block.withFOUserAgent(foUserAgent);
            return BoxedUnit.UNIT;
        }
    }
}
