name := "ticket_booking"

organization := "mcnkowski"
maintainer := "mcnkowski@gmail.com"

version := "0.1"

scalaVersion := "2.12.13"

lazy val root = (project in file(".")).enablePlugins(PlayScala).disablePlugins(PlayLayoutPlugin)

libraryDependencies ++= Seq("com.typesafe.play" %% "play-slick" % "4.0.0",
  "com.typesafe.slick" %% "slick"           % "3.3.0",
  "com.h2database"      % "h2"              % "1.4.197",
  "ch.qos.logback"      % "logback-classic" % "1.2.3",
  guice
)

scalacOptions ++= Seq("-feature")