package com.dmanchester.playfop.api_j;

import org.apache.fop.apps.FOUserAgent;

/**
 * Processing options for <code>PlayFop</code> invocations. Instances can be
 * constructed via the Builder inner class.
 *
 * Instances are immutable and thread-safe unless mutability is injected via
 * the builder's <code>foUserAgentBlock</code> method.
 */
public class ProcessOptions {

    private boolean autoDetectFontsForPDF;
    private FOUserAgentBlock foUserAgentBlock;

    /**
     * Builder class for <code>ProcessOptions<code>. A builder instance can be
     * customized via its methods. Once customized, calling <code>build()</code>
     * produces a <code>ProcessOptions<code>.
     */
    public static class Builder {

        // default attributes of an instance
        private boolean autoDetectFontsForPDF = false;
        private FOUserAgentBlock foUserAgentBlock = new FOUserAgentBlock() {

            @Override
            public void withFOUserAgent(FOUserAgent foUserAgent) { /* no-op */ }
        };

        /**
         * Whether operating system fonts should be auto-detected and made
         * available to Apache FOP. Only relevant to PDF output.
         *
         * @param autoDetectFontsForPDF
         * @return
         */
        public Builder autoDetectFontsForPDF(boolean autoDetectFontsForPDF) {
            this.autoDetectFontsForPDF = autoDetectFontsForPDF;
            return this;
        }

        /**
         * Specifies a block of code that acts on an Apache FOP
         * <code>FOUserAgent</code> to customize output (e.g., to set the author
         * of a PDF file).
         *
         * @param foUserAgentBlock
         * @return
         */
        public Builder foUserAgentBlock(FOUserAgentBlock foUserAgentBlock) {
            this.foUserAgentBlock = foUserAgentBlock;
            return this;
        }

        public ProcessOptions build() {
            return new ProcessOptions(this);
        }
    }

    private ProcessOptions(Builder builder) {
        this.autoDetectFontsForPDF = builder.autoDetectFontsForPDF;
        this.foUserAgentBlock = builder.foUserAgentBlock;
    }

    public boolean isAutoDetectFontsForPDF() {
        return autoDetectFontsForPDF;
    }

    public FOUserAgentBlock getFoUserAgentBlock() {
        return foUserAgentBlock;
    }
}
