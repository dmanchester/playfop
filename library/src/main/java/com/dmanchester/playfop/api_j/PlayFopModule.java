package com.dmanchester.playfop.api_j;

import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;

import com.dmanchester.playfop.internal_j.PlayFopImpl;

public class PlayFopModule extends Module {

    @Override
    public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
        return seq(
            bind(PlayFop.class).to(PlayFopImpl.class)
        );
    }
}
