package models

import org.joda.time.{DateTime, Duration, LocalDateTime}

import scala.annotation.tailrec

case class Schedule(openingTimes: Seq[OpeningTime]) {
  def openingTimesFor(dayOfWeek: Int, after: Option[LocalDateTime]): Seq[OpeningTime] =
    openingTimes.filter(o =>
      o.dayOfWeek == dayOfWeek && (after.isEmpty || o.start.isAfter(after.get)))

  /**
    * Computes all available timeslots (i.e. during opening hours and not overlapping with
    * an [[Appointment]]).
    *
    * @param startingDate A starting [[DateTime]] from which starting computations
    * @param timeSlotDuration A minute-based duration of the [[TimeSlot]] durations
    * @return A [[Seq]] of all available [[TimeSlot]]
    */
  def availableTimeSlots(startingDate: DateTime, timeSlotDuration: Duration): Seq[TimeSlot] = {
    val s = this
    val mappedByDay = s.openingTimes
      .sortBy(_.start)(Ordering.fromLessThan(_.isBefore(_)))
      .groupBy(_.dayOfWeek)

    val nextOpenDay = OpeningTime.nextOpenDay(startingDate)
    val days = remainingWeekDays(nextOpenDay)

    mappedByDay.flatMap {
      case (dayOfWeek, os) =>
        os.flatMap { o =>
          if (days.contains(dayOfWeek)) {
            val start = days(dayOfWeek).withTime(o.start)
            val end = start.withTime(o.end)

            TimeSlot.availableTimeSlots(start, end, timeSlotDuration)
          } else
            Seq.empty

        }
    }.toSeq
  }

  /**
    * Computes one DateTime per remaining week day starting from start
    *
    * @param start The starting time for the computation
    * @return A [[Map]] from a Integer-represented weekday to a DateTime for that day.
    */
  private def remainingWeekDays(start: DateTime = DateTime.now()): Map[Int, DateTime] = {
    @tailrec
    def loop(start: DateTime, endDay: Int, acc: Map[Int, DateTime]): Map[Int, DateTime] =
      if (endDay == 0) acc
      else {
        val day = start.dayOfWeek().get()

        loop(start.plusDays(1), endDay - 1, acc + (day -> start.withTime(0, 0, 0, 0)))
      }

    loop(start, 7 - start.dayOfWeek().get(), Map.empty)
  }
}
