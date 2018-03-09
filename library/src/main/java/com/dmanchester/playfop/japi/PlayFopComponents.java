package com.dmanchester.playfop.japi;

import com.dmanchester.playfop.jinternal.PlayFopImpl;

/**
 * An interface for dependency-injecting PlayFOP into Java applications at
 * compile time.
 */
public interface PlayFopComponents {

    default PlayFop playFop() {
        return new PlayFopImpl();
    }
}
