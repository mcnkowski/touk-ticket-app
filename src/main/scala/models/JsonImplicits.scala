package models

import play.api.libs.json._

object JsonImplicits {

  implicit val screenwrites: Writes[Screening] =  (screening: Screening) => JsObject(
    Seq(
      "title" -> JsString(screening.movie),
      "hour" -> JsString(screening.hour.toString),
      "room" -> JsNumber(screening.room)
    )
  )

  implicit val rowWrites: Writes[Map[Int, Seq[Int]]]= (map: Map[Int, Seq[Int]]) =>
    Json.obj(
      "rows" -> JsArray(map.map { case (k, v) =>
        JsObject(Seq(
          "row" -> JsNumber(k),
          "seats" -> JsArray(v.map(JsNumber(_)))
        ))
      }.toSeq)
  )
}