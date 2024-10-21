-- Создание таблиц
CREATE TABLE IF NOT EXISTS airport
(
    id           SERIAL PRIMARY KEY,
    city         VARCHAR(100) NOT NULL,
    airport_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS airplane
(
    id    SERIAL PRIMARY KEY,
    model VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS flight
(
    id                   SERIAL PRIMARY KEY,
    departure_airport_id INT       NOT NULL REFERENCES airport (id),
    arrival_airport_id   INT       NOT NULL REFERENCES airport (id),
    departure_time       TIMESTAMP NOT NULL,
    arrival_time         TIMESTAMP NOT NULL,
    airplane_id          INT       NOT NULL REFERENCES airplane (id)
);

-- Заполнение таблиц
INSERT INTO airport (city, airport_name)
VALUES ('Moscow', 'Sheremetyevo'),
       ('Saint Petersburg', 'Pulkovo');

INSERT INTO airplane (model)
VALUES ('Boeing 737'),
       ('Airbus A320');

INSERT INTO flight (departure_airport_id, arrival_airport_id, departure_time, arrival_time, airplane_id)
VALUES (1, 2, '2024-10-10 10:00:00', '2024-10-10 16:00:00', 1);
