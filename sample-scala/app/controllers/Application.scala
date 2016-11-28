package controllers

import java.io.ByteArrayOutputStream
import scala.collection.JavaConverters._
import scala.collection.immutable.ListMap
import models.Label
import models.PaperSizeAndWhiteSpace
import org.apache.fop.apps.FOUserAgent
import org.apache.fop.fo.FOTreeBuilder
import org.apache.xmlgraphics.util.MimeConstants
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.number
import play.api.data.Forms.optional
import play.api.data.Forms.text
import play.api.mvc.Action
import play.api.mvc.Controller
import views.util.Calc
import com.dmanchester.playfop.api.ClasspathURIResolver
import com.dmanchester.playfop.sapi.PlayFop
import com.dmanchester.playfop.api.Units

object Application extends Controller {

  private val SheetSizeAndWhiteSpaceInMM = new PaperSizeAndWhiteSpace(297, 210, 20, 10, 2)  // A4
  private val mm = new Units("mm", 1)
  private val SheetRows = 10
  private val SheetCols = 3
  private val SheetFilename = "labels-sheet.pdf"
  private val SheetPdfCreator = "PlayFOP Labels, Scala version";  // producer already references Apache FOP, so no need to do so again here

  private val FontSizesInPoints = (6 to 18)

  private val ImageNamesToPaths = ListMap(
    "Cityscape" -> "images/emoji_u1f306_cityscape.svg",
    "Sunrise" -> "images/emoji_u1f304_sunrise.svg",
    "Volcano" -> "images/emoji_u1f30b_volcano.svg"
  )

  private val InitialText =
    """John Smith
      |123 Main St.
      |Anytown, MA  09876""".stripMargin
  private val InitialFontFamily = "DejaVu Sans Condensed"
  private val InitialFontSizeInPoints = 9
  private val InitialImageName = "Cityscape"

  private val SingleLabelScaleFactor = 3
  
  private val FopConfigPDFAutoDetectFonts =
      <fop version="1.0">
        <renderers>
          <renderer mime="application/pdf">
            <fonts>
              <auto-detect/>
            </fonts>
          </renderer>
        </renderers>
      </fop>

  private val LabelForm = Form(
    mapping(
      "text" -> text(maxLength = Label.TextMaxLength),
      "fontFamily" -> text,
      "fontSizeInPoints" -> number,
      "imageName" -> optional(text)
    )(Label.apply)(Label.unapply)
  )
  
  def index() = Action {
    Redirect(routes.Application.designLabels())
  }

  def designLabels() = Action {
    val imageNames = "" :: ImageNamesToPaths.keys.toList  // at top of list, offer a no-image option
    Ok(views.html.labelDesign(getInitialForm(), getFontFamilies(), getFontSizesAndText(), imageNames))
  }

  private def getInitialForm() = {

    // Confirm the initial font family is available.
    val fontFamilies = getFontFamilies()
    val initialFontFamily = if (fontFamilies.contains(InitialFontFamily))
      InitialFontFamily
    else
      fontFamilies.head  // we presume the List has at least one element

    val label =  Label(InitialText, InitialFontFamily, InitialFontSizeInPoints, Some(InitialImageName))

    LabelForm.fill(label)
  }

  private def getFontFamilies() = {
    
    val fop = PlayFop.newFop(MimeConstants.MIME_PDF, new ByteArrayOutputStream(), FopConfigPDFAutoDetectFonts)

    val fontInfo = fop.getDefaultHandler().asInstanceOf[FOTreeBuilder].getEventHandler().getFontInfo()

    val typefaces = fontInfo.getFonts().values().asScala

    typefaces.map(_.getFullName()).toList.sorted
  }

  private def getFontSizesAndText() = {
    val fontSizesAndTextAsSeq = FontSizesInPoints.map { size => (size.toString, size + " points") }
    ListMap(fontSizesAndTextAsSeq:_*)
  }

  def generateSingleLabelAsPNG() = Action { implicit request =>

    LabelForm.bindFromRequest.fold(
      formWithErrors => {
        // UI prevents user from entering bad input; only a hand-rolled input
        // URL can trigger errors. We want to alert user to those errors, but we
        // don't bother with a nice presentation.
        BadRequest(formWithErrors.errorsAsJson)
      },
      label => {
        val classpathURIResolver = new ClasspathURIResolver()
        val imageURI = label.imageName.
          flatMap(ImageNamesToPaths.get(_)).
          map(classpathURIResolver.createHref(_))

        val mimeType = MimeConstants.MIME_PNG

        val foUserAgentBlock = { foUserAgent: FOUserAgent =>
          foUserAgent.setURIResolver(classpathURIResolver)
        }

        val labelWidthInMM = SingleLabelScaleFactor * Calc.labelWidth(SheetSizeAndWhiteSpaceInMM, SheetCols)
        val labelHeightInMM = SingleLabelScaleFactor * Calc.labelHeight(SheetSizeAndWhiteSpaceInMM, SheetRows)
        val intraLabelPaddingInMM = SingleLabelScaleFactor * SheetSizeAndWhiteSpaceInMM.intraLabelPadding

        Ok(
          PlayFop.process(
            views.xml.labelSingle.render(labelWidthInMM, labelHeightInMM, intraLabelPaddingInMM, mm, imageURI, label.scale(SingleLabelScaleFactor)),
            mimeType,
            foUserAgentBlock
          )
        ).as(mimeType)
      }
    )
  }

  def generateLabelsSheetAsPDF() = Action { implicit request =>

    LabelForm.bindFromRequest.fold(
      formWithErrors => {
        // See comment above about not bothering with a nice presentation.
        BadRequest(formWithErrors.errorsAsJson)
      },
      label => {
        val classpathURIResolver = new ClasspathURIResolver()
        val imageURI = label.imageName.
          flatMap(ImageNamesToPaths.get(_)).
          map(classpathURIResolver.createHref(_))

        val mimeType = MimeConstants.MIME_PDF

        val foUserAgentBlock = { foUserAgent: FOUserAgent =>
          foUserAgent.setCreator(SheetPdfCreator)
          foUserAgent.setURIResolver(classpathURIResolver)
        }

        Ok(
          PlayFop.process(
            views.xml.labelsSheet.render(SheetSizeAndWhiteSpaceInMM, mm, SheetRows, SheetCols, imageURI, label),
            mimeType,
            FopConfigPDFAutoDetectFonts,
            foUserAgentBlock
          )
        ).as(mimeType).withHeaders(
          CONTENT_DISPOSITION -> s"attachment; filename=$SheetFilename"
        )
      }
    )
  }

  def showAbout() = Action {
    Ok(views.html.about())
  }
}