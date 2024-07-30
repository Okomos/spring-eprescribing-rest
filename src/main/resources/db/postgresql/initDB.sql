CREATE TABLE IF NOT EXISTS prescribers (
  id SERIAL,
  first_name VARCHAR(30),
  last_name VARCHAR(30),
  CONSTRAINT pk_prescribers PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_prescribers_last_name ON prescribers (last_name);

ALTER SEQUENCE prescribers_id_seq RESTART WITH 100;


CREATE TABLE IF NOT EXISTS specialties (
  id SERIAL,
  name VARCHAR(80),
  CONSTRAINT pk_specialties PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_specialties_name ON specialties (name);

ALTER SEQUENCE specialties_id_seq RESTART WITH 100;


CREATE TABLE IF NOT EXISTS prescriber_specialties (
  prescriber_id INT NOT NULL,
  specialty_id INT NOT NULL,
  FOREIGN KEY (prescriber_id) REFERENCES prescribers(id),
  FOREIGN KEY (specialty_id) REFERENCES specialties(id),
  CONSTRAINT unique_ids UNIQUE (prescriber_id,specialty_id)
);



CREATE TABLE IF NOT EXISTS types (
  id SERIAL,
  name VARCHAR(80),
  CONSTRAINT pk_types PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_types_name ON types (name);

ALTER SEQUENCE types_id_seq RESTART WITH 100;

CREATE TABLE IF NOT EXISTS owners (
  id SERIAL,
  first_name VARCHAR(30),
  last_name VARCHAR(30),
  address VARCHAR(255),
  city VARCHAR(80),
  telephone VARCHAR(20),
  CONSTRAINT pk_owners PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_owners_last_name ON owners (last_name);

ALTER SEQUENCE owners_id_seq RESTART WITH 100;


CREATE TABLE IF NOT EXISTS medications (
  id SERIAL,
  name VARCHAR(30),
  expiration_date DATE,
  type_id INT NOT NULL,
  owner_id INT NOT NULL,
  FOREIGN KEY (owner_id) REFERENCES owners(id),
  FOREIGN KEY (type_id) REFERENCES types(id),
  CONSTRAINT pk_medications PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_medications_name ON medications (name);

ALTER SEQUENCE medications_id_seq RESTART WITH 100;


CREATE TABLE IF NOT EXISTS prescriptions (
  id SERIAL,
  medication_id INT NOT NULL,
  prescription_date DATE,
  description VARCHAR(255),
  FOREIGN KEY (medication_id) REFERENCES medications(id),
  CONSTRAINT pk_prescriptions PRIMARY KEY (id)
);

ALTER SEQUENCE prescriptions_id_seq RESTART WITH 100;

CREATE TABLE IF NOT EXISTS users (
  username VARCHAR(20) NOT NULL ,
  password VARCHAR(20) NOT NULL ,
  enabled boolean NOT NULL DEFAULT true ,
  CONSTRAINT pk_users PRIMARY KEY (username)
);

CREATE TABLE IF NOT EXISTS roles (
  id SERIAL,
  username varchar(20) NOT NULL,
  role varchar(20) NOT NULL,
  CONSTRAINT pk_roles PRIMARY KEY (id),
  FOREIGN KEY (username) REFERENCES users (username)
);

ALTER TABLE roles ADD CONSTRAINT uni_username_role UNIQUE (role,username);
ALTER SEQUENCE roles_id_seq RESTART WITH 100;
