package com.dmanchester.playfop.sapi

import com.dmanchester.playfop.internal.PlayFopImpl

import play.api.Configuration
import play.api.Environment
import play.api.inject.Module

class PlayFopModule extends Module {

  def bindings(environment: Environment, configuration: Configuration) = Seq(
    bind[PlayFop].to[PlayFopImpl]
  )
}
