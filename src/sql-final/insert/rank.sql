INSERT INTO rank (rank, points, color)
VALUES
('Bot', 0, '#424242'),
('Noob', 128, '#bdbdbd'),
('Lucker', 256, '#00ce21'),
('Strawberry Legend', 512, '#ff8413'),
('Drunken Master', 1024, '#13f1ff'),
('Rambo', 2048, '#c8ff2c'),
('Terminator', 4096, '#009fd1'),
('Terminator 2', 8192, '#2c5aff'),
('Professional', 16384, '#a513ff'),
('Legend', 32768, '#ffd600'),
('Nanaxer', 65536, '#d00a0a');

SELECT * FROM rank WHERE points <= 512 ORDER BY points DESC LIMIT 1;
