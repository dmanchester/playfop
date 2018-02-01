package com.dmanchester.playfop.jinternal;

import java.io.OutputStream;

import javax.inject.Singleton;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;

import com.dmanchester.playfop.japi.FOUserAgentBlock;
import com.dmanchester.playfop.japi.PlayFop;
import com.dmanchester.playfop.japi.ProcessOptions;

import play.twirl.api.Xml;
import scala.Function1;
import scala.runtime.AbstractFunction1;
import scala.runtime.BoxedUnit;

/**
 * The standard implementation of {@link PlayFop}.
 *
 * While the code within this class is thread-safe, its same-named Scala
 * collaborator is PlayFOP's primary integration point with Apache FOP, and
 * there is an open question around the thread safety of Apache FOP itself. For
 * more information, see the "Thread Safety" discussion in the PlayFOP User
 * Guide.
 */
@Singleton
public class PlayFopImpl implements PlayFop {

    private static final ProcessOptions DEFAULT_PROCESS_OPTIONS = new ProcessOptions.Builder().build();

    private com.dmanchester.playfop.sapi.PlayFop playFopScala = new com.dmanchester.playfop.sinternal.PlayFopImpl();

    @Override
    public byte[] processTwirlXml(Xml xslfo, String outputFormat) {

        return processTwirlXml(xslfo, outputFormat, DEFAULT_PROCESS_OPTIONS);
    }

    @Override
    public byte[] processTwirlXml(Xml xslfo, String outputFormat, ProcessOptions processOptions) {

        Function1<FOUserAgent, BoxedUnit> blockAsFunction = new BlockAsFunction(processOptions.getFoUserAgentBlock());

        return playFopScala.processTwirlXml(xslfo, outputFormat, processOptions.isAutoDetectFontsForPDF(), blockAsFunction);
    }

    @Override
    public byte[] processStringXml(String xslfo, String outputFormat) {

        return processStringXml(xslfo, outputFormat, DEFAULT_PROCESS_OPTIONS);
    }

    @Override
    public byte[] processStringXml(String xslfo, String outputFormat, ProcessOptions processOptions) {

            Function1<FOUserAgent, BoxedUnit> blockAsFunction = new BlockAsFunction(processOptions.getFoUserAgentBlock());

            return playFopScala.processStringXml(xslfo, outputFormat, processOptions.isAutoDetectFontsForPDF(), blockAsFunction);
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
