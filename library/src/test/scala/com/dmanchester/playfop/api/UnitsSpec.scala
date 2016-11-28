package com.dmanchester.playfop.api

import org.specs2.mutable.Specification

class UnitsSpec extends Specification {

  "constructor" should {
    "throw an IllegalArgumentException on formatPrecision < 0" in {
      new Units("mm", -1) must throwAn[IllegalArgumentException]
    }
  }

  "format(Double)" should {
    "support formatPrecision > 0" in {
      val units = new Units("mm", 1)
      units.format(2.34) must beEqualTo("2.3mm")
    }

    "support formatPrecision == 0" in {
      val units = new Units("mm", 0)
      units.format(2.34) must beEqualTo("2mm")
    }
  }

  "format(Int)" should {
    "support formatPrecision > 0" in {
      val units = new Units("cm", 1)
      units.format(5) must beEqualTo("5.0cm")
    }

    "support formatPrecision == 0" in {
      val units = new Units("cm", 0)
      units.format(5) must beEqualTo("5cm")
    }
  }

  "format" should {
    "support label == '%'" in {
      val units = new Units("%", 1)
      units.format(6.78) must beEqualTo("6.8%")
    }
  }
}