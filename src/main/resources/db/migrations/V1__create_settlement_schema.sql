CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE participant (
     id         UUID         NOT NULL DEFAULT gen_random_uuid(),
     ispb       CHAR(8)      NOT NULL,
     name       VARCHAR(255) NOT NULL,
     type       VARCHAR(30)  NOT NULL,
     created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),

     CONSTRAINT pk_participant PRIMARY KEY (id),
     CONSTRAINT uq_participant_ispb UNIQUE (ispb),
     CONSTRAINT ck_participant_type CHECK (type IN (
        'COMMERCIAL_BANK', 'MULTIPLE_BANK', 'FINTECH', 'COOPERATIVE', 'BACEN'
     ))
);

CREATE TABLE file_batch (
    id             UUID          NOT NULL DEFAULT gen_random_uuid(),
    participant_id UUID          NOT NULL,
    window         VARCHAR(30)   NOT NULL,
    status         VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    total_orders   INT           NOT NULL DEFAULT 0,
    total_amount   NUMERIC(19,2) NOT NULL DEFAULT 0,
    generated_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    sent_at        TIMESTAMPTZ,

    CONSTRAINT pk_file_batch PRIMARY KEY (id),
    CONSTRAINT fk_batch_participant FOREIGN KEY (participant_id)
        REFERENCES participant(id),
    CONSTRAINT ck_batch_status CHECK (status IN (
         'PENDING', 'PROCESSING', 'EMITTED', 'CONFIRMED', 'REJECTED', 'ERROR'
    ))
);

CREATE INDEX idx_batch_participant    ON file_batch(participant_id);
CREATE INDEX idx_batch_window_status  ON file_batch(window, status);

CREATE TABLE settlement_order (
      id                     UUID          NOT NULL DEFAULT gen_random_uuid(),
      originator_id          UUID          NOT NULL,
      destination_id         UUID          NOT NULL,
      batch_id               UUID,
      order_type             VARCHAR(10)   NOT NULL,
      amount                 NUMERIC(19,2) NOT NULL,
      currency               CHAR(3)       NOT NULL DEFAULT 'BRL',
      settlement_date        DATE          NOT NULL,
      status                 VARCHAR(20)   NOT NULL DEFAULT 'AWAITING_BATCH',
      created_at             TIMESTAMPTZ   NOT NULL DEFAULT now(),

      CONSTRAINT pk_settlement_order PRIMARY KEY (id),
      CONSTRAINT fk_order_originator FOREIGN KEY (originator_id)
          REFERENCES participant(id),
      CONSTRAINT fk_order_destination FOREIGN KEY (destination_id)
          REFERENCES participant(id),
      CONSTRAINT fk_order_batch FOREIGN KEY (batch_id)
          REFERENCES file_batch(id),
      CONSTRAINT ck_order_type CHECK (order_type IN ('TED', 'DOC', 'STR', 'PIX')),
      CONSTRAINT ck_order_status CHECK (status IN (
                                                   'AWAITING_BATCH', 'BATCHED', 'EMITTED', 'SETTLED', 'REJECTED'
          )),
      CONSTRAINT ck_order_amount CHECK (amount > 0)
);

CREATE INDEX idx_order_batch        ON settlement_order(batch_id);
CREATE INDEX idx_order_status_date  ON settlement_order(status, settlement_date);
CREATE INDEX idx_order_originator   ON settlement_order(originator_id);

CREATE TABLE xml_file (
      id              UUID         NOT NULL DEFAULT gen_random_uuid(),
      batch_id        UUID         NOT NULL,
      s3_bucket       VARCHAR(63)  NOT NULL,
      s3_key          VARCHAR(512) NOT NULL,
      checksum_sha256 CHAR(64)     NOT NULL,
      size_bytes      BIGINT       NOT NULL,
      xsd_version     VARCHAR(20)  NOT NULL DEFAULT '1.0',
      emitted_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),

      CONSTRAINT pk_xml_file PRIMARY KEY (id),
      CONSTRAINT fk_xml_batch FOREIGN KEY (batch_id)
          REFERENCES file_batch(id),
      CONSTRAINT uq_xml_batch UNIQUE (batch_id)
);

CREATE INDEX idx_xml_s3_key ON xml_file(s3_key);

CREATE TABLE settlement_response (
     id            UUID        NOT NULL DEFAULT gen_random_uuid(),
     batch_id      UUID        NOT NULL,
     response_code VARCHAR(10) NOT NULL,
     description   TEXT        NOT NULL,
     status        VARCHAR(20) NOT NULL,
     received_at   TIMESTAMPTZ NOT NULL DEFAULT now(),

     CONSTRAINT pk_settlement_response PRIMARY KEY (id),
     CONSTRAINT fk_response_batch FOREIGN KEY (batch_id)
         REFERENCES file_batch(id),
     CONSTRAINT ck_response_status CHECK (status IN (
         'ACCEPTED', 'REJECTED', 'PENDING_ANALYSIS'
     ))
);

CREATE INDEX idx_response_batch  ON settlement_response(batch_id);
CREATE INDEX idx_response_status ON settlement_response(status);

INSERT INTO participant (ispb, name, type) VALUES
    ('00000000', 'Banco Central do Brasil', 'BACEN'),
    ('00000208', 'Banco BRB',              'COMMERCIAL_BANK'),
    ('00360305', 'Caixa Economica Federal',  'COMMERCIAL_BANK'),
    ('60701190', 'Itau Unibanco',            'MULTIPLE_BANK'),
    ('90587081', 'Santander Brasil',         'MULTIPLE_BANK')
ON CONFLICT (ispb) DO NOTHING;