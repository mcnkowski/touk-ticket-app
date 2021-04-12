package services

import models.{DatabaseModel, ReservationData}

import java.time.{LocalDate, LocalDateTime}
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class MovieService @Inject()(database:DatabaseModel)(implicit executionContext: ExecutionContext){

  //return sequences of screenings grouped by movie titles
  def getMovies(from:LocalDate,to:LocalDate) = {
    //groupMap would've been better but playslick doesn't work with Scala 2.13
    database.screeningsAt(from,to) map (_.groupBy(_.date.toString))
  }

  def getAvailableSeats(date:LocalDateTime,room:Int) = {
    database.available(room,date).map(_.groupBy(_._1).map { case (k,v) => k -> v.map(_._2)})
  }

  def getAvailableSeats(date:LocalDateTime,room:Int,row:Int) = {
    database.availableInRow(room,date,row)
  }

  def register(data:ReservationData) = {
    database.register(data.occupant,data.room,data.time,data.row,data.seat, data.ticket)
  }

}
