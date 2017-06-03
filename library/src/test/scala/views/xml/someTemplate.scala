package views.xml

import com.dmanchester.playfop.TestHelpers

import play.twirl.api.Xml

import scala.xml.Text

/**
 * A companion object that mimics a compiled Twirl template (via its method
 * name, return type, and package).
 */
object someTemplate {

  def render(someArg: String): Xml = TestHelpers.wrapInXslfoDocument(Text(someArg))
}
