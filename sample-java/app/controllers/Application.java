package controllers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.fo.FOTreeBuilder;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.Typeface;
import org.apache.xmlgraphics.util.MimeConstants;

import com.dmanchester.playfop.api.Units;
import com.dmanchester.playfop.api_j.FOUserAgentBlock;
import com.dmanchester.playfop.api_j.PlayFop;
import com.dmanchester.playfop.api_j.ProcessOptions;

import models.Label;
import models.PaperSizeAndWhiteSpace;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http.HeaderNames;
import play.mvc.Result;
import views.util.Calc;

public class Application extends Controller {

    private static final PaperSizeAndWhiteSpace SHEET_SIZE_AND_WHITESPACE_IN_MM =
            new PaperSizeAndWhiteSpace(297 /* height (A4) */,
            210 /* width (A4) */, 20 /* margin */, 10 /* interLabelGutter */,
            2 /* intraLabelPadding */);

    private static final Units MM = new Units("mm", 1);
    private static final int SHEET_ROWS = 10;
    private static final int SHEET_COLS = 3;
    private static final String SHEET_FILENAME = "labels-sheet.pdf";
    private static final String SHEET_PDF_CREATOR = "PlayFOP Labels, Java version";  // producer already references Apache FOP, so no need to do so again here

    private static final int FONT_SIZE_IN_POINTS__START = 6;
    private static final int FONT_SIZE_IN_POINTS__END = 18;
    private static final String IMAGE_NAME__CITYSCAPE = "Cityscape";
    private static final String IMAGE_NAME__SUNRISE = "Sunrise";
    private static final String IMAGE_NAME__VOLCANO = "Volcano";
    private static final Map<String, String> IMAGE_NAMES_TO_PATHS = Collections.unmodifiableMap(getImageNamesToPaths());

    private static final String INITIAL_TEXT =
            "John Smith\n" +
            "123 Main St.\n" +
            "Anytown, MA  09876";
    private static final String INITIAL_FONT_FAMILY = "DejaVu Sans Condensed";
    private static final Integer INITIAL_FONT_SIZE_IN_POINTS = 9;
    private static final String INITIAL_IMAGE_NAME = IMAGE_NAME__CITYSCAPE;

    private static final int SINGLE_LABEL_SCALE_FACTOR = 3;

    private static final String PLAYFOP_URL = "https://www.dmanchester.com/playfop";

    private static final String ABOUT_PAGE__ADDL_INFO_PROPERTY = "about.page.addl.info";

    private static Map<String, String> getImageNamesToPaths() {

        Map<String, String> imageNamesToPaths = new LinkedHashMap<String, String>();

        imageNamesToPaths.put(IMAGE_NAME__CITYSCAPE, "images/emoji_u1f306_cityscape.svg");
        imageNamesToPaths.put(IMAGE_NAME__SUNRISE, "images/emoji_u1f304_sunrise.svg");
        imageNamesToPaths.put(IMAGE_NAME__VOLCANO, "images/emoji_u1f30b_volcano.svg");

        return imageNamesToPaths;
    }

    private FormFactory formFactory;
    private PlayFop playFop;

    @Inject
    public Application(FormFactory formFactory, PlayFop playFop) {
        this.formFactory = formFactory;
        this.playFop = playFop;
    }

    public Result index() {
        return redirect(controllers.routes.Application.designLabels());
    }

    public Result designLabels() {

        List<String> fontFamilies = getFontFamilies();
        List<String> imageNames = new ArrayList<String>(IMAGE_NAMES_TO_PATHS.keySet());
        imageNames.add(0, "");  // offer a no-image option

        return ok(views.html.labelDesign.render(getInitialForm(), fontFamilies, getFontSizesAndText(), imageNames));
    }

    private Form<Label> getInitialForm() {

        Label initialLabel = new Label();
        initialLabel.setText(INITIAL_TEXT);
        initialLabel.setFontSizeInPoints(INITIAL_FONT_SIZE_IN_POINTS);
        initialLabel.setImageName(INITIAL_IMAGE_NAME);

        // Confirm the initial font family is available.
        List<String> fontFamilies = getFontFamilies();
        if (fontFamilies.contains(INITIAL_FONT_FAMILY)) {
            initialLabel.setFontFamily(INITIAL_FONT_FAMILY);
        } else {
            initialLabel.setFontFamily(fontFamilies.get(0));    // we presume the List has at least one element
        }

        return formFactory.form(Label.class).fill(initialLabel);
    }

    private List<String> getFontFamilies() {

        ProcessOptions processOptions = new ProcessOptions.Builder().
                autoDetectFontsForPDF(true).build();

        Fop fop = playFop.newFop(MimeConstants.MIME_PDF, new ByteArrayOutputStream(), processOptions);

        FontInfo fontInfo;
        try {
            fontInfo = ((FOTreeBuilder)fop.getDefaultHandler()).getEventHandler().getFontInfo();
        } catch (FOPException ex) {
            throw new RuntimeException(ex);
        }

        Collection<Typeface> typefaces = fontInfo.getFonts().values();

        List<String> fontNames = new ArrayList<String>(typefaces.size());
        for (Typeface typeface : typefaces) {
            fontNames.add(typeface.getFullName());
        }

        Collections.sort(fontNames);

        return fontNames;
    }

    private Map<String, String> getFontSizesAndText() {

        Map<String, String> fontSizes = new LinkedHashMap<String, String>(FONT_SIZE_IN_POINTS__END - FONT_SIZE_IN_POINTS__START + 1);

        for (int i = FONT_SIZE_IN_POINTS__START; i <= FONT_SIZE_IN_POINTS__END; i++) {
            fontSizes.put(Integer.toString(i), i + " points");
        }

        return fontSizes;
    }

    public Result generateSingleLabelAsPNG() {

        Form<Label> labelForm = formFactory.form(Label.class).bindFromRequest();
        if (labelForm.hasErrors()) {
            // UI prevents user from entering bad input; only a hand-rolled
            // input URL can trigger errors. We want to alert user to those
            // errors, but we don't bother with a nice presentation.
            return badRequest(labelForm.errorsAsJson());
        }

        Label label = labelForm.get();

        String imageURI = getImageURI(label.getImageName());
        // In the case of a null imageURI, template simply doesn't show an
        // image.

        String mimeType = MimeConstants.MIME_PNG;

        double labelWidthInMM = SINGLE_LABEL_SCALE_FACTOR * Calc.getLabelWidth(SHEET_SIZE_AND_WHITESPACE_IN_MM, SHEET_COLS);
        double labelHeightInMM = SINGLE_LABEL_SCALE_FACTOR * Calc.getLabelHeight(SHEET_SIZE_AND_WHITESPACE_IN_MM, SHEET_ROWS);
        double intraLabelPaddingInMM = SINGLE_LABEL_SCALE_FACTOR * SHEET_SIZE_AND_WHITESPACE_IN_MM.getIntraLabelPadding();

        return ok(playFop.process(
                views.xml.labelSingle.render(labelWidthInMM, labelHeightInMM, intraLabelPaddingInMM, MM, imageURI, label.scale(SINGLE_LABEL_SCALE_FACTOR)),
                mimeType
            )).as(mimeType);
    }

    private String getImageURI(String imageName) {

        String imagePath = IMAGE_NAMES_TO_PATHS.get(imageName);
        String imageURI = (imagePath == null) ? null :
            Application.class.getClassLoader().getResource(imagePath).toString();

        return imageURI;
    }

    public Result generateLabelsSheetAsPDF() {

        Form<Label> labelForm = formFactory.form(Label.class).bindFromRequest();
        if (labelForm.hasErrors()) {
            // See comment above about not bothering with a nice presentation.
            return badRequest(labelForm.errorsAsJson());
        }

        Label label = labelForm.get();

        String imageURI = getImageURI(label.getImageName());
        // See comment above about a null imageURI leading the template to not
        // show an image.

        String mimeType = MimeConstants.MIME_PDF;

        FOUserAgentBlock foUserAgentBlock = new FOUserAgentBlock() {
            @Override
            public void withFOUserAgent(FOUserAgent foUserAgent) {
                foUserAgent.setCreator(SHEET_PDF_CREATOR);
            }
        };

        ProcessOptions processOptions = new ProcessOptions.Builder().
                autoDetectFontsForPDF(true).foUserAgentBlock(foUserAgentBlock).build();

        String contentDispHeader = String.format("attachment; filename=%s", SHEET_FILENAME);
        response().setHeader(HeaderNames.CONTENT_DISPOSITION, contentDispHeader);

        return ok(playFop.process(
                views.xml.labelsSheet.render(SHEET_SIZE_AND_WHITESPACE_IN_MM, MM, SHEET_ROWS, SHEET_COLS, imageURI, label),
                mimeType,
                processOptions
            )).as(mimeType);
    }

    public Result showAbout() {

        String addlInfoAsHtml;
        String addlInfoPath = System.getProperty(ABOUT_PAGE__ADDL_INFO_PROPERTY);

        if (addlInfoPath == null) {

            addlInfoAsHtml = null;

        } else {

            Path addlInfoPathObj = Paths.get(addlInfoPath);

            try {

                byte[] addlInfoAsBytes = Files.readAllBytes(addlInfoPathObj);
                addlInfoAsHtml = new String(addlInfoAsBytes, "utf-8");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return ok(views.html.about.render(PLAYFOP_URL, addlInfoAsHtml));
    }
}
