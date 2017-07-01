package models

case class Label(val text: String, val fontFamily: String,
    val fontSizeInPoints: Int, val imageName: Option[String]) {

  def scale(scaleFactor: Int): Label = {
    this.copy(fontSizeInPoints = scaleFactor * this.fontSizeInPoints)
  }
}

object Label {
  val TextMaxLength = 512
}
