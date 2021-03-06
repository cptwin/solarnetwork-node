CREATE TABLE solarnode.sn_consum_datum (
	id				BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
	created			TIMESTAMP NOT NULL WITH DEFAULT CURRENT_TIMESTAMP,
	source_id 		VARCHAR(255) NOT NULL,
	price_loc_id	BIGINT,
	amps			DOUBLE,
	voltage			DOUBLE,
	watt_hour		BIGINT,
	error_msg		VARCHAR(32672),
	PRIMARY KEY (id)
);

CREATE INDEX consum_datum_created_idx ON solarnode.sn_consum_datum (created);

INSERT INTO solarnode.sn_settings (skey, svalue) 
VALUES ('solarnode.sn_consum_datum.version', '6');

CREATE TABLE solarnode.sn_consum_datum_upload (
	consum_datum_id	BIGINT NOT NULL,
	destination		VARCHAR(255) NOT NULL,
	created			TIMESTAMP NOT NULL WITH DEFAULT CURRENT_TIMESTAMP,
	track_id		BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
	PRIMARY KEY (consum_datum_id, destination)
);

ALTER TABLE solarnode.sn_consum_datum_upload ADD CONSTRAINT
sn_consum_datum_upload_consum_datum_fk FOREIGN KEY (consum_datum_id)
REFERENCES solarnode.sn_consum_datum ON DELETE CASCADE;

INSERT INTO solarnode.sn_settings (skey, svalue) 
VALUES ('solarnode.sn_consum_datum_upload.version', '1');
