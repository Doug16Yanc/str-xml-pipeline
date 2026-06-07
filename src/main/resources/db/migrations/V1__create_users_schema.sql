CREATE TABLE roles (
       id          UUID         NOT NULL,
       role_type   VARCHAR(30)  NOT NULL,
       description VARCHAR(255) NOT NULL,

       CONSTRAINT pk_roles          PRIMARY KEY (id),
       CONSTRAINT uq_roles_type     UNIQUE (role_type),
       CONSTRAINT chk_roles_type    CHECK (role_type IN (
            'SETTLEMENT_OPERATOR', 'BACEN_AUDITOR', 'ADMIN'
        ))
);

CREATE UNIQUE INDEX idx_roles_role_type ON roles (role_type);

CREATE TABLE users (
       id            UUID         NOT NULL,
       name          VARCHAR(50)  NOT NULL,
       password_hash VARCHAR(255) NOT NULL,
       role_id       UUID         NOT NULL,
       created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

       CONSTRAINT pk_users         PRIMARY KEY (id),
       CONSTRAINT uq_users_name    UNIQUE (name),
       CONSTRAINT fk_users_role    FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE UNIQUE INDEX idx_users_name    ON users (name);
CREATE INDEX idx_users_role_id ON users (role_id);