package com.dmanchester.playfop.sapi

import com.dmanchester.playfop.sinternal.PlayFopImpl

/** A trait for dependency-injecting PlayFOP into Scala applications at
  * compile time.
  */
trait PlayFopComponents {
  // TODO Should this be a singleton?
  lazy val playFop: PlayFop = new PlayFopImpl()
}
