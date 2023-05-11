CREATE TABLE polyflow.wallet_address_alias (
    alias_name     VARCHAR    NOT NULL,
    wallet_address VARCHAR    NOT NULL,
    project_id     PROJECT_ID NOT NULL REFERENCES polyflow.project(id),
    UNIQUE (alias_name, project_id)
);

CREATE INDEX ON polyflow.wallet_address_alias(project_id);

CREATE TABLE polyflow.user_id_alias (
    alias_name VARCHAR    NOT NULL,
    user_id    VARCHAR    NOT NULL,
    project_id PROJECT_ID NOT NULL REFERENCES polyflow.project(id),
    UNIQUE (alias_name, project_id)
);

CREATE INDEX ON polyflow.user_id_alias(project_id);

CREATE TABLE polyflow.session_id_alias (
    alias_name VARCHAR    NOT NULL,
    session_id VARCHAR    NOT NULL,
    project_id PROJECT_ID NOT NULL REFERENCES polyflow.project(id),
    UNIQUE (alias_name, project_id)
);

CREATE INDEX ON polyflow.session_id_alias(project_id);
