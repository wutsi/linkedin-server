INSERT INTO T_SECRET(id, user_id, site_id, linkedin_id, access_token, creation_date_time, modification_date_time)
    VALUES
        (1, 1, 1, 11, 'access-token', now(), now()),
        (666, 666, 1, 6, 'access-token-666', now(), now());

INSERT INTO T_SHARE(secret_fk, site_id, story_id, post_id, linkedin_share_id, share_date_time, success)
    VALUES
        (1, 1, 123, null, '49403943', now(), true);
