package models

import org.joda.time.{Duration, LocalDate}

import scala.util.{Success, Try}

object Appointment {
  def apply(timeSlot: TimeSlot,
            firstName: String,
            lastName: String,
            birthDate: LocalDate,
            phoneNumber: String,
            email: String,
            description: Option[String],
            tpe: String): Appointment = {
    val patient = Patient(firstName, lastName, birthDate, phoneNumber, email)
    new Appointment(timeSlot, patient, description, tpe)
  }

  def formUnapply(a: Appointment)
    : Option[(TimeSlot, String, String, LocalDate, String, String, Option[String], String)] =
    a match {
      case Appointment(ts, Patient(fn, ln, bd, pn, e), d, t) =>
        Some(ts, fn, ln, bd, pn, e, d, t)
    }
}

case class Appointment(timeSlot: TimeSlot,
                       patient: Patient,
                       description: Option[String],
                       tpe: String)

object AppointmentType {
  final val LABEL_INPUT_NAME = "appointment_type"
  final val FIRST_APPOINTMENT = "first_appointment"
  final val FOLLOWUP_APPOINTMENT = "followup_appointment"

  final val typeLabels = Map(
    FIRST_APPOINTMENT -> "First appointment",
    FOLLOWUP_APPOINTMENT -> "Followup appointment"
  )

  private final val fromTypeMap: Map[String, Duration] = Map(
    FIRST_APPOINTMENT -> Duration.standardMinutes(45),
    FOLLOWUP_APPOINTMENT -> Duration.standardMinutes(30)
  )

  private final val fromDurationMap: Map[Duration, String] = fromTypeMap.map {
    case (key, value) => value -> key
  }

  def durationFromType(l: String): Either[String, Duration] =
    Try(Right(fromTypeMap(l))).getOrElse(Left("Invalid appointment type"))

  def fromDuration(d: Duration): Either[String, String] =
    Try(Right(fromDurationMap(d))).getOrElse(Left("Invalid appointment duration"))

  def validateLabel(l: String): Boolean = {
    println(s"validating label $l")
    l == FIRST_APPOINTMENT || l == FOLLOWUP_APPOINTMENT
  }
}

case class AppointmentType(label: String)
