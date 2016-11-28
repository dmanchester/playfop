package com.dmanchester.playfop.japi;

import org.apache.fop.apps.FOUserAgent;

/**
 * To configure an <code>FOUserAgent</code>, implement this
 * interface, invoke the desired methods on the <code>FOUserAgent</code>, and
 * pass an instance of the implementation to a {@link PlayFop}
 * <code>process</code> or <code>newFop</code> method as appropriate.
 */
public interface FOUserAgentBlock {

	public void withFOUserAgent(FOUserAgent foUserAgent);
}
