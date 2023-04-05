CREATE DOMAIN polyflow.USER_ID AS UUID;
CREATE TYPE polyflow.USER_ACCOUNT_TYPE AS ENUM ('EMAIL_REGISTERED');

CREATE TABLE polyflow.user (
    id            USER_ID                  PRIMARY KEY,
    email         VARCHAR                  NOT NULL UNIQUE,
    password_hash VARCHAR                  NOT NULL,
    account_type  USER_ACCOUNT_TYPE        NOT NULL,
    registered_at TIMESTAMP WITH TIME ZONE NOT NULL,
    verified_at   TIMESTAMP WITH TIME ZONE
);

CREATE TABLE polyflow.user_verification_token (
    token      VARCHAR                  PRIMARY KEY,
    user_id    USER_ID                  NOT NULL REFERENCES polyflow.user(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX user_verification_token_user_id ON polyflow.user_verification_token(user_id);

CREATE TABLE polyflow.user_password_reset_token (
    token      VARCHAR                  PRIMARY KEY,
    user_id    USER_ID                  NOT NULL REFERENCES polyflow.user(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX user_password_reset_token_user_id ON polyflow.user_password_reset_token(user_id);

CREATE DOMAIN polyflow.PROJECT_FEATURES_ID AS UUID;

CREATE TABLE polyflow.project_features (
    id               PROJECT_FEATURES_ID PRIMARY KEY,
    gas_station      BOOLEAN             NOT NULL,
    network_switcher BOOLEAN             NOT NULL,
    connect_wallet   BOOLEAN             NOT NULL,
    compliance       BOOLEAN             NOT NULL,
    error_messages   BOOLEAN             NOT NULL
);

CREATE DOMAIN polyflow.PROJECT_ID AS UUID;

CREATE TABLE polyflow.project (
    id                  PROJECT_ID               PRIMARY KEY,
    name                VARCHAR                  NOT NULL,
    api_key             VARCHAR                      NULL UNIQUE,
    owner_id            USER_ID                  NOT NULL REFERENCES polyflow.user(id),
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    whitelisted_domains VARCHAR[]                NOT NULL,
    features_id         PROJECT_FEATURES_ID      NOT NULL UNIQUE REFERENCES polyflow.project_features(id)
);

CREATE INDEX project_owner_id ON polyflow.project(owner_id);
CREATE INDEX project_created_at ON polyflow.project(created_at);
