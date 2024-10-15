CREATE TABLE IF NOT EXISTS airport (
    id SERIAL PRIMARY KEY,
    city VARCHAR(100) NOT NULL,
    airport_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS airplane (
    id SERIAL PRIMARY KEY,
    model VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS flight (
    id SERIAL PRIMARY KEY,
    departure_airport_id INT NOT NULL REFERENCES airport(id),
    arrival_airport_id INT NOT NULL REFERENCES airport(id),
    departure_time TIMESTAMP NOT NULL,
    arrival_time TIMESTAMP NOT NULL,
    airplane_id INT NOT NULL REFERENCES airplane(id)
);

CREATE TABLE IF NOT EXISTS ticket (
    id SERIAL PRIMARY KEY,
    flight_id INT NOT NULL REFERENCES flight(id),
    passenger_name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);


alter table airplane
    add image bytea;