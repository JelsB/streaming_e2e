CREATE TABLE klant (
    klant_id serial PRIMARY key,
    naam VARCHAR (50) NOT NULL,
    email VARCHAR (255) NOT NULL,
    creation_date date NOT NULL DEFAULT CURRENT_DATE
);
CREATE table provincie (
    provincie_id serial PRIMARY key,
    naam VARCHAR(50) not null
);
CREATE TABLE klant_provincie (
    klant_id int REFERENCES klant(klant_id) ON UPDATE CASCADE ON DELETE CASCADE,
    provincie_id INT REFERENCES provincie(provincie_id) on UPDATE CASCADE,
    verhuis_date date NOT NULL DEFAULT CURRENT_DATE,
    CONSTRAINT bill_product_pkey PRIMARY KEY (klant_id, provincie_id) -- explicit pk
);
INSERT INTO klant(klant_id, naam, email)
values (1, 'Jan', 'jan@mail.com'),
    (2, 'Kev', 'kev@mail.com'),
    (3, 'Gman', 'gman@mail.com'),
    (4, 'Gloop', 'gloop@mail.com');
INSERT INTO provincie(provincie_id, naam)
values (1, 'Vlaams-Brabant'),
    (2, 'Limburg'),
    (3, 'Antwerpen'),
    (4, 'Waals-Branbant');
INSERT into klant_provincie(klant_id, provincie_id, verhuis_date)
VALUES (1, 1, '1996-12-02'),
    (1, 3, '2020-08-02'),
    (2, 1, '2000-02-01'),
    (3, 4, '2020-10-16'),
    (4, 2, '1990-10-20');