PlayFOP Labels (Scala)
======================

PlayFOP Labels is a sample [Play Framework](https://www.playframework.com/) application for PlayFOP and [Apache FOP](https://xmlgraphics.apache.org/fop/).
It demonstrates generating PDFs and images (PNGs) from Play [Twirl](https://www.playframework.com/documentation/2.6.x/ScalaTemplates) templates.

This is the Scala version of PlayFOP Labels.

A Java version of PlayFOP Labels with equivalent functionality is [also available](../sample-java).

Running PlayFOP Labels
----------------------

To run PlayFOP Labels, [configure](https://www.playframework.com/documentation/2.6.x/AllowedHostsFilter#Configuring-allowed-hosts) the [AllowedHostsFilter](https://www.playframework.com/documentation/2.6.x/AllowedHostsFilter).
Then, start `sbt` and enter `run`.

PlayFOP Labels supports the following optional system properties, passed via `-D` on application startup:

* `about.page.addl.info`: The name and path of an HTML file whose contents should be added to the About page.
* `font.family.exclusion.regex`: A regular expression indicating the font families that should be excluded from the Design page's drop-down list.

  **Note:** The application excludes font families whose names match _any_ portion of the regular expression. To exclude only names against which the regex fully matches, apply `^` and `$` anchor characters to the regex.
* `initial.font.family`: The font family that should be initially chosen on the Design page.
