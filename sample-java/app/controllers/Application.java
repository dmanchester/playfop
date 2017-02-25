package controllers;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import models.Label;
import models.PaperSizeAndWhiteSpace;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.fo.FOTreeBuilder;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.Typeface;
import org.apache.xmlgraphics.util.MimeConstants;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.util.Calc;

import com.dmanchester.playfop.api.Units;
import com.dmanchester.playfop.japi.FOUserAgentBlock;
import com.dmanchester.playfop.japi.PlayFop;
import com.dmanchester.playfop.japi.ProcessOptions;

public class Application extends Controller {

    private static final PaperSizeAndWhiteSpace SHEET_SIZE_AND_WHITESPACE_IN_MM =
            new PaperSizeAndWhiteSpace(297, 210, 20, 10, 2);  // A4
    private static final Units MM = new Units("mm", 1);
    private static final int SHEET_ROWS = 10;
    private static final int SHEET_COLS = 3;
    private static final String SHEET_FILENAME = "labels-sheet.pdf";
    private static final String SHEET_PDF_CREATOR = "PlayFOP Labels, Java version";  // producer already references Apache FOP, so no need to do so again here

    private static final int FONT_SIZE_IN_POINTS__START = 6;
    private static final int FONT_SIZE_IN_POINTS__END = 18;
    private static final Map<String, String> IMAGE_NAMES_TO_PATHS = Collections.unmodifiableMap(getImageNamesToPaths());

    private static final String INITIAL_TEXT =
            "John Smith\n" +
            "123 Main St.\n" +
            "Anytown, MA  09876";
    private static final String INITIAL_FONT_FAMILY = "DejaVu Sans Condensed";
    private static final Integer INITIAL_FONT_SIZE_IN_POINTS = 9;
    private static final String INITIAL_IMAGE_NAME = "Cityscape";

    private static final int SINGLE_LABEL_SCALE_FACTOR = 3;

    private static Map<String, String> getImageNamesToPaths() {

        Map<String, String> imageNamesToPaths = new LinkedHashMap<String, String>();

        imageNamesToPaths.put("Cityscape", "images/emoji_u1f306_cityscape.svg");
        imageNamesToPaths.put("Sunrise", "images/emoji_u1f304_sunrise.svg");
        imageNamesToPaths.put("Volcano", "images/emoji_u1f30b_volcano.svg");

        return imageNamesToPaths;
    }

    public static Result index() {
        return redirect(controllers.routes.Application.designLabels());
    }

    public static Result designLabels() {

        List<String> fontFamilies = getFontFamilies();
        List<String> imageNames = new ArrayList<String>(IMAGE_NAMES_TO_PATHS.keySet());
        imageNames.add(0, "");  // offer a no-image option

        return ok(views.html.labelDesign.render(getInitialForm(), fontFamilies, getFontSizesAndText(), imageNames));
    }

    private static Form<Label> getInitialForm() {

        Label initialLabel = new Label();
        initialLabel.text = INITIAL_TEXT;
        initialLabel.fontSizeInPoints = INITIAL_FONT_SIZE_IN_POINTS;
        initialLabel.imageName = INITIAL_IMAGE_NAME;

        // Confirm the initial font family is available.
        List<String> fontFamilies = getFontFamilies();
        if (fontFamilies.contains(INITIAL_FONT_FAMILY)) {
            initialLabel.fontFamily = INITIAL_FONT_FAMILY;
        } else {
            initialLabel.fontFamily = fontFamilies.get(0);    // we presume the List has at least one element
        }

        return Form.form(Label.class).fill(initialLabel);
    }

    private static List<String> getFontFamilies() {

        ProcessOptions processOptions = new ProcessOptions.Builder().
                autoDetectFontsForPDF(true).build();

        Fop fop = PlayFop.newFop(MimeConstants.MIME_PDF, new ByteArrayOutputStream(), processOptions);

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

    private static Map<String, String> getFontSizesAndText() {

        Map<String, String> fontSizes = new LinkedHashMap<String, String>(FONT_SIZE_IN_POINTS__END - FONT_SIZE_IN_POINTS__START + 1);

        for (int i = FONT_SIZE_IN_POINTS__START; i <= FONT_SIZE_IN_POINTS__END; i++) {
            fontSizes.put(Integer.toString(i), i + " points");
        }

        return fontSizes;
    }

    public static Result generateSingleLabelAsPNG() {

        Form<Label> labelForm = Form.form(Label.class).bindFromRequest();
        if (labelForm.hasErrors()) {
            // UI prevents user from entering bad input; only a hand-rolled
            // input URL can trigger errors. We want to alert user to those
            // errors, but we don't bother with a nice presentation.
            return badRequest(labelForm.errorsAsJson());
        }

        Label label = labelForm.get();

        String imageURI = getImageURI(label.imageName);
        // In the case of a null imageURI, template simply doesn't show an
        // image.

        String mimeType = MimeConstants.MIME_PNG;

        double labelWidthInMM = SINGLE_LABEL_SCALE_FACTOR * Calc.getLabelWidth(SHEET_SIZE_AND_WHITESPACE_IN_MM, SHEET_COLS);
        double labelHeightInMM = SINGLE_LABEL_SCALE_FACTOR * Calc.getLabelHeight(SHEET_SIZE_AND_WHITESPACE_IN_MM, SHEET_ROWS);
        double intraLabelPaddingInMM = SINGLE_LABEL_SCALE_FACTOR * SHEET_SIZE_AND_WHITESPACE_IN_MM.getIntraLabelPadding();

        return ok(PlayFop.process(
                views.xml.labelSingle.render(labelWidthInMM, labelHeightInMM, intraLabelPaddingInMM, MM, imageURI, label.scale(SINGLE_LABEL_SCALE_FACTOR)),
                mimeType
            )).as(mimeType);        
    }

    private static String getImageURI(String imageName) {

        String imagePath = IMAGE_NAMES_TO_PATHS.get(imageName);
        String imageURI = (imagePath == null) ? null :
            Application.class.getClassLoader().getResource(imagePath).toString();

        return imageURI;
    }

    public static Result generateLabelsSheetAsPDF() {

        Form<Label> labelForm = Form.form(Label.class).bindFromRequest();
        if (labelForm.hasErrors()) {
            // See comment above about not bothering with a nice presentation.
            return badRequest(labelForm.errorsAsJson());
        }

        Label label = labelForm.get();

        String imageURI = getImageURI(label.imageName);
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
        response().setHeader("Content-Disposition", contentDispHeader);  // TODO Once app is on Play 2.4, switch to HeaderNames.CONTENT_DISPOSITION

        return ok(PlayFop.process(
                views.xml.labelsSheet.render(SHEET_SIZE_AND_WHITESPACE_IN_MM, MM, SHEET_ROWS, SHEET_COLS, imageURI, label),
                mimeType,
                processOptions
            )).as(mimeType);        
    }

    public static Result showAbout() {
        return ok(views.html.about.render());
    }
}
