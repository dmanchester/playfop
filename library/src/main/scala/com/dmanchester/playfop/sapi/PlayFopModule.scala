package com.dmanchester.playfop.sapi

import com.dmanchester.playfop.sinternal.PlayFopImpl

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
