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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.fo.FOTreeBuilder;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.Typeface;
import org.apache.xmlgraphics.util.MimeConstants;

import com.dmanchester.playfop.api.Units;
import com.dmanchester.playfop.japi.FOUserAgentBlock;
import com.dmanchester.playfop.japi.PlayFop;
import com.dmanchester.playfop.japi.ProcessOptions;
import com.typesafe.config.Config;

import models.Label;
import models.PaperSizeAndWhiteSpace;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http.HeaderNames;
import play.mvc.Result;
import views.util.Calc;

public class Application extends Controller {

    private static final String ABOUT_PAGE_ADDL_INFO_CONFIG_PATH = "about.page.addl.info";
    private static final String FONT_FAMILY_EXCLUSION_REGEX_CONFIG_PATH = "font.family.exclusion.regex";
    private static final String INITIAL_FONT_FAMILY_CONFIG_PATH = "initial.font.family";

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
    private static final Integer INITIAL_FONT_SIZE_IN_POINTS = 9;
    private static final String INITIAL_IMAGE_NAME = IMAGE_NAME__CITYSCAPE;

    private static final int SINGLE_LABEL_SCALE_FACTOR = 3;

    private static final String PLAYFOP_URL = "https://www.dmanchester.com/playfop";

    private static Map<String, String> getImageNamesToPaths() {

        Map<String, String> imageNamesToPaths = new LinkedHashMap<String, String>();

        imageNamesToPaths.put(IMAGE_NAME__CITYSCAPE, "images/emoji_u1f306_cityscape.svg");
        imageNamesToPaths.put(IMAGE_NAME__SUNRISE, "images/emoji_u1f304_sunrise.svg");
        imageNamesToPaths.put(IMAGE_NAME__VOLCANO, "images/emoji_u1f30b_volcano.svg");

        return imageNamesToPaths;
    }

    private Config config;
    private FormFactory formFactory;
    private PlayFop playFop;
    private List<String> fontFamilies;

    public Application(Config config, FormFactory formFactory, PlayFop playFop) {

        this.config = config;
        this.formFactory = formFactory;
        this.playFop = playFop;

        this.fontFamilies = getFontFamilies();  // getFontFamilies() is an expensive operation, so cache its result
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

        List<String> fontNamesUnfiltered = new ArrayList<String>(typefaces.size());
        for (Typeface typeface : typefaces) {
            fontNamesUnfiltered.add(typeface.getFullName());
        }

        List<String> fontNames;

        // If an exclusion regex was provided, filter out any font names that
        // match it.
        if (config.hasPath(FONT_FAMILY_EXCLUSION_REGEX_CONFIG_PATH)) {

            fontNames = new ArrayList<String>();

            String fontFamilyExclusionRegex = config.getString(FONT_FAMILY_EXCLUSION_REGEX_CONFIG_PATH);
            Pattern pattern = Pattern.compile(fontFamilyExclusionRegex);

            for (String fontName : fontNamesUnfiltered) {
                Matcher matcher = pattern.matcher(fontName);
                if (!matcher.find()) {
                    fontNames.add(fontName);
                }
            }

        } else {

            fontNames = fontNamesUnfiltered;

        }

        Collections.sort(fontNames);

        return fontNames;
    }

    public Result index() {
        return redirect(controllers.routes.Application.designLabels());
    }

    public Result designLabels() {

        List<String> imageNames = new ArrayList<String>(IMAGE_NAMES_TO_PATHS.keySet());
        imageNames.add(0, "");  // offer a no-image option

        return ok(views.html.labelDesign.render(getInitialForm(), fontFamilies, getFontSizesAndText(), imageNames));
    }

    private Form<Label> getInitialForm() {

        Label initialLabel = new Label();
        initialLabel.setText(INITIAL_TEXT);
        initialLabel.setFontSizeInPoints(INITIAL_FONT_SIZE_IN_POINTS);
        initialLabel.setImageName(INITIAL_IMAGE_NAME);

        if (config.hasPath(INITIAL_FONT_FAMILY_CONFIG_PATH)) {

            // The property has been set. Get its value and confirm that the
            // font family is available.

            String initialFontFamily = config.getString(INITIAL_FONT_FAMILY_CONFIG_PATH);

            if (fontFamilies.contains(initialFontFamily)) {

                initialLabel.setFontFamily(initialFontFamily);

            } else {

                String message = String.format("Font family %s not found!", initialFontFamily);
                throw new IllegalArgumentException(message);
            }

        } else {

            // The property has not been set. Use the first font family in the
            // list.
            initialLabel.setFontFamily(fontFamilies.get(0));
        }

        return formFactory.form(Label.class).fill(initialLabel);
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

        byte[] pngBytes = playFop.processTwirlXml(
            views.xml.labelSingle.render(labelWidthInMM, labelHeightInMM, intraLabelPaddingInMM, MM, imageURI, label.scale(SINGLE_LABEL_SCALE_FACTOR)),
            mimeType
        );

        return ok(pngBytes).as(mimeType);
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

        byte[] pdfBytes = playFop.processTwirlXml(
            views.xml.labelsSheet.render(SHEET_SIZE_AND_WHITESPACE_IN_MM, MM, SHEET_ROWS, SHEET_COLS, imageURI, label),
            mimeType,
            processOptions
        );

        return ok(pdfBytes).as(mimeType);
    }

    public Result showAbout() {

        String addlInfoAsHtml;

        if (config.hasPath(ABOUT_PAGE_ADDL_INFO_CONFIG_PATH)) {

            String addlInfoPath = config.getString(ABOUT_PAGE_ADDL_INFO_CONFIG_PATH);
            Path addlInfoPathObj = Paths.get(addlInfoPath);

            try {

                byte[] addlInfoAsBytes = Files.readAllBytes(addlInfoPathObj);
                addlInfoAsHtml = new String(addlInfoAsBytes, "utf-8");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {

            addlInfoAsHtml = null;
        }

        return ok(views.html.about.render(PLAYFOP_URL, addlInfoAsHtml));
    }
}
