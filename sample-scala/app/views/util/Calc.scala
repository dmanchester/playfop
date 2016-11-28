package views.util

import models.PaperSizeAndWhiteSpace

object Calc {

  def labelWidth(paper: PaperSizeAndWhiteSpace, cols: Int): Double = {
    (paper.width - 2 * paper.margin - (cols - 1) * paper.interLabelGutter) / cols.toDouble
  }

  def labelHeight(paper: PaperSizeAndWhiteSpace, rows: Int): Double = {
    (paper.height - 2 * paper.margin - (rows - 1) * paper.interLabelGutter) / rows.toDouble
  }
}
