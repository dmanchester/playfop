package com.dmanchester.playfop

import org.specs2.specification.Scope

import com.dmanchester.playfop.sapi.PlayFop
import com.dmanchester.playfop.sinternal.PlayFopImpl

/** Makes a PlayFop instance available to a specs2 test.
  */
trait playFopBlock extends Scope {
  val playFop: PlayFop = new PlayFopImpl()
}
