package models;

import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;

public class Label {

    public static final int TEXT__MAX_LENGTH = 512;

    @Required
    @MaxLength(TEXT__MAX_LENGTH)
    // It's notable that, here in the sample Java application, we place the max
    // length in the Java domain class. (In the Scala application, we place it
    // in the form definition.)
    private String text;

    @Required
    private String fontFamily;

    @Required
    private Integer fontSizeInPoints;

    private String imageName;

    public Label scale(int scaleFactor) {

        Label scaledLabel = new Label();

        scaledLabel.text = text;
        scaledLabel.fontFamily = fontFamily;
        scaledLabel.fontSizeInPoints = scaleFactor * fontSizeInPoints;
        scaledLabel.imageName = imageName;

        return scaledLabel;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public Integer getFontSizeInPoints() {
        return fontSizeInPoints;
    }

    public void setFontSizeInPoints(Integer fontSizeInPoints) {
        this.fontSizeInPoints = fontSizeInPoints;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
