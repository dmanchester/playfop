package views.xml

import com.dmanchester.playfop.TestHelpers

import play.twirl.api.Xml

/**
 * A companion object that mimics a compiled Twirl template (via its method
 * name, return type, and package).
 */
object someTwirlTemplate {

  def render(someArg: String): Xml = TestHelpers.wrapInTwirlXmlDocument(someArg)
}
