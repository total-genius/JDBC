INSERT INTO airport (city, airport_name) VALUES
('Moscow', 'Sheremetyevo'),
('Saint Petersburg', 'Pulkovo'),
('New York', 'John F. Kennedy'),
('Los Angeles', 'LAX'),
('London', 'Heathrow'),
('Paris', 'Charles de Gaulle'),
('Tokyo', 'Narita'),
('Berlin', 'Tegel'),
('Dubai', 'Dubai International'),
('Singapore', 'Changi');

INSERT INTO airplane (model) VALUES
('Boeing 737'),
('Airbus A320'),
('Boeing 777'),
('Airbus A380'),
('Boeing 747'),
('Embraer E190'),
('Bombardier CRJ900'),
('Airbus A330'),
('Boeing 787'),
('Airbus A350');

INSERT INTO flight (departure_airport_id, arrival_airport_id, departure_time, arrival_time, airplane_id) VALUES
(1, 3, '2024-10-10 10:00:00', '2024-10-10 16:00:00', 1),
(2, 4, '2024-10-11 11:30:00', '2024-10-11 15:30:00', 2),
(3, 5, '2024-10-12 14:00:00', '2024-10-12 18:00:00', 3),
(4, 6, '2024-10-13 08:00:00', '2024-10-13 12:00:00', 4),
(5, 7, '2024-10-14 09:00:00', '2024-10-14 17:00:00', 5),
(6, 8, '2024-10-15 13:00:00', '2024-10-15 17:00:00', 6),
(7, 9, '2024-10-16 15:00:00', '2024-10-16 23:00:00', 7),
(8, 10, '2024-10-17 19:00:00', '2024-10-18 07:00:00', 8),
(9, 1, '2024-10-18 12:00:00', '2024-10-18 20:00:00', 9),
(10, 2, '2024-10-19 18:00:00', '2024-10-19 22:00:00', 10);

INSERT INTO ticket (flight_id, passenger_name, price) VALUES
(1, 'Ivan Ivanov', 200.00),
(1, 'Oleg Pavlov', 210.00),
(2, 'Petr Petrov', 150.00),
(2, 'Sergey Sidorov', 160.00),
(3, 'Maria Ivanova', 300.00),
(3, 'Anna Smirnova', 310.00),
(4, 'John Doe', 250.00),
(4, 'Dmitry Medvedev', 255.00),
(5, 'Jane Smith', 400.00),
(5, 'Yuri Gagarin', 420.00),
(6, 'Elena Nikitina', 350.00),
(6, 'Irina Alekseeva', 360.00),
(7, 'Aleksey Alekseev', 500.00),
(7, 'Andrey Volkov', 510.00),
(8, 'Alex Johnson', 450.00),
(8, 'Nikolay Nikolaev', 460.00),
(9, 'Olga Petrova', 600.00),
(9, 'Oksana Fedorova', 610.00),
(10, 'Michael Brown', 550.00),
(10, 'Vladimir Putin', 560.00);


