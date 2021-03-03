package services

import models.ReservationData
import play.api.libs.json.{JsNumber, JsObject, JsString, Json}

import java.time.LocalDateTime
import scala.util.Try

object AdditionalMethods {

  def loneSeats(seats:Seq[Int]):Seq[Boolean] = {
    /*
      available seats are grouped into triples (previous,current,next)
      difference of adjacent seat numbers is calculated
      if the difference is equal to -1 the seats are adjacent, for any different value they're separated by an
      occupied seat; if seat doesn't have a -1 on any side that means it's a lone available seat
      Seats no. -1 are prepended and appended to the collection to allow computing outer seats
      true = lone seat
    */
    Try {
      (Seq(-1) ++ seats ++ Seq(-1)).sliding(3, 1).map {
        case Seq(prev, cur, next) =>
          (prev - cur) != (-1) && (cur - next) != (-1)
      }.toIndexedSeq
    }.getOrElse(IndexedSeq.empty)
  }

  def validName(name:String): Boolean = { //PLAY should support utf-8 by default
    val pattern = """[A-ZĆŚŻŹŁÓ][a-ząęóćśłżź]{2,} [A-ZĆŚŻŹŁÓ][a-ząęóćśłżź]{2,}(-[A-ZĆŚŻŹŁ][a-ząęóćśłżź]{2,})?"""
    name.matches(pattern)
  }

  def total(data:ReservationData,current:LocalDateTime):JsObject = {
    Json.obj(
      "reservation" -> JsObject(Seq(
        "name" -> JsString(data.occupant),
        "expires" -> JsString(current.plusMinutes(15).toString),
        "total" -> JsNumber(data.ticket.foldLeft(0d)((z,t) => z + t.price))
      ))
    )
  }
}