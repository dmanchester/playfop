package com.dmanchester.playfop.japi;

import java.io.OutputStream;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;

import play.twirl.api.Xml;
import scala.Function1;
import scala.runtime.AbstractFunction1;
import scala.runtime.BoxedUnit;

/**
 * The primary entry point into PlayFOP for Java applications.
 */
public class PlayFop {

	private static final ProcessOptions DEFAULT_PROCESS_OPTIONS = new ProcessOptions.Builder().build();

    // A private constructor to prevent client code from instantiating this
    // class.
    private PlayFop() {}

    /**
     * Processes XSL-FO with Apache FOP. Generates output in the specified
     * format.
     *
     * @param xslfo the XSL-FO to process
     * @param outputFormat the format to generate
     * @return the Apache FOP output
     */
    public static byte[] process(Xml xslfo, String outputFormat) {

        return process(xslfo, outputFormat, DEFAULT_PROCESS_OPTIONS);
    }

    /**
     * Processes XSL-FO with Apache FOP, applying the processing options.
     * Generates output in the specified format.
     *
     * @param xslfo the XSL-FO to process
     * @param outputFormat the format to generate
     * @param processOptions the processing options
     * @return the Apache FOP output
     */
    public static byte[] process(Xml xslfo, String outputFormat, ProcessOptions processOptions) {

        Function1<FOUserAgent, BoxedUnit> blockAsFunction = new BlockAsFunction(processOptions.getFoUserAgentBlock());

        return com.dmanchester.playfop.sapi.PlayFop.process(xslfo, outputFormat, processOptions.isAutoDetectFontsForPDF(), blockAsFunction);
    }

    /**
     * Creates a new <code>Fop</code> instance. Sets it up to save output to the
     * supplied <code>OutputStream</code> in the supplied format.
     * <p/>
     * <b>Note:</b> This method is offered primarily for client code to
     * interrogate the Apache FOP environment (for example, to determine
     * available fonts). Code wishing to process XSL-FO with Apache FOP should
     * rely on the <code>process</code> method instead.
     *
     * @param outputFormat the format the <code>Fop</code> should generate
     * @param output the <code>OutputStream</code> to which the <code>Fop</code>
     *        should save output
     * @return the <code>Fop</code>
     */
    public static Fop newFop(String outputFormat, OutputStream output) {

        return newFop(outputFormat, output, DEFAULT_PROCESS_OPTIONS);
    }

    /**
     * Creates a new <code>Fop</code> instance, configuring it with the
     * processing options. Sets it up to save output to the supplied
     * <code>OutputStream</code> in the supplied format.
     * <p/>
     * <b>Note:</b> This method is offered primarily for client code to
     * interrogate the Apache FOP environment (for example, to determine
     * available fonts). Code wishing to process XSL-FO with Apache FOP should
     * rely on the <code>process</code> method instead.
     *
     * @param outputFormat the format the <code>Fop</code> should generate
     * @param output the <code>OutputStream</code> to which the <code>Fop</code>
     *        should save output
     * @param processOptions the processing options
     * @return the <code>Fop</code>
     */
    public static Fop newFop(String outputFormat, OutputStream output, ProcessOptions processOptions) {

        Function1<FOUserAgent, BoxedUnit> blockAsFunction = new BlockAsFunction(processOptions.getFoUserAgentBlock());

        return com.dmanchester.playfop.sapi.PlayFop.newFop(outputFormat, output, processOptions.isAutoDetectFontsForPDF(), blockAsFunction);
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
