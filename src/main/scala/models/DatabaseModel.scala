package models

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.H2Profile.api._

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime, LocalTime}


@Singleton
class DatabaseModel @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  lazy val seats = TableQuery[ReservationTable]
  lazy val screenings = TableQuery[ScreeningTable]

  //create an in-memory DB and populate it with example data
  dbConfig.db.run(seats.schema.create andThen screenings.schema.create) flatMap (_ => populate)

  ////////////////////////////////////////////////////


  //get screenings for given time interval
  def screeningsAt(from:LocalDate,to:LocalDate):Future[Seq[Screening]] = {
    val query = screenings.filter(scr => scr.date >= from && scr.date <= to)
      .sortBy(scr => (scr.date,scr.movie,scr.hour))

    dbConfig.db.run(query.result)
  }

  ////////////////////////////////////////////////////

  //app assumes that seat reservation table is populated with entries for each existing seat
  //unoccupied seat will have a NULL occupant

  //get unoccupied seats for given room and screening time
  def available(room:Int,time:LocalDateTime):Future[Seq[(Int,Int)]] = {
    val query = seats.filter( s =>
      s.room === room &&
      s.time === time &&
      s.occupant.isEmpty )
      .sortBy(s => s.row -> s.seat)
      .map(s => s.row -> s.seat)

    dbConfig.db.run(query.result)
  }

  def availableInRow(room:Int,time:LocalDateTime,row:Int):Future[Seq[Int]] = {
    val query = seats.filter( s =>
      s.room === room &&
      s.time === time &&
      s.row === row &&
      s.occupant.isEmpty )
      .sortBy(_.seat)
      .map(_.seat)

    dbConfig.db.run(query.result)
  }

  //update seat reservation with an occupant's name
  def register(name:String,room:Int,time:LocalDateTime,row:Int,seat:Seq[Int],ticket:Seq[Ticket]):Future[Seq[Int]] = {

    val query = seats.filter( s =>
      s.room === room && s.time === time && s.row === row
    )

    //update each field individually so that different ticket types can be assigned
    val querySeq = DBIO.sequence(
      seat.zip(ticket).map {
        case (s,t) =>
          query.filter(_.seat === s).map(reg => (reg.occupant,reg.ticket)).update(Some(name) -> Some(t))
      }
    )

    dbConfig.db.run(querySeq)
  }


  def populate: Future[Option[Int]] = {
    val datePtn = DateTimeFormatter.ofPattern("""yyyy-MM-dd""")
    val timePtn = DateTimeFormatter.ofPattern("""HH:mm""")
    val datetimePtn = DateTimeFormatter.ofPattern("""yyy-MM-dd-HH:mm""")

    //Screening(date: LocalDate, movie: String, hour: LocalTime, room: Int, id: Option[Int] = None)
    //embarrassingly copy-pasted data
    val screeningData = Seq(
      Screening(LocalDate.parse("2022-01-01",datePtn),"Store Wares",LocalTime.parse("16:00",timePtn),1),
        Screening(LocalDate.parse("2022-01-02",datePtn),"Store Wares",LocalTime.parse("19:00",timePtn),2),
      Screening(LocalDate.parse("2022-01-03",datePtn),"Store Wares",LocalTime.parse("21:00",timePtn),3),
        Screening(LocalDate.parse("2022-01-01",datePtn),"The Mandarinian",LocalTime.parse("16:00",timePtn),2),
      Screening(LocalDate.parse("2022-01-02",datePtn),"The Mandarinian",LocalTime.parse("19:00",timePtn),3),
        Screening(LocalDate.parse("2022-01-03",datePtn),"The Mandarinian",LocalTime.parse("21:00",timePtn),1),
      Screening(LocalDate.parse("2022-01-01",datePtn),"The Last Franchise",LocalTime.parse("16:00",timePtn),3),
        Screening(LocalDate.parse("2022-01-02",datePtn),"The Last Franchise",LocalTime.parse("19:00",timePtn),1),
      Screening(LocalDate.parse("2022-01-03",datePtn),"The Last Franchise",LocalTime.parse("21:00",timePtn),2)
    )

    //SeatReservation(room: Int, time: LocalDateTime, row: Int, seat: Int, occupant: Option[String], ticket:Option[Ticket], id: Option[Int] = None)
    val seatsData = {
      for (rm <- 1 to 3; row <- 1 to 2; seat <- 1 to 6; datetime <- Vector("2022-01-01-16:00","2022-01-02-19:00","2022-01-03-21:00"))
        yield SeatReservation(rm,LocalDateTime.parse(datetime,datetimePtn),row,seat,None,None)
    }

    dbConfig.db.run(screenings ++= screeningData)
    dbConfig.db.run(seats ++= seatsData)
  }
}

