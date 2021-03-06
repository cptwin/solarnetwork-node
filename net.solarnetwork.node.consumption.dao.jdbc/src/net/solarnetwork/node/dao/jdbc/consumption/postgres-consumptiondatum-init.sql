CREATE TABLE solarnode.sn_consum_datum (
	id				BIGINT NOT NULL DEFAULT nextval('solarnode.solarnode_seq'),
	created			TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
	source_id 		VARCHAR(255) NOT NULL,
	amps			REAL,
	voltage			REAL,
	error_msg		TEXT,
	PRIMARY KEY (id)
);

CREATE INDEX consum_datum_created_idx ON solarnode.sn_consum_datum (created);

CREATE TABLE solarnode.sn_consum_datum_upload (
	consum_datum_id	BIGINT NOT NULL,
	destination		VARCHAR(255) NOT NULL,
	created			TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
	track_id		BIGINT NOT NULL,
	PRIMARY KEY (consum_datum_id, destination)
);

ALTER TABLE solarnode.sn_consum_datum_upload ADD CONSTRAINT
sn_consum_datum_upload_consum_datum_fk FOREIGN KEY (consum_datum_id)
REFERENCES solarnode.sn_consum_datum ON DELETE CASCADE;

INSERT INTO solarnode.sn_settings (skey, svalue) 
VALUES ('solarnode.sn_consum_datum_upload.version', '1');

INSERT INTO solarnode.sn_settings (skey, svalue) 
VALUES ('solarnode.sn_consum_datum.version', '4');
