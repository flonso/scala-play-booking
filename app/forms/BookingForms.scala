package forms

import models._
import org.joda.time.format.DateTimeFormat
import org.joda.time.{LocalDate, DateTime => JodaDateTime}
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import utils.JodaDateUtils

import scala.util.{Failure, Success, Try}

object BookingForms {

  /**
    * Custom [[Formatter]] for processing [[org.joda.time.DateTime]] in forms.
    * Generates a [[org.joda.time.DateTime]] based on the received data [[String]] value.
    */
  private val jodaTimeFormatter = new Formatter[JodaDateTime] {
    override def bind(key: String,
                      data: Map[String, String]): Either[Seq[FormError], JodaDateTime] =
      data
        .get(key)
        .map[Either[Seq[FormError], JodaDateTime]] { dateString =>
        val date = new JodaDateTime(dateString)
        val ok = Right(date)

        if (date.isBeforeNow) Left(Seq(FormError(key, "Cannot book an appointment in the past")))
        else ok
      }
        .getOrElse(Left(Seq(FormError(key, "This field is required"))))

    override def unbind(key: String, value: JodaDateTime): Map[String, String] =
      Map(key -> value.toString())
  }

  /**
    * Custom [[Formatter]] for processing [[org.joda.time.LocalDate]] in forms.
    * Generates a [[org.joda.time.LocalDate]] based on the received data [[String]] value.
    */
  private val birthDateFormatter = new Formatter[LocalDate] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] =
      data
        .get(key)
        .map { dateString =>
          val formatter = DateTimeFormat.forPattern(JodaDateUtils.BIRTHDATE_PATTERN)
          Try(LocalDate.parse(dateString, formatter)) match {
            case Success(d)   => Right(d)
            case Failure(err) => Left(Seq(FormError(key, err.getMessage)))
          }
        }
        .getOrElse(Left(Seq(FormError(key, "This field is required"))))

    override def unbind(key: String, value: LocalDate): Map[String, String] =
      Map(key -> value.toString(JodaDateUtils.BIRTHDATE_PATTERN))
  }

  /**
    * Custom [[Formatter]] for processing [[TimeSlot]] in forms.
    * Generates a [[TimeSlot]] based on the received data [[String]] value.
    */
  private val timeSlotFormatter = new Formatter[TimeSlot] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], TimeSlot] =
      data
        .get(key)
        .map { ts =>
          val formatter = DateTimeFormat.forPattern(TimeSlot.URL_PATTERN)
          Try(JodaDateTime.parse(ts, formatter)) match {
            case Success(d) =>
              val tpe = data.get(AppointmentType.LABEL_INPUT_NAME).get
              Right(TimeSlot(d, tpe))

            case Failure(err) => Left(Seq(FormError(key, err.getMessage)))
          }
        }
        .getOrElse(Left(Seq(FormError(key, "This field is required"))))

    override def unbind(key: String, value: TimeSlot): Map[String, String] =
      Map(key -> value.start.toString(TimeSlot.URL_PATTERN))
  }


  /**
    * Defines a mapping for handling [[AppointmentType]] selection.
    */
  val chooseAppointmentTypeForm = Form {
    mapping(
      AppointmentType.LABEL_INPUT_NAME -> text.verifying("Invalid appointment type given",
                                                         AppointmentType.validateLabel(_))
    )(AppointmentType.apply)(AppointmentType.unapply)
  }

  /**
    * Defines a mapping for handling [[TimeSlot]] selection.
    */
  val chooseTimeSlotForm = Form {
    mapping(
      "start" -> of(jodaTimeFormatter),
      AppointmentType.LABEL_INPUT_NAME -> text.verifying("Invalid appointment type given",
                                                         AppointmentType.validateLabel(_))
    )(TimeSlot.apply)(TimeSlot.formUnapply).verifying(
      "Invalid time slot chosen",
      TimeSlot.validateTimeSlot(_)
    )
  }

  /**
    * Defines a mapping for handling [[Appointment]] selection.
    */
  val personalInfoForm = Form {
    mapping(
      "timeSlot" -> of(timeSlotFormatter),
      "firstName" -> text,
      "lastName" -> text,
      "birthDate" -> of(birthDateFormatter),
      "phoneNumber" -> text,
      "email" -> email,
      "description" -> optional(text),
      AppointmentType.LABEL_INPUT_NAME -> text
    )(Appointment.apply)(Appointment.formUnapply)
  }

}
