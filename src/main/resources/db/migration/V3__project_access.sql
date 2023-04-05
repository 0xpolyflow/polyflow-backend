CREATE TYPE polyflow.ACCESS_TYPE AS ENUM ('READ', 'WRITE');

CREATE TABLE polyflow.user_project_access (
    user_id     USER_ID     NOT NULL,
    project_id  PROJECT_ID  NOT NULL,
    access_type ACCESS_TYPE NOT NULL,
    PRIMARY KEY (user_id, project_id)
);
