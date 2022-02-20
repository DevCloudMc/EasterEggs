CREATE TABLE IF NOT EXISTS eastereggs
(
    id       INT(11) AUTO_INCREMENT PRIMARY KEY,
    player_uuid   VARCHAR(40),
    category VARCHAR(32),
    egg_id INT(11)
);