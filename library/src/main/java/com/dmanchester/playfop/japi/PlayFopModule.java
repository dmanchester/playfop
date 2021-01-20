package com.dmanchester.playfop.japi;

import com.dmanchester.playfop.jinternal.PlayFopImpl;
import com.typesafe.config.Config;
import play.inject.Module;

import java.util.Collections;
import java.util.List;

/**
 * A Play <a href="https://www.playframework.com/documentation/2.8.x/api/scala/play/api/inject/Module.html"><code>Module</code></a>
 * for dependency-injecting PlayFOP into Java applications at runtime.
 */
public class PlayFopModule extends Module {

    @Override
    public List<play.inject.Binding<?>> bindings(play.Environment environment, Config config) {
        return Collections.singletonList(
                bindClass(PlayFop.class).to(PlayFopImpl.class)
        );
    }
}
