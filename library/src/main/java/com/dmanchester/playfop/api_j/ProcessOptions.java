package com.dmanchester.playfop.api_j;

import org.apache.fop.apps.FOUserAgent;

/**
 * Packages processing options for {@link PlayFop} invocations. Instances can be
 * constructed via the {@link Builder} inner class.
 *
 * Instances are immutable and thread-safe unless mutability is injected via
 * the builder's <code>foUserAgentBlock</code> method.
 */
public class ProcessOptions {

    private boolean autoDetectFontsForPDF;
    private FOUserAgentBlock foUserAgentBlock;

    /**
     * Builder class for {@link ProcessOptions}. A builder instance can be
     * customized via its methods. Once customized, calling {@link #build()}
     * produces a {@link ProcessOptions}.
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
         * @param autoDetectFontsForPDF whether to auto-detect fonts
         * @return the Builder (for chaining method calls)
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
         * @param foUserAgentBlock the code block for the
         *        <code>FOUserAgent</code>
         * @return the Builder (for chaining method calls)
         */
        public Builder foUserAgentBlock(FOUserAgentBlock foUserAgentBlock) {
            this.foUserAgentBlock = foUserAgentBlock;
            return this;
        }

        /**
         * Produces a {@link ProcessOptions}.
         *
         * @return the ProcessOptions
         */
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
