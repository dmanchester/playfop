package controllers

import java.io.ByteArrayOutputStream
import javax.inject.Inject
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
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import views.util.Calc
import com.dmanchester.playfop.api_s.PlayFop
import com.dmanchester.playfop.api.Units

class Application @Inject() (val playFop: PlayFop, val messagesApi: MessagesApi)
    extends Controller with I18nSupport {

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

    val fop = playFop.newFop(MimeConstants.MIME_PDF, new ByteArrayOutputStream(), autoDetectFontsForPDF = true)

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

        val imageURI: Option[String] = getImageURI(label.imageName)

        val mimeType = MimeConstants.MIME_PNG

        val labelWidthInMM = SingleLabelScaleFactor * Calc.labelWidth(SheetSizeAndWhiteSpaceInMM, SheetCols)
        val labelHeightInMM = SingleLabelScaleFactor * Calc.labelHeight(SheetSizeAndWhiteSpaceInMM, SheetRows)
        val intraLabelPaddingInMM = SingleLabelScaleFactor * SheetSizeAndWhiteSpaceInMM.intraLabelPadding

        Ok(
          playFop.process(
            views.xml.labelSingle.render(labelWidthInMM, labelHeightInMM, intraLabelPaddingInMM, mm, imageURI, label.scale(SingleLabelScaleFactor)),
            mimeType
          )
        ).as(mimeType)
      }
    )
  }

  private def getImageURI(imageName: Option[String]): Option[String] = {

    imageName.flatMap(imageName => ImageNamesToPaths.get(imageName)).
        map(imagePath => this.getClass().getClassLoader().getResource(imagePath).toString())
  }

  def generateLabelsSheetAsPDF() = Action { implicit request =>

    LabelForm.bindFromRequest.fold(
      formWithErrors => {
        // See comment above about not bothering with a nice presentation.
        BadRequest(formWithErrors.errorsAsJson)
      },
      label => {

        val imageURI: Option[String] = getImageURI(label.imageName)

        val mimeType = MimeConstants.MIME_PDF

        val foUserAgentBlock = { foUserAgent: FOUserAgent =>
          foUserAgent.setCreator(SheetPdfCreator)
        }

        Ok(
          playFop.process(
            views.xml.labelsSheet.render(SheetSizeAndWhiteSpaceInMM, mm, SheetRows, SheetCols, imageURI, label),
            mimeType,
            autoDetectFontsForPDF = true,
            foUserAgentBlock = foUserAgentBlock
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