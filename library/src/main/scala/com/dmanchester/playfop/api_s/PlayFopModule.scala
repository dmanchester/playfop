package com.dmanchester.playfop.api_s

import com.dmanchester.playfop.internal_s.PlayFopImpl

import play.api.Configuration
import play.api.Environment
import play.api.inject.Module

/** A Play `[[https://playframework.com/documentation/2.6.x/api/scala/index.html#play.api.inject.Module Module]]`
  * for dependency-injecting PlayFOP into Scala applications.
  */
class PlayFopModule extends Module {

  def bindings(environment: Environment, configuration: Configuration) = Seq(
    bind[PlayFop].to[PlayFopImpl]
  )
}
