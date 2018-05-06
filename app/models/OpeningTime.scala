package models

import org.joda.time.{DateTime, DateTimeConstants, Days, LocalTime}

object OpeningTime {
  def forDay(dayOfWeek: Int): Seq[OpeningTime] =
    Database.query[OpeningTime].whereEqual("dayOfWeek", dayOfWeek).fetch()

  /**
    * Computes the next day for which there is an OpeningTime
    *
    * @param from A starting [[DateTime]] for next open day computation
    * @return A [[DateTime]] representation of the next open day
    */
  def nextOpenDay(from: DateTime = DateTime.now()): DateTime = {
    val nextWeekDay = Database
      .query[OpeningTime]
      .whereLargerOrEqual("dayOfWeek", from.dayOfWeek.get)
      .order("dayOfWeek")
      .fetchOne()
      .map(_.dayOfWeek)
      .getOrElse(DateTimeConstants.MONDAY)

    val diffInDays =
      if (nextWeekDay < from.dayOfWeek.get)
        Days.SEVEN.minus(from.dayOfWeek().get()).plus(nextWeekDay).getDays
      else nextWeekDay - from.dayOfWeek().get()

    from.plusDays(diffInDays)
  }
}

case class OpeningTime(dayOfWeek: Int, start: LocalTime, end: LocalTime)