ALTER TYPE polyflow.EVENT_TRACKER_MODEL ADD ATTRIBUTE sdk_version VARCHAR;

CREATE TABLE polyflow.sdk_error_event (
    id         EVENT_ID                 PRIMARY KEY,
    project_id PROJECT_ID               NOT NULL REFERENCES polyflow.project(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    tracker    EVENT_TRACKER_MODEL      NOT NULL,
    wallet     WALLET_STATE,
    device     DEVICE_STATE,
    network    NETWORK_STATE,
    metadata   VARCHAR
);

CREATE INDEX sdk_error_event_project_id ON polyflow.sdk_error_event(project_id);
CREATE INDEX sdk_error_event_created_at ON polyflow.sdk_error_event(created_at);
