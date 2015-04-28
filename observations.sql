-- Database generated with pgModeler (PostgreSQL Database Modeler).
-- pgModeler  version: 0.8.0
-- PostgreSQL version: 9.4
-- Project Site: pgmodeler.com.br
-- Model Author: ---


-- Database creation must be done outside an multicommand file.
-- These commands were put in this file only for convenience.
-- -- object: observations | type: DATABASE --
-- -- DROP DATABASE IF EXISTS observations;
-- 
-- -- Prepended SQL commands --
-- -- Enable PostGIS (includes raster)
-- CREATE EXTENSION postgis;
-- -- Enable Topology
-- CREATE EXTENSION postgis_topology;
-- -- ddl-end --
-- 
-- CREATE DATABASE observations
-- 	OWNER = postgres
-- ;
-- -- ddl-end --
-- 

-- object: public.devices | type: TABLE --
-- DROP TABLE IF EXISTS public.devices CASCADE;
CREATE TABLE public.devices(
	id serial NOT NULL,
	serial_number varchar,
	device_type varchar,
	notes text,
	CONSTRAINT devices_pk PRIMARY KEY (id),
	CONSTRAINT unique_serial_number UNIQUE (serial_number)

);
-- ddl-end --
ALTER TABLE public.devices OWNER TO postgres;
-- ddl-end --

-- object: public.sensors | type: TABLE --
-- DROP TABLE IF EXISTS public.sensors CASCADE;
CREATE TABLE public.sensors(
	id serial NOT NULL,
	serial_number varchar,
	device_id integer NOT NULL,
	type_of_measurement varchar NOT NULL,
	unit_of_measurement varchar,
	accuracy numeric,
	precision numeric,
	height_in_metres numeric,
	CONSTRAINT sensors_pk PRIMARY KEY (id)

);
-- ddl-end --
ALTER TABLE public.sensors OWNER TO postgres;
-- ddl-end --

-- object: public.locations | type: TABLE --
-- DROP TABLE IF EXISTS public.locations CASCADE;
CREATE TABLE public.locations(
	id serial NOT NULL,
	name varchar,
	responsible_party varchar,
	coordinates geometry(POINT, 4326) NOT NULL,
	elevation integer,
	CONSTRAINT locations_pk PRIMARY KEY (id)

);
-- ddl-end --
ALTER TABLE public.locations OWNER TO postgres;
-- ddl-end --

-- object: public.device_location | type: TABLE --
-- DROP TABLE IF EXISTS public.device_location CASCADE;
CREATE TABLE public.device_location(
	id bigserial NOT NULL,
	timestamp timestamp NOT NULL,
	device_id integer NOT NULL,
	location_id integer NOT NULL,
	notes text,
	CONSTRAINT device_location_pk PRIMARY KEY (id)

);
-- ddl-end --
ALTER TABLE public.device_location OWNER TO postgres;
-- ddl-end --

-- object: public.observations | type: TABLE --
-- DROP TABLE IF EXISTS public.observations CASCADE;
CREATE TABLE public.observations(
	id bigint NOT NULL,
	sensor_id integer NOT NULL,
	timestamp timestamp NOT NULL,
	numeric_value numeric,
	text_value text,
	CONSTRAINT observations_pk PRIMARY KEY (id)

);
-- ddl-end --
ALTER TABLE public.observations OWNER TO postgres;
-- ddl-end --

-- object: public.dois | type: TABLE --
-- DROP TABLE IF EXISTS public.dois CASCADE;
CREATE TABLE public.dois(
	id serial NOT NULL,
	doi varchar NOT NULL,
	notes text,
	CONSTRAINT dois_pk PRIMARY KEY (id)

);
-- ddl-end --
ALTER TABLE public.dois OWNER TO postgres;
-- ddl-end --

-- object: public.observation_doi | type: TABLE --
-- DROP TABLE IF EXISTS public.observation_doi CASCADE;
CREATE TABLE public.observation_doi(
	id bigserial NOT NULL,
	observation_id bigint NOT NULL,
	doi_id bigint NOT NULL
);
-- ddl-end --
ALTER TABLE public.observation_doi OWNER TO postgres;
-- ddl-end --

-- object: fk_device_id | type: CONSTRAINT --
-- ALTER TABLE public.sensors DROP CONSTRAINT IF EXISTS fk_device_id CASCADE;
ALTER TABLE public.sensors ADD CONSTRAINT fk_device_id FOREIGN KEY (device_id)
REFERENCES public.devices (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: device_location_fk_device | type: CONSTRAINT --
-- ALTER TABLE public.device_location DROP CONSTRAINT IF EXISTS device_location_fk_device CASCADE;
ALTER TABLE public.device_location ADD CONSTRAINT device_location_fk_device FOREIGN KEY (device_id)
REFERENCES public.devices (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: device_location_fk_location | type: CONSTRAINT --
-- ALTER TABLE public.device_location DROP CONSTRAINT IF EXISTS device_location_fk_location CASCADE;
ALTER TABLE public.device_location ADD CONSTRAINT device_location_fk_location FOREIGN KEY (location_id)
REFERENCES public.locations (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: observations_sensor_fk | type: CONSTRAINT --
-- ALTER TABLE public.observations DROP CONSTRAINT IF EXISTS observations_sensor_fk CASCADE;
ALTER TABLE public.observations ADD CONSTRAINT observations_sensor_fk FOREIGN KEY (sensor_id)
REFERENCES public.sensors (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: observation_doi_observation_fk | type: CONSTRAINT --
-- ALTER TABLE public.observation_doi DROP CONSTRAINT IF EXISTS observation_doi_observation_fk CASCADE;
ALTER TABLE public.observation_doi ADD CONSTRAINT observation_doi_observation_fk FOREIGN KEY (observation_id)
REFERENCES public.observations (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: observation_doi_doi_fk | type: CONSTRAINT --
-- ALTER TABLE public.observation_doi DROP CONSTRAINT IF EXISTS observation_doi_doi_fk CASCADE;
ALTER TABLE public.observation_doi ADD CONSTRAINT observation_doi_doi_fk FOREIGN KEY (doi_id)
REFERENCES public.dois (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --


