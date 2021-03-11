A ticket booking app for a movie cinema using Play framework and Slick.

The app is set to listen for HTTP requests on port 9080 and HTTPS requests on port 9090.

The app has been created using Play 2.0 framework, which encourages using SBT for building projects.
Calling `sbt run` in the application directory launches it in developer mode.
To launch the app in production mode call `sbt dist`, extract the resulting `ticket_booking-0.1.zip`, and run `ticket_booking` script in the `bin` directory.

https://www.scala-sbt.org/

`test.sh` script contains example curl calls.

Available actions:
```
GET /movies?start=DATE1&end=DATE2
```
Fetches a list of movies available between `DATE1` and `DATE2`. Dates must be formatted using `yyyy-MM-dd` pattern.

```
GET /room/:id/:date
```
Get a list of available seats for given room number at a given date, where `:id` is an integer specifying room number, and `:date` is a screening time following `yyyy-MM-dd-HH:mm` format.

```
POST /register
```
Make a reservation on specified seats. The request form requires following fields: `occupant (text), room(number), time (localdatetime), row (number), seat (array of numbers), ticket (array of texts)`. "Seat" and "ticket" fields are required to have the same number of values, or else the form will throw an error. "Ticket" field will also only accept `"adult"`, `"student"`, and `"child"` values. 

Successful responses will yield data in JSON format.

For demonstration purposes the App uses an in-memory H2 database with screenings taking place between 2022-01-01 and 2022-01-03. 
The database structure is as follows:
- a `screening` table containing data about available movies and their screenings: `id[int], date[date], movie[varchar], hour[time], room[int]`;
- a `reservation` table containing data about seat reservations for specified rooms and screening times: `id[int], room[int], time[timestamp], row[int], seat[int], occupant[varchar nullable], ticket[varchar nullable]`.

The app assumes that the database will hold an entry for every existing seat, and that a seat that hasn't been reserved yet will have a NULL occupant.

Additional assumptions:

If a reservation results in a situation where an empty seat has no adjacent empty seats, the reservation will not go through, but only assuming that there are other empty seats in the same row (i.e. the client can make a reservation that doesn't create such a situation). If the lone seat is the only seat that would remain empty in the row, the reservation will be accepted.

The app also assumes there are no Polish names starting with "Ą" or "Ę".