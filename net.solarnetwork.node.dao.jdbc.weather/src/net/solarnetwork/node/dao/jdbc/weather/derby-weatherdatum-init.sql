CREATE TABLE solarnode.sn_weather_datum (
	id				BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
	created			TIMESTAMP NOT NULL WITH DEFAULT CURRENT_TIMESTAMP,
	source_id 		VARCHAR(255),
	info_date		TIMESTAMP NOT NULL,
	sky_cond		VARCHAR(255),
	temperature		DOUBLE,
	humidity		DOUBLE,
	bar_pressure	DOUBLE,
	bar_delta		VARCHAR(255),
	visibility		DOUBLE,
	uv_index		INTEGER,
	dew_point		DOUBLE,	
	error_msg		VARCHAR(32672),
	PRIMARY KEY (id)
);

CREATE INDEX weather_datum_created_idx ON solarnode.sn_weather_datum (created);

INSERT INTO solarnode.sn_settings (skey, svalue) 
VALUES ('solarnode.sn_weather_datum.version', '1');

CREATE TABLE solarnode.sn_weather_datum_upload (
	datum_id		BIGINT NOT NULL,
	destination		VARCHAR(255) NOT NULL,
	created			TIMESTAMP NOT NULL WITH DEFAULT CURRENT_TIMESTAMP,
	track_id		BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
	PRIMARY KEY (datum_id, destination)
);

ALTER TABLE solarnode.sn_weather_datum_upload ADD CONSTRAINT
sn_weather_datum_upload_weather_datum_fk FOREIGN KEY (datum_id)
REFERENCES solarnode.sn_weather_datum ON DELETE CASCADE;

INSERT INTO solarnode.sn_settings (skey, svalue) 
VALUES ('solarnode.sn_weather_datum_upload.version', '1');
