package com.dmanchester.playfop.sinternal

import org.specs2.mutable.Specification

class FopFactoryCacheSpec extends Specification {

  private val BogusFopVersion = "123.456"
  private val FopConfig =
    <fop version={BogusFopVersion}/>

  "get()" should {
    "return the same instance when called repeatedly without fopConfigXml" in {
      val cache = new FopFactorySource()
      cache.get(None) must beTheSameAs(cache.get(None))
    }

    "return the same instance when called repeatedly with the same fopConfigXml" in {
      val cache = new FopFactorySource()
      cache.get(Some(FopConfig)) must beTheSameAs(
        cache.get(Some(FopConfig))
      )
    }

    "apply the fopConfigXml it receives" in {
      val cache = new FopFactorySource()
      val factory = cache.get(Some(FopConfig))
      factory.getUserConfig.getAttribute("version") must beEqualTo(BogusFopVersion)
    }
  }
}