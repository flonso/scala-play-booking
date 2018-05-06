package models

import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.joda.time.{DateTime, Duration, Minutes}

import scala.annotation.tailrec

object TimeSlot {
  final val LABEL_INPUT_NAME = "timeslot"

  final val URL_PATTERN = "dd-MM-yy-HH-mm"

  def apply(start: DateTime, tpe: String): TimeSlot =
    AppointmentType.durationFromType(tpe) match {
      case Left(err) => throw new IllegalArgumentException(s"Invalid appointment type $tpe")

      case Right(d) => TimeSlot(start, start.plus(d))
    }

  def formUnapply(s: TimeSlot): Option[(DateTime, String)] =
    Some((s.start, Minutes.minutesBetween(s.start, s.end).getMinutes.toString))

  def buildTimeSlots(start: DateTime, end: DateTime, duration: Duration): Seq[TimeSlot] = {
    @tailrec
    def loop(start: DateTime,
             end: DateTime,
             duration: Duration,
             slots: Seq[TimeSlot]): Seq[TimeSlot] = {
      val slotEnd = start.plus(duration)

      if (slotEnd.isAfter(end))
        slots
      else
        loop(slotEnd, end, duration, TimeSlot(start, slotEnd) +: slots)

    }

    loop(start, end, duration, Seq.empty)
  }

  def availableTimeSlots(start: DateTime, end: DateTime, duration: Duration): Seq[TimeSlot] = {
    val taken = Database
      .query[TimeSlot]
      .whereLargerOrEqual("start", start)
      .whereSmallerOrEqual("end", end)
      .fetch()

    val all = buildTimeSlots(start, end, duration)

    all.filter(ts =>
      taken.foldRight(true) { (ts0, acc) =>
        !ts.overlaps(ts0) && acc
    })
  }

  def validateTimeSlot(ts: TimeSlot): Boolean = {

    val duration = Minutes.minutesBetween(ts.start, ts.end).toStandardDuration

    val validTimeSlots = OpeningTime
      .forDay(ts.start.dayOfWeek().get())
      .flatMap { o =>
        val instant = ts.start.withTime(0, 0, 0, 0)
        TimeSlot.availableTimeSlots(o.start.toDateTime(instant),
                                    o.end.toDateTime(instant),
                                    duration)
      }
      .sortBy(_.start)(Ordering.fromLessThan(_.isBefore(_)))

    println(s"timeslot to validate : \n $ts")
    println(s"valid time slots :\n $validTimeSlots")
    validTimeSlots.contains(ts)
  }
}

case class TimeSlot(start: DateTime, end: DateTime) {

  def formatted: String = {
    val date = "dd-MM-yyyy"
    val hour = "HH:mm"

    s"${start.toString(date)} from ${start.toString(hour)} to ${end.toString(hour)}"

  }

  def duration: Duration =
    Minutes.minutesBetween(start, end).toStandardDuration

  /**
    * Determines if two [[TimeSlot]] instances are overlapping.
    * @param s2 The other [[TimeSlot]]
    * @return true if there is an overlap, false otherwise
    */
  def overlaps(s2: TimeSlot): Boolean = {
    val s1 = this
    // s1 == s2
    s1.start.isEqual(s2.start) ||
    // s1 is included in s2
    (s1.start.isAfter(s2.start) && s1.end.isBefore(s2.end)) ||
    // s2 is included in s1
    (s2.start.isAfter(s1.start) && s2.end.isBefore(s1.end)) ||
    // s1 starts before s2 and s2 starts before s1 ends
    (s1.start.isBefore(s2.start) && s2.start.isBefore(s1.end)) ||
    // s2 starts before s1 and s1 starts before s2 ends
    (s2.start.isBefore(s1.start) && s1.start.isBefore(s2.end))

  }
}
