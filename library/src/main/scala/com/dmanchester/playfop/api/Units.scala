package com.dmanchester.playfop.api

/** Formats values with a unit of measure. Output is intended for use with
  * Apache FOP.
  *
  * Instances of this class are thread-safe.
  */
class Units private (formatString: String) {

  /** Creates a new instance.
    *
    * @param label units label to append when formatting a value
    * @param precision number of decimal places to display when formatting a
    *                  value
    * @throws `IllegalArgumentException` if `precision` is negative
    */
  def this(label: String, precision: Int) = {
    this(Units.formatString(label, precision))
  }

  /** Formats `value`, displaying it with `precision` decimal places and
    * appending `label` (e.g., "`1.2cm`").
    *
    * @param value value to format
    * @return `value`, formatted
    */
def format(value: Double): String = {
    formatString.format(value)
  }

  /** Formats `value`, displaying it with `precision` decimal places and
    * appending `label` (e.g., "`1.0cm`").
    *
    * @param value value to format
    * @return `value`, formatted
    */
  def format(value: Int): String = {
    format(value.toDouble)
  }
}

/** Companion object of the `[[Units]]` class. Consists of private members for
  * the class's use.
  */
object Units {

  private def formatString(label: String, precision: Int): String = {

    if (precision < 0) {
      throw new IllegalArgumentException(s"precision must be non-negative! (was $precision)")
    }

    val labelEscaped = label.replace("%", "%%")
    s"%.${precision}f${labelEscaped}"  // create a format string like "%.1fmm"
  }
}