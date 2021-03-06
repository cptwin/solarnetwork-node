CREATE TABLE solarnode.sn_energy_capacity_datum (
	id				BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
	created			TIMESTAMP NOT NULL WITH DEFAULT CURRENT_TIMESTAMP,
	source_id 		VARCHAR(255) NOT NULL,
	voltage			REAL,
	amp_hour		DOUBLE PRECISION,
	watt_hour		DOUBLE PRECISION,
	CONSTRAINT sn_energy_capacity_datum_pkey PRIMARY KEY (id)
);

CREATE INDEX energy_capacity_datum_created_idx 
ON solarnode.sn_energy_capacity_datum (created);

INSERT INTO solarnode.sn_settings (skey, svalue) 
VALUES ('solarnode.sn_energy_capacity_datum.version', '1');

CREATE TABLE solarnode.sn_sn_energy_capacity_datum_upload (
	datum_id		BIGINT NOT NULL,
	destination		VARCHAR(255) NOT NULL,
	created			TIMESTAMP NOT NULL WITH DEFAULT CURRENT_TIMESTAMP,
	track_id		BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
	CONSTRAINT sn_energy_capacity_datum_upload_pkey 
		PRIMARY KEY (datum_id, destination),
	CONSTRAINT sn_energy_capacity_datum_upload_datum_fk 
		FOREIGN KEY (datum_id)
		REFERENCES solarnode.sn_energy_capacity_datum ON DELETE CASCADE
);

INSERT INTO solarnode.sn_settings (skey, svalue) 
VALUES ('solarnode.sn_energy_capacity_datum_upload.version', '1');
