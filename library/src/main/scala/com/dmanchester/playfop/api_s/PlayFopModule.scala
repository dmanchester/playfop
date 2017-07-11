package com.dmanchester.playfop.api_s

import com.dmanchester.playfop.internal_s.PlayFopImpl

import play.api.Configuration
import play.api.Environment
import play.api.inject.Module

class PlayFopModule extends Module {

  def bindings(environment: Environment, configuration: Configuration) = Seq(
    bind[PlayFop].to[PlayFopImpl]
  )
}
