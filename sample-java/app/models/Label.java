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
    public String text;

    @Required
    public String fontFamily;

    @Required
    public Integer fontSizeInPoints;

    public String imageName;

    public Label scale(int scaleFactor) {

        Label scaledLabel = new Label();

        scaledLabel.text = text;
        scaledLabel.fontFamily = fontFamily;
        scaledLabel.fontSizeInPoints = scaleFactor * fontSizeInPoints;
        scaledLabel.imageName = imageName;

        return scaledLabel;
    }
}
