PlayFOP Labels (Scala)
======================

PlayFOP Labels is a sample [Play Framework](https://www.playframework.com/) application for PlayFOP and [Apache FOP](https://xmlgraphics.apache.org/fop/).
It demonstrates generating PDFs and images (PNGs) from Play [Twirl](https://www.playframework.com/documentation/2.6.x/ScalaTemplates) templates.

This is the Scala version of PlayFOP Labels.

A Java version of PlayFOP Labels with equivalent functionality is [also available](../sample-java).

Miscellaneous
-------------

PlayFOP accepts an optional `about.page.addl.info` system option.
It can be set to the name of an HTML file whose contents should be added to the About page:

```
-Dabout.page.addl.info="/some-path/some-file.html"
```
