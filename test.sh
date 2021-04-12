#!/bin/bash

export LANG=C.UTF-8

echo "Look up movies available between 2022-01-01 and 2022-01-02"
curl -X GET "http://localhost:9080/movies?start=2022-01-01&end=2022-01-02"

echo "Look up available seats for one of the screenings"
curl -X GET "http://localhost:9080/room/3/2022-01-02-19:00"

echo "Make a reservation on seats 3 and 4 in row 2."
curl -g -d "{\"occupant\":\"Tamara Strong\", \"room\":3, \"time\":\"2022-01-02-19:00\", \"row\":2, \"seat\":[3,4], \"ticket\":[\"adult\",\"adult\"]}" -H "Content-Type: application/json" -X POST "http://localhost:9080/register"

echo "Seats 3 and 4 no longer show up on the avaiable seat list."
curl -X GET "http://localhost:9080/room/3/2022-01-02-19:00"

echo "Attempt making a reservation on seat 4, which is no longer available."
curl -g -d "{\"occupant\":\"Tamara Strong\", \"room\":3, \"time\":\"2022-01-02-19:00\", \"row\":2, \"seat\":[4], \"ticket\":[\"student\"]}" -H "Content-Type: application/json" -X POST "http://localhost:9080/register"

echo "Attempt making a reservation on seat 2, leaving seat 1 empty."
curl -g -d "{\"occupant\":\"Tamara Strong\", \"room\":3, \"time\":\"2022-01-02-19:00\", \"row\":2, \"seat\":[2], \"ticket\":[\"child\"]}" -H "Content-Type: application/json" -X POST "http://localhost:9080/register"

echo "Attempt making a reservation using a name that doesn't follow the required pattern."
curl -g -d "{\"occupant\":\"Marcin Marc1n\", \"room\":3, \"time\":\"2022-01-02-19:00\", \"row\":1, \"seat\":[2], \"ticket\":[\"child\"]}" -H "Content-Type: application/json" -X POST "http://localhost:9080/register"