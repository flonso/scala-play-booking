package models

import forms.BookingForms
import org.joda.time.LocalDate

case class Patient(firstName: String,
                   lastName: String,
                   birthDate: LocalDate,
                   phoneNumber: String,
                   email: String)
