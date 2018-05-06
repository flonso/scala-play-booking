package utils

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
  * Date and Time helpers.
  */
object JodaDateUtils {

  final val URL_DATE_PATTERN = "dd-MM-yyyy"
  final val BIRTHDATE_PATTERN = "dd.MM.yyyy"

  def parseUrlDate(date: Option[String]): DateTime = {
    val formatter = DateTimeFormat.forPattern(URL_DATE_PATTERN)
    date
      .map { d =>
        DateTime.parse(d, formatter)
      }
      .getOrElse(DateTime.now)
  }

  def dateToUrlString(date: DateTime = DateTime.now): String =
    date.toString(URL_DATE_PATTERN)

  implicit def dateOrdering: Ordering[DateTime] = Ordering.fromLessThan(_.isBefore(_))
}
