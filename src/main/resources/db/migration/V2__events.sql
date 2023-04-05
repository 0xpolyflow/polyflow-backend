CREATE TYPE polyflow.WALLET_STATE AS (
    wallet_address VARCHAR,
    gas_balance    NUMERIC(78),
    nonce          NUMERIC(78),
    network_id     BIGINT
);

CREATE TYPE polyflow.NETWORK_STATE AS (
    chain_id     BIGINT,
    gas_price    NUMERIC(78),
    block_height NUMERIC(78)
);

CREATE TYPE polyflow.EVENT_TRACKER_MODEL AS (
    event_tracker VARCHAR,
    user_id       VARCHAR,
    session_id    VARCHAR,
    utm_source    VARCHAR,
    utm_medium    VARCHAR,
    utm_campaign  VARCHAR,
    utm_content   VARCHAR,
    utm_term      VARCHAR,
    origin        VARCHAR,
    path          VARCHAR
);

CREATE TYPE polyflow.SCREEN_STATE AS (
    w INT,
    h INT
);

CREATE TYPE polyflow.DEVICE_STATE AS (
    os              VARCHAR,
    browser         VARCHAR,
    country         VARCHAR,
    screen          SCREEN_STATE,
    wallet_provider VARCHAR
);

CREATE TYPE polyflow.TX_STATUS AS ENUM ('PENDING', 'SUCCESS', 'FAILURE');

CREATE TYPE polyflow.TX_DATA AS (
    from_address             VARCHAR,
    to_address               VARCHAR,
    tx_value                 NUMERIC(78),
    tx_input                 VARCHAR,
    nonce                    NUMERIC(78),
    gas                      NUMERIC(78),
    gas_price                NUMERIC(78),
    max_fee_per_gas          NUMERIC(78),
    max_priority_fee_per_gas NUMERIC(78),
    v                        VARCHAR,
    r                        VARCHAR,
    s                        VARCHAR,
    hash                     VARCHAR,
    status                   TX_STATUS
);

CREATE DOMAIN polyflow.EVENT_ID AS UUID;

CREATE TABLE polyflow.wallet_connected_event (
    id         EVENT_ID                 PRIMARY KEY,
    project_id PROJECT_ID               NOT NULL REFERENCES polyflow.project(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    tracker    EVENT_TRACKER_MODEL      NOT NULL,
    wallet     WALLET_STATE             NOT NULL,
    device     DEVICE_STATE             NOT NULL,
    network    NETWORK_STATE            NOT NULL
);

CREATE INDEX wallet_connected_event_project_id ON polyflow.wallet_connected_event(project_id);
CREATE INDEX wallet_connected_event_created_at ON polyflow.wallet_connected_event(created_at);

CREATE TABLE polyflow.tx_request_event (
    id         EVENT_ID                 PRIMARY KEY,
    project_id PROJECT_ID               NOT NULL REFERENCES polyflow.project(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    tracker    EVENT_TRACKER_MODEL      NOT NULL,
    wallet     WALLET_STATE             NOT NULL,
    device     DEVICE_STATE             NOT NULL,
    network    NETWORK_STATE            NOT NULL,
    tx         TX_DATA                  NOT NULL
);

CREATE INDEX tx_request_event_project_id ON polyflow.tx_request_event(project_id);
CREATE INDEX tx_request_event_created_at ON polyflow.tx_request_event(created_at);


CREATE TABLE polyflow.error_event (
    id         EVENT_ID                 PRIMARY KEY,
    project_id PROJECT_ID               NOT NULL REFERENCES polyflow.project(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    tracker    EVENT_TRACKER_MODEL      NOT NULL,
    errors     VARCHAR[]                NOT NULL,
    wallet     WALLET_STATE,
    device     DEVICE_STATE             NOT NULL,
    network    NETWORK_STATE
);

CREATE INDEX error_event_project_id ON polyflow.error_event(project_id);
CREATE INDEX error_event_created_at ON polyflow.error_event(created_at);

CREATE TABLE polyflow.blockchain_error_event (
    id         EVENT_ID                 PRIMARY KEY,
    project_id PROJECT_ID               NOT NULL REFERENCES polyflow.project(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    tracker    EVENT_TRACKER_MODEL      NOT NULL,
    errors     VARCHAR[]                NOT NULL,
    wallet     WALLET_STATE             NOT NULL,
    device     DEVICE_STATE             NOT NULL,
    network    NETWORK_STATE            NOT NULL,
    tx         TX_DATA                  NOT NULL
);

CREATE INDEX blockchain_error_event_project_id ON polyflow.blockchain_error_event(project_id);
CREATE INDEX blockchain_error_event_created_at ON polyflow.blockchain_error_event(created_at);

CREATE TABLE polyflow.user_landed_event (
    id         EVENT_ID                 PRIMARY KEY,
    project_id PROJECT_ID               NOT NULL REFERENCES polyflow.project(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    tracker    EVENT_TRACKER_MODEL      NOT NULL,
    wallet     WALLET_STATE,
    device     DEVICE_STATE             NOT NULL,
    network    NETWORK_STATE
);

CREATE INDEX user_landed_event_project_id ON polyflow.user_landed_event(project_id);
CREATE INDEX user_landed_event_created_at ON polyflow.user_landed_event(created_at);
