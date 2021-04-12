package controllers

import models._
import models.JsonImplicits._
import services.MovieService
import services.AdditionalMethods._

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import play.api.Logging

import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class MoviesController @Inject()(val controllerComponents: ControllerComponents,service:MovieService) extends BaseController
  with Logging{

  def getMovies() = Action.async{ implicit request =>
    val datePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val startAt = request.getQueryString("start").fold(LocalDate.now())(LocalDate.parse(_,datePattern))
    val endAt = request.getQueryString("end").fold(LocalDate.now())(LocalDate.parse(_,datePattern))

    val movieFuture = service.getMovies(startAt,endAt)
    movieFuture.map(m => Ok(Json.toJson(m)))
      .fallbackTo(Future.successful(InternalServerError("Something went wrong.")))
  }

  def availableSeats(room:Int,date:String) = Action.async{ implicit request =>
    val time = LocalDateTime.parse(date,DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"))

    service.getAvailableSeats(time,room).map(m => Ok(Json.toJson(m)(rowWrites)))
      .fallbackTo(Future.successful(InternalServerError("Something went wrong.")))
  }

  def makeReservation() = Action.async { implicit request =>
    models.ReservationForm.bindFromRequest.fold (
      errorForm => {
        logger.warn(s"Form submission with error: ${errorForm.errors}")
        Future.successful(BadRequest("Provided form submission contains errors."))
      },
      data => {
        val current = LocalDateTime.now()
        if (current.until(data.time, ChronoUnit.MINUTES) > 15) { //can only make a reservation up to 15 minutes before screening begins

          val preReservation = service.getAvailableSeats(data.time, data.room, data.row) //available seats before reservation

          //subtract available seats from reservation to see if client isn't trying to make an order on occupied seats
          val checkForOccupied = preReservation.map(seats => data.seat.diff(seats))
          //subtract reservation from available seats to see the remaining seats and check for lone empty seats
          val checkForLone = preReservation.map(seats => loneSeats(seats.diff(data.seat)))

          checkForOccupied.zip(checkForLone).flatMap {
            case (diff, lone) =>
              if (diff.nonEmpty) {
                Future.successful(Ok("Attempted making a reservation on seats that do not exist, or are already occupied."))
              } else if (lone.exists(identity) && lone.size > 1) {
                Future.successful(Ok("Following reservation would leave a single seat unoccupied. Please make a different order."))
              } else {
                logger.warn(data.toString)
                service.register(data).map(
                    _ => Created(total(data,current))
                )
              }
          }.fallbackTo(Future.successful(InternalServerError("Something went wrong.")))

        } else {Future.successful(Ok("The time to make reservations for given screening has expired."))}
      }
    )
  }

}