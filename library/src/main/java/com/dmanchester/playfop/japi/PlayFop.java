package com.dmanchester.playfop.japi;

import java.io.OutputStream;

import org.apache.fop.apps.Fop;

import play.twirl.api.Xml;

/**
 * The primary entry point into PlayFOP for Java applications.
 */
public interface PlayFop {

    /**
     * Processes XSL-FO with Apache FOP. Generates output in the specified
     * format.
     *
     * @param xslfo the XSL-FO to process
     * @param outputFormat the format to generate
     * @return the Apache FOP output
     */
    public byte[] process(Xml xslfo, String outputFormat);

    /**
     * Processes XSL-FO with Apache FOP, applying the processing options.
     * Generates output in the specified format.
     *
     * @param xslfo the XSL-FO to process
     * @param outputFormat the format to generate
     * @param processOptions the processing options
     * @return the Apache FOP output
     */
    public byte[] process(Xml xslfo, String outputFormat, ProcessOptions processOptions);

    /**
     * Creates a new <code>Fop</code> instance. Sets it up to save output to the
     * supplied <code>OutputStream</code> in the supplied format.
     * <p>
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
    public Fop newFop(String outputFormat, OutputStream output);

    /**
     * Creates a new <code>Fop</code> instance, configuring it with the
     * processing options. Sets it up to save output to the supplied
     * <code>OutputStream</code> in the supplied format.
     * <p>
     * <b>Note:</b> This method is offered primarily for client code to
     * interrogate the Apache FOP environment (for example, to determine
     * available fonts). Code wishing to process XSL-FO with Apache FOP should
     * rely on a <code>process</code> method instead.
     *
     * @param outputFormat the format the <code>Fop</code> should generate
     * @param output the <code>OutputStream</code> to which the <code>Fop</code>
     *        should save output
     * @param processOptions the processing options
     * @return the <code>Fop</code>
     */
    public Fop newFop(String outputFormat, OutputStream output, ProcessOptions processOptions);
}
