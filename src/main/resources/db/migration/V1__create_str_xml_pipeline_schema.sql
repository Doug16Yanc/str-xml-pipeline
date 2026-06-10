CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ── participant ───────────────────────────────────────────────────────────────
CREATE TABLE participant (
         id         UUID         NOT NULL DEFAULT gen_random_uuid(),
         ispb       CHAR(8)      NOT NULL,
         name       VARCHAR(255) NOT NULL,
         type       VARCHAR(30)  NOT NULL,
         account    VARCHAR(20),
         branch     VARCHAR(4),
         created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),

         CONSTRAINT pk_participant      PRIMARY KEY (id),
         CONSTRAINT uq_participant_ispb UNIQUE (ispb),
         CONSTRAINT ck_participant_type CHECK (type IN (
            'COMMERCIAL_BANK', 'MULTIPLE_BANK', 'FINTECH', 'COOPERATIVE', 'BACEN'
        )),
         CONSTRAINT ck_participant_ispb CHECK (ispb ~ '^\d{8}$')
);

INSERT INTO participant (ispb, name, type, branch, account) VALUES
       ('00000000', 'Banco Central do Brasil',        'BACEN',           NULL, NULL),
       ('00000208', 'Banco do Brasil S.A.',           'COMMERCIAL_BANK', '0001', '200000-2'),
       ('00360305', 'Caixa Economica Federal',        'COMMERCIAL_BANK', '0001', '300000-3'),
       ('60701190', 'Itaú Unibanco S.A.',             'MULTIPLE_BANK',   '0001', '400000-4'),
       ('90587081', 'Banco Santander (Brasil) S.A.',  'MULTIPLE_BANK',   '0001', '500000-5'),
       ('60746948', 'Banco Bradesco S.A.', 'MULTIPLE_BANK', '0001', '1234567-8'),
       ('23731301', 'Banco BTG Pactual S.A.', 'MULTIPLE_BANK', '0001', '987654-3'),
       ('07450604', 'Banco Original S.A.', 'MULTIPLE_BANK', NULL, NULL),
       ('18236120', 'Nu Pagamentos S.A. (Nubank)', 'FINTECH', '0001', '1928374-5'),
       ('17298092', 'Banco Inter S.A.', 'FINTECH', '0001', '554433-2'),
       ('13140088', 'Neon Pagamentos S.A.', 'FINTECH', NULL, NULL),
       ('33132224', 'PicPay Serviços S.A.', 'FINTECH', '0001', '887766-1'),
       ('08561701', 'C6 Bank S.A.', 'FINTECH', '0001', '102030-4'),
       ('02038232', 'Banco Cooperativo Sicredi S.A.', 'COOPERATIVE', '0100', '45678-9'),
       ('03032251', 'Banco Cooperativo Sicoob S.A.', 'COOPERATIVE', '0050', '32109-8'),
       ('03323840', 'Banco Citibank S.A.', 'COMMERCIAL_BANK', '0001', '112233-4'),
       ('40303299', 'Banco de Brasília S.A. (BRB)', 'MULTIPLE_BANK', '100', '99887-7')
ON CONFLICT (ispb) DO NOTHING;

-- ── file_batch ────────────────────────────────────────────────────────────────
CREATE TABLE file_batch (
        id             UUID          NOT NULL DEFAULT gen_random_uuid(),
        participant_id UUID          NOT NULL,
        window_code         VARCHAR(30)   NOT NULL,
        reference_date DATE          NOT NULL,
        status         VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
        total_orders   INT           NOT NULL DEFAULT 0,
        total_amount   NUMERIC(19,2) NOT NULL DEFAULT 0,
        generated_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
        sent_at        TIMESTAMPTZ,

        CONSTRAINT pk_file_batch        PRIMARY KEY (id),
        CONSTRAINT fk_batch_participant FOREIGN KEY (participant_id) REFERENCES participant (id),
        CONSTRAINT ck_batch_status      CHECK (status IN (
              'PENDING', 'EMITTED', 'CONFIRMED', 'REJECTED', 'REJECTED_CUTOFF'
        ))
);

CREATE INDEX idx_batch_participant   ON file_batch (participant_id);
CREATE INDEX idx_batch_window_status ON file_batch (window_code, status);
CREATE INDEX idx_batch_window_date   ON file_batch (window_code, reference_date, status);

-- Prevents duplicate active batches for the same window+date.
-- Partial unique index: only one non-rejected batch per window per day.
CREATE UNIQUE INDEX uq_batch_active_window
    ON file_batch (window_code, reference_date)
    WHERE status NOT IN ('REJECTED', 'REJECTED_CUTOFF');

-- ── settlement_order ──────────────────────────────────────────────────────────
CREATE TABLE settlement_order (
          id              UUID          NOT NULL DEFAULT gen_random_uuid(),
          originator_id   UUID          NOT NULL,
          destination_id  UUID          NOT NULL,
          batch_id        UUID,
          window_code          VARCHAR(30)   NOT NULL,
          order_type      VARCHAR(10)   NOT NULL,
          amount          NUMERIC(19,2) NOT NULL,
          currency        CHAR(3)       NOT NULL DEFAULT 'BRL',
          settlement_date DATE          NOT NULL,
          end_to_end_id   VARCHAR(35)   NOT NULL,
          status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
          created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
          updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
          version         BIGINT        NOT NULL DEFAULT 0,

          CONSTRAINT pk_settlement_order          PRIMARY KEY (id),
          CONSTRAINT uq_order_e2e                 UNIQUE (end_to_end_id),
          CONSTRAINT fk_order_originator          FOREIGN KEY (originator_id)  REFERENCES participant (id),
          CONSTRAINT fk_order_destination         FOREIGN KEY (destination_id) REFERENCES participant (id),
          CONSTRAINT fk_order_batch               FOREIGN KEY (batch_id)       REFERENCES file_batch (id),
          CONSTRAINT ck_order_type                CHECK (order_type IN ('TED', 'DOC', 'STR', 'PIX')),
          CONSTRAINT ck_order_status              CHECK (status IN (
                                                                    'PENDING', 'BATCHED', 'EMITTED', 'ACCEPTED', 'REJECTED', 'REJECTED_CUTOFF'
              )),
          CONSTRAINT ck_order_amount              CHECK (amount > 0),
          CONSTRAINT ck_order_no_self_settlement  CHECK (originator_id <> destination_id)
);

CREATE INDEX idx_order_batch        ON settlement_order (batch_id)       WHERE batch_id IS NOT NULL;
CREATE INDEX idx_order_status_date  ON settlement_order (status, settlement_date);
CREATE INDEX idx_order_originator   ON settlement_order (originator_id);

-- Core query path: scheduler fetches PENDING orders per window+date
CREATE INDEX idx_order_pending_window_date
    ON settlement_order (window_code, settlement_date, status)
    WHERE status = 'PENDING' AND batch_id IS NULL;

COMMENT ON CONSTRAINT ck_order_no_self_settlement ON settlement_order IS
    'Internal settlements between same participant must not transit through STR — resolve via internal ledger';

-- ── xml_file ──────────────────────────────────────────────────────────────────
CREATE TABLE xml_file (
          id              UUID         NOT NULL DEFAULT gen_random_uuid(),
          batch_id        UUID         NOT NULL,
          s3_bucket       VARCHAR(63)  NOT NULL,
          s3_key          VARCHAR(512) NOT NULL,
          checksum_sha256 CHAR(64)     NOT NULL,
          size_bytes      BIGINT       NOT NULL,
          xsd_version     VARCHAR(20)  NOT NULL DEFAULT '2.0',
          emitted_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),

          CONSTRAINT pk_xml_file          PRIMARY KEY (id),
          CONSTRAINT uq_xml_batch         UNIQUE (batch_id),
          CONSTRAINT uq_xml_checksum      UNIQUE (checksum_sha256),
          CONSTRAINT fk_xml_batch         FOREIGN KEY (batch_id) REFERENCES file_batch (id),
          CONSTRAINT ck_xml_checksum      CHECK (checksum_sha256 ~ '^[a-f0-9]{64}$'),
    CONSTRAINT ck_xml_size          CHECK (size_bytes > 0)
);

CREATE INDEX idx_xml_s3_key ON xml_file (s3_key);

-- ── settlement_return ─────────────────────────────────────────────────────────
CREATE TABLE settlement_return (
           id               UUID        NOT NULL DEFAULT gen_random_uuid(),
           batch_id         UUID        NOT NULL,
           file_id          UUID        NOT NULL,
           result           VARCHAR(10) NOT NULL,
           message_code     VARCHAR(20) NOT NULL,
           description      TEXT        NOT NULL,
           rejection_reason VARCHAR(30),
           received_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

           CONSTRAINT pk_settlement_return       PRIMARY KEY (id),
           CONSTRAINT uq_return_batch            UNIQUE (batch_id),
           CONSTRAINT fk_return_batch            FOREIGN KEY (batch_id) REFERENCES file_batch (id),
           CONSTRAINT fk_return_file             FOREIGN KEY (file_id)  REFERENCES xml_file (id),
           CONSTRAINT ck_return_result           CHECK (result IN ('ACCEPTED', 'REJECTED')),
           CONSTRAINT ck_return_rejection_reason CHECK (
               (result = 'ACCEPTED' AND rejection_reason IS NULL) OR
               (result = 'REJECTED' AND rejection_reason IS NOT NULL)
               ),
           CONSTRAINT ck_return_rejection_value  CHECK (rejection_reason IN (
                 'INSUFFICIENT_RESERVES',
                 'INACTIVE_PARTICIPANT',
                 'INVALID_XSD',
                 'WINDOW_CLOSED',
                 'DUPLICATE_FILE',
                 'STR_INTERNAL_ERROR'
            ))
);

-- uq_return_batch already creates an implicit index on batch_id
CREATE INDEX idx_return_file_id ON settlement_return (file_id);

COMMENT ON TABLE settlement_return IS
    'Immutable record of every BACEN/STR return. One return per batch — enforced by uq_return_batch.';

COMMENT ON COLUMN settlement_return.rejection_reason IS
    'NULL when result = ACCEPTED. Enum value name from RejectionReason — human-readable for audit queries.';

-- ── raw_settlement_return ─────────────────────────────────────────────────────
CREATE TABLE raw_settlement_return (
       id              UUID        NOT NULL DEFAULT gen_random_uuid(),
       raw_payload     TEXT        NOT NULL,
       batch_id        UUID,
       kafka_partition INT         NOT NULL,
       kafka_offset    BIGINT      NOT NULL,
       received_at     TIMESTAMPTZ NOT NULL DEFAULT now(),

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

-- ── roles ─────────────────────────────────────────────────────────────────────
CREATE TABLE roles (
       id          UUID         NOT NULL DEFAULT gen_random_uuid(),
       role_type   VARCHAR(30)  NOT NULL,
       description VARCHAR(255) NOT NULL,

       CONSTRAINT pk_roles       PRIMARY KEY (id),
       CONSTRAINT uq_roles_type  UNIQUE (role_type),
       CONSTRAINT ck_roles_type  CHECK (role_type IN (
          'SETTLEMENT_OPERATOR', 'BACEN_AUDITOR', 'ADMIN'
       ))
);

INSERT INTO roles (role_type, description) VALUES
       ('SETTLEMENT_OPERATOR', 'Bank operator — submits settlement orders on behalf of their institution'),
       ('BACEN_AUDITOR',       'BACEN fiscal auditor — read-only access across all participants'),
       ('ADMIN',               'System administrator — manages participants and users');

-- ── users ─────────────────────────────────────────────────────────────────────
CREATE TABLE users (
       id            UUID         NOT NULL DEFAULT gen_random_uuid(),
       name          VARCHAR(50)  NOT NULL,
       password_hash VARCHAR(255) NOT NULL,
       role_id       UUID         NOT NULL,
       ispb          CHAR(8),
       created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),

       CONSTRAINT pk_users      PRIMARY KEY (id),
       CONSTRAINT uq_users_name UNIQUE (name),
       CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE INDEX idx_users_role_id ON users (role_id);
CREATE INDEX idx_users_ispb ON users (ispb);