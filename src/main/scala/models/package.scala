import java.time.{LocalDate, LocalDateTime, LocalTime}
import slick.jdbc.H2Profile.api._
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms._
import services.AdditionalMethods._

package object models {

  trait Ticket{def price:Double; def isDefined:Boolean}
  case object Adult extends Ticket {override val price = 25d; override def toString = "adult"; override val isDefined = true}
  case object Student extends Ticket {override val price = 18d; override def toString = "student"; override val isDefined = true}
  case object Child extends Ticket {override val price = 12.5d; override def toString = "child"; override val isDefined = true}
  case object WrongTicket extends Ticket {override val price = 0d; override val isDefined = false}

  implicit val ticketType = MappedColumnType.base[Ticket,String](
    ticket => ticket.toString,
    string => str2ticket(string)
  )

  case class Screening(date: LocalDate, movie: String, hour: LocalTime, room: Int, id: Option[Int] = None)

  case class SeatReservation(room: Int, time: LocalDateTime, row: Int, seat: Int, occupant: Option[String], ticket:Option[Ticket], id: Option[Int] = None)

  case class ReservationData(occupant: String, room: Int, time: LocalDateTime, row: Int, seat: List[Int], ticket:List[Ticket])

  //coupling seat and ticket into a case class caused issues with parsing form data
  val ReservationForm:Form[ReservationData] = Form(
    mapping(
      "occupant" -> nonEmptyText,
      "room" -> number(min = 1),
      "time" -> localDateTime("yyyy-MM-dd-HH:mm"),
      "row" -> number,
      "seat" -> list(number),
      "ticket" -> list(nonEmptyText)
    )((o,rm,tm,rw,s,tc) => ReservationData(o,rm, tm, rw, s, tc.map(str2ticket))) //apply
    (rd => Some((rd.occupant,rd.room,rd.time,rd.row,rd.seat,rd.ticket.map(_.toString)))) //unapply
      .verifying(
        "Form constraints failed.",
        fields => fields match {
          case rd =>
            validName(rd.occupant) &&
            rd.seat.size == rd.ticket.size && //amounts of seats and tickets have to be the same
            rd.ticket.forall(_.isDefined)
        }
      )
  )


  class ScreeningTable(tag: Tag) extends Table[Screening](tag, "screening") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def date = column[LocalDate]("date")
    def movie = column[String]("movie")
    def hour = column[LocalTime]("hour")
    def room = column[Int]("room")

    def * = (date, movie, hour, room, id.?).mapTo[Screening]
  }

  class ReservationTable(tag: Tag) extends Table[SeatReservation](tag, "reservation") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def room = column[Int]("room")
    def time = column[LocalDateTime]("time")
    def row = column[Int]("row")
    def seat = column[Int]("seat")
    def occupant = column[Option[String]]("occupant")
    def ticket = column[Option[Ticket]]("ticket")

    def * = (room, time, row, seat, occupant, ticket, id.?).mapTo[SeatReservation]
  }

  def str2ticket(ticket:String) = {
    ticket match {
      case "adult" => Adult
      case "student" => Student
      case "child" => Child
      case _ => WrongTicket
    }
  }
}