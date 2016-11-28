package com.dmanchester.playfop.internal

import org.specs2.mutable.Specification

class MutableRefSpec extends Specification {

  "get()" should {
    "return None on a new instance" in {
      new MutableRef[AnyRef].get must beNone
    }

    "return Some(text) after set(text) is called" in {
      val text = "hi"
      val mutableRef = new MutableRef[String]
      mutableRef.set(text)
      mutableRef.get must beSome(text)
    }
  }
}