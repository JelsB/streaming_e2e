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