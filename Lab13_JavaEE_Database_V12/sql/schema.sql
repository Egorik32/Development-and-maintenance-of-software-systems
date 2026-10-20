DROP TABLE IF EXISTS cities;
DROP TABLE IF EXISTS parks;

CREATE TABLE parks
(
    id serial PRIMARY KEY,
    name character varying(80) NOT NULL,
    type character varying(50) NOT NULL,
    area numeric(10, 2) NOT NULL,
    has_water boolean NOT NULL,
    build_date date NOT NULL
);

CREATE TABLE cities
(
    id serial PRIMARY KEY,
    park_id integer NOT NULL REFERENCES parks(id) ON DELETE RESTRICT,
    name character varying(80) NOT NULL,
    status character varying(80) NOT NULL,
    population integer NOT NULL
);

INSERT INTO parks (name, type, area, has_water, build_date) VALUES
('Парк имени Горького', 'городской', 19.50, true, '1928-08-12'),
('Парк Революции', 'культурный', 17.00, false, '1926-05-01'),
('Олимпийский парк', 'спортивный', 200.00, true, '2014-02-07');

INSERT INTO cities (park_id, name, status, population) VALUES
(1, 'Ростов-на-Дону', 'областной центр', 1142162),
(2, 'Шахты', 'город областного значения', 226452),
(3, 'Сочи', 'курортный город', 466078);
