package services

import javax.inject.{Inject, Singleton}
import models.{Database, OpeningTime, Schedule}
import org.joda.time.{DateTimeConstants, LocalTime}
import play.api.inject.ApplicationLifecycle
import play.api.Logger
import sorm.Persisted

import scala.concurrent.Future

/**
  * Predefined data for running the application.
  */
@Singleton
class ApplicationSetup @Inject()(appLifecycle: ApplicationLifecycle) {

  Logger.info("Cleaning OpenTime table")
  Database.query[OpeningTime].fetch().foreach(Database.delete[OpeningTime](_))

  assert(Database.query[OpeningTime].fetch().isEmpty)

  Logger.info("Cleaning Schedule table")
  Database.query[Schedule].fetch().foreach(Database.delete[Schedule](_))

  assert(Database.query[Schedule].fetch().isEmpty)

  Logger.info("Persisting predefined OpenTime")
  private val openingTimes = Seq(
    OpeningTime(DateTimeConstants.MONDAY, new LocalTime(9, 0), new LocalTime(12, 0)),
    OpeningTime(DateTimeConstants.MONDAY, new LocalTime(14, 0), new LocalTime(18, 0)),
    OpeningTime(DateTimeConstants.TUESDAY, new LocalTime(9, 0), new LocalTime(12, 0)),
    OpeningTime(DateTimeConstants.TUESDAY, new LocalTime(14, 0), new LocalTime(18, 0)),
    OpeningTime(DateTimeConstants.WEDNESDAY, new LocalTime(9, 0), new LocalTime(12, 0)),
    OpeningTime(DateTimeConstants.THURSDAY, new LocalTime(9, 0), new LocalTime(12, 0)),
    OpeningTime(DateTimeConstants.THURSDAY, new LocalTime(14, 0), new LocalTime(18, 0)),
    OpeningTime(DateTimeConstants.FRIDAY, new LocalTime(10, 0), new LocalTime(13, 0)),
    OpeningTime(DateTimeConstants.FRIDAY, new LocalTime(15, 0), new LocalTime(17, 0))
  )

  private val persistedOpeningTimes: Seq[OpeningTime with Persisted] =
    openingTimes.map(o => Database.save(o))

  Logger.info("Persisting predefined Schedule")
  private val schedule = Schedule(persistedOpeningTimes)
  Database.save(schedule)

  Logger.info("Done with application setup")
}
