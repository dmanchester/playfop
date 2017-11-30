package com.dmanchester.playfop.japi;

import com.dmanchester.playfop.jinternal.PlayFopImpl;

import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;

/**
 * A Play <a href="https://playframework.com/documentation/2.6.x/api/scala/index.html#play.api.inject.Module"><code>Module</code></a>
 * for dependency-injecting PlayFOP into Java applications.
 */
public class PlayFopModule extends Module {

    @Override
    public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
        return seq(
            bind(PlayFop.class).to(PlayFopImpl.class)
        );
    }
}
