package com.dmanchester.playfop.internal_j;

import java.io.OutputStream;

import javax.inject.Singleton;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;

import com.dmanchester.playfop.api_j.FOUserAgentBlock;
import com.dmanchester.playfop.api_j.PlayFop;
import com.dmanchester.playfop.api_j.ProcessOptions;

import play.twirl.api.Xml;
import scala.Function1;
import scala.runtime.AbstractFunction1;
import scala.runtime.BoxedUnit;

/**
 * The standard implementation of {@link PlayFop}.
 *
 * Instances of this class are thread-safe. They may be used across multiple
 * threads.
 */
@Singleton
public class PlayFopImpl implements PlayFop {

    private static final ProcessOptions DEFAULT_PROCESS_OPTIONS = new ProcessOptions.Builder().build();

    private com.dmanchester.playfop.api_s.PlayFop playFopScala = new com.dmanchester.playfop.internal_s.PlayFopImpl();

    @Override
    public byte[] process(Xml xslfo, String outputFormat) {

        return process(xslfo, outputFormat, DEFAULT_PROCESS_OPTIONS);
    }

    @Override
    public byte[] process(Xml xslfo, String outputFormat, ProcessOptions processOptions) {

        Function1<FOUserAgent, BoxedUnit> blockAsFunction = new BlockAsFunction(processOptions.getFoUserAgentBlock());

        return playFopScala.process(xslfo, outputFormat, processOptions.isAutoDetectFontsForPDF(), blockAsFunction);
    }

    @Override
    public Fop newFop(String outputFormat, OutputStream output) {

        return newFop(outputFormat, output, DEFAULT_PROCESS_OPTIONS);
    }

    @Override
    public Fop newFop(String outputFormat, OutputStream output, ProcessOptions processOptions) {

        Function1<FOUserAgent, BoxedUnit> blockAsFunction = new BlockAsFunction(processOptions.getFoUserAgentBlock());

        return playFopScala.newFop(outputFormat, output, processOptions.isAutoDetectFontsForPDF(), blockAsFunction);
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
