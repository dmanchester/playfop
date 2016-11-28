package views.util;

import models.PaperSizeAndWhiteSpace;

public class Calc {

    public static double getLabelWidth(PaperSizeAndWhiteSpace paper, int cols) {
        return (paper.getWidth() - 2 * paper.getMargin() - (cols - 1) * paper.getInterLabelGutter())/(double)cols;
    }

    public static double getLabelHeight(PaperSizeAndWhiteSpace paper, int rows) {
        return (paper.getHeight() - 2 * paper.getMargin() - (rows - 1) * paper.getInterLabelGutter())/(double)rows;
    }
}
