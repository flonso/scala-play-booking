package models

import javax.inject.Singleton
import sorm._

@Singleton
object Database
    extends Instance(
      entities = Seq(Entity[Appointment](),
                     Entity[OpeningTime](),
                     Entity[Schedule](),
                     Entity[TimeSlot](),
                     Entity[Patient]()),
      url = "jdbc:h2:mem:test"
    )
