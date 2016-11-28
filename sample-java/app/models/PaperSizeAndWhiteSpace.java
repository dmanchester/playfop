package models;

public class PaperSizeAndWhiteSpace {

    private int height;
    private int width;
    private int margin;
    private int interLabelGutter;
    private int intraLabelPadding;

    public PaperSizeAndWhiteSpace(int height, int width, int margin, int interLabelGutter, int intraLabelPadding) {
        this.height = height;
        this.width = width;
        this.margin = margin;
        this.interLabelGutter = interLabelGutter;
        this.intraLabelPadding = intraLabelPadding;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getMargin() {
        return margin;
    }

    public int getInterLabelGutter() {
        return interLabelGutter;
    }

    public int getIntraLabelPadding() {
        return intraLabelPadding;
    }
}
