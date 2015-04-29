-- CREATE DATABASE observations
-- 	ENCODING = 'UTF8'
-- 	TABLESPACE = pg_default
-- 	OWNER = postgres

 CREATE ROLE sensordb WITH 
 	INHERIT
 	LOGIN;

 CREATE ROLE observations_read WITH 
 	INHERIT;

CREATE ROLE observations_write WITH 
	INHERIT;

CREATE ROLE observations_admin WITH 
	INHERIT;

CREATE EXTENSION postgis;
CREATE EXTENSION postgis_topology;
CREATE EXTENSION "uuid-ossp";

CREATE TABLE public.devices(
	id uuid NOT NULL DEFAULT uuid_generate_v4(),
	serial_number character varying,
	device_type character varying,
	notes text,
	CONSTRAINT devices_pk PRIMARY KEY (id),
	CONSTRAINT unique_serial_number UNIQUE (serial_number)

);
ALTER TABLE public.devices OWNER TO observations_admin;

CREATE TABLE public.sensors(
	id uuid NOT NULL DEFAULT uuid_generate_v4(),
	device_id uuid NOT NULL,
	label character varying NOT NULL,
	type_of_measurement character varying NOT NULL,
	unit_of_measurement character varying,
	accuracy numeric,
	precision numeric,
	height_in_metres numeric,
	serial_number character varying,
	CONSTRAINT sensors_pk PRIMARY KEY (id)

);
ALTER TABLE public.sensors OWNER TO observations_admin;

CREATE TABLE public.locations(
	id uuid NOT NULL DEFAULT uuid_generate_v4(),
	name character varying,
	responsible_party character varying,
	coordinates geometry NOT NULL,
	elevation integer NOT NULL,
	CONSTRAINT locations_pk PRIMARY KEY (id)

);
ALTER TABLE public.locations OWNER TO observations_admin;

CREATE TABLE public.devices_locations(
	id uuid NOT NULL DEFAULT uuid_generate_v4(),
	timestamp timestamp NOT NULL,
	device_id uuid NOT NULL,
	location_id uuid NOT NULL,
	notes text,
	CONSTRAINT device_location_pk PRIMARY KEY (id)

);
ALTER TABLE public.devices_locations OWNER TO observations_admin;

CREATE TABLE public.observations(
	id uuid NOT NULL DEFAULT uuid_generate_v4(),
	sensor_id uuid NOT NULL,
	timestamp timestamp NOT NULL,
	numeric_value numeric,
	text_value text,
	CONSTRAINT observations_pk PRIMARY KEY (id)

);
ALTER TABLE public.observations OWNER TO observations_admin;

CREATE TABLE public.dois(
	id uuid NOT NULL DEFAULT uuid_generate_v4(),
	doi character varying NOT NULL,
	notes text,
	CONSTRAINT dois_pk PRIMARY KEY (id)

);
ALTER TABLE public.dois OWNER TO observations_admin;

CREATE TABLE public.observations_dois(
	id uuid NOT NULL DEFAULT uuid_generate_v4(),
	observation_id uuid NOT NULL,
	doi_id uuid NOT NULL
);
ALTER TABLE public.observations_dois OWNER TO observations_admin;

ALTER TABLE public.sensors ADD CONSTRAINT fk_device_id FOREIGN KEY (device_id)
REFERENCES public.devices (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE public.devices_locations ADD CONSTRAINT device_location_fk_device FOREIGN KEY (device_id)
REFERENCES public.devices (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE public.devices_locations ADD CONSTRAINT device_location_fk_location FOREIGN KEY (location_id)
REFERENCES public.locations (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE public.observations ADD CONSTRAINT observations_sensor_fk FOREIGN KEY (sensor_id)
REFERENCES public.sensors (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE public.observations_dois ADD CONSTRAINT observation_doi_observation_fk FOREIGN KEY (observation_id)
REFERENCES public.observations (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE public.observations_dois ADD CONSTRAINT observation_doi_doi_fk FOREIGN KEY (doi_id)
REFERENCES public.dois (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
