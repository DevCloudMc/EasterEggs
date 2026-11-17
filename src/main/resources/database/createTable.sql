CREATE TABLE IF NOT EXISTS eastereggs
(
    player_uuid VARCHAR(36) NOT NULL,
    category    TEXT        NOT NULL,
    egg_id      INTEGER     NOT NULL,
    UNIQUE (player_uuid, category, egg_id)
);

CREATE INDEX IF NOT EXISTS eastereggs_player_uuid ON eastereggs (player_uuid);
