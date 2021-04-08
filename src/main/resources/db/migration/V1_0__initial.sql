CREATE TABLE T_SECRET(
  id                      SERIAL NOT NULL PRIMARY KEY,

  user_id                 BIGINT NOT NULL,
  site_id                 BIGINT NOT NULL,
  linkedin_id             VARCHAR(64),
  access_token            TEXT NOT NULL,
  creation_date_time      TIMESTAMPTZ NOT NULL,
  modification_date_time  TIMESTAMPTZ NOT NULL,

  UNIQUE(user_id, site_id)
);

CREATE TABLE T_SHARE(
    id                      SERIAL NOT NULL PRIMARY KEY,

    secret_fk               BIGINT NOT NULL,

    site_id                 BIGINT NOT NULL,
    story_id                BIGINT NOT NULL,
    post_id                 BIGINT,
    linkedin_share_id       VARCHAR(64),
    share_date_time         TIMESTAMPTZ NOT NULL,
    success                 BOOLEAN,
    error_code              INT,
    error_description       TEXT,

    CONSTRAINT fk_secret FOREIGN KEY(secret_fk) REFERENCES T_SECRET(id)
);
