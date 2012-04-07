CREATE TABLE solarnode.sn_day_datum (
	id				BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
	created			TIMESTAMP NOT NULL WITH DEFAULT CURRENT_TIMESTAMP,
	source_id 		VARCHAR(255),
	tz				VARCHAR(255) NOT NULL,
	latitude		DOUBLE,
	longitude		DOUBLE,
	sunrise			TIME,
	sunset			TIME,
	error_msg		VARCHAR(32672),
	PRIMARY KEY (id)
);

CREATE INDEX day_datum_created_idx ON solarnode.sn_day_datum (created);

INSERT INTO solarnode.sn_settings (skey, svalue) 
VALUES ('solarnode.sn_day_datum.version', '1');

CREATE TABLE solarnode.sn_day_datum_upload (
	datum_id		BIGINT NOT NULL,
	destination		VARCHAR(255) NOT NULL,
	created			TIMESTAMP NOT NULL WITH DEFAULT CURRENT_TIMESTAMP,
	track_id		BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
	PRIMARY KEY (datum_id, destination)
);

ALTER TABLE solarnode.sn_day_datum_upload ADD CONSTRAINT
sn_day_datum_upload_day_datum_fk FOREIGN KEY (datum_id)
REFERENCES solarnode.sn_day_datum ON DELETE CASCADE;

INSERT INTO solarnode.sn_settings (skey, svalue) 
VALUES ('solarnode.sn_day_datum_upload.version', '1');
