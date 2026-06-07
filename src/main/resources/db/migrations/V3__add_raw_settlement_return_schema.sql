CREATE TABLE raw_settlement_return (
       id               UUID        NOT NULL,
       raw_payload      TEXT        NOT NULL,
       batch_id         UUID,
       kafka_partition  INT         NOT NULL,
       kafka_offset     BIGINT      NOT NULL,
       received_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

       CONSTRAINT pk_raw_settlement_return PRIMARY KEY (id),
       CONSTRAINT fk_raw_return_batch      FOREIGN KEY (batch_id) REFERENCES file_batch (id)
);

CREATE INDEX idx_raw_return_batch_id
    ON raw_settlement_return (batch_id)
    WHERE batch_id IS NOT NULL;

CREATE UNIQUE INDEX uq_raw_return_kafka_coords
    ON raw_settlement_return (kafka_partition, kafka_offset);

COMMENT ON TABLE raw_settlement_return IS
    'Immutable audit log of every raw XML payload received from BACEN/STR. '
    'Committed in REQUIRES_NEW — survives processing failures and rollbacks.';

COMMENT ON COLUMN raw_settlement_return.batch_id IS
    'Populated after successful XML parsing. NULL indicates parse failure — '
    'raw payload preserved for manual inspection.';