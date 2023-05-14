(ns db.init-tables
  (:require [db.connection :as db]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]))

(defn query [string]
  (.query db/pool string))

(defn create-team []
  (query
    "CREATE TABLE IF NOT EXISTS team (
      id SERIAL PRIMARY KEY,
      created_at timestamptz DEFAULT NOW() NOT NULL
    )"))

(defn create-server []
  (query
    "CREATE TABLE IF NOT EXISTS server (
      server_id VARCHAR(255) PRIMARY KEY,
      server VARCHAR(100) NOT NULL,
      created_at timestamptz DEFAULT NOW() NOT NULL
    )"))

(defn create-rank []
  (query
    "CREATE TABLE IF NOT EXISTS rank (
      rank VARCHAR(255) PRIMARY KEY,
      points INT NOT NULL UNIQUE,
      created_at timestamptz DEFAULT NOW() NOT NULL
    )"))

(defn create-author []
  (query
    "CREATE TABLE IF NOT EXISTS author (
      author VARCHAR(255) PRIMARY KEY,
      created_at timestamptz DEFAULT NOW() NOT NULL
    )"))

(defn create-map []
  (query
    "CREATE TABLE IF NOT EXISTS map (
      map VARCHAR(100) PRIMARY KEY, -- Nuke
      created_at timestamptz DEFAULT NOW() NOT NULL
    )"))

(defn create-maptype []
  (query
    "CREATE TABLE IF NOT EXISTS maptype (
      maptype VARCHAR(100) PRIMARY KEY,
      created_at timestamptz DEFAULT NOW() NOT NULL
    )"))

(defn create-player []
  (query
    "CREATE TABLE IF NOT EXISTS player (
      player_id VARCHAR(255) PRIMARY KEY,
      player VARCHAR(255) NOT NULL,
      tag VARCHAR(255) DEFAULT 'UNKNOWN' NOT NULL,
      created_at timestamptz DEFAULT NOW() NOT NULL,
      updated_at timestamptz DEFAULT NOW() NOT NULL
    )"))

(defn add-player-trigger []
  (query "CREATE OR REPLACE FUNCTION trigger_set_timestamp()
         RETURNS TRIGGER AS $$
         BEGIN
           NEW.updated_at = NOW();
             RETURN NEW;
             END;
         $$ LANGUAGE plpgsql;
         CREATE TRIGGER set_timestamp
         BEFORE UPDATE ON player
         FOR EACH ROW
           EXECUTE PROCEDURE trigger_set_timestamp()"))

(defn create-quote []
  (query
    "CREATE TABLE IF NOT EXISTS quote (
      id SERIAL PRIMARY KEY,
      quote TEXT NOT NULL,
      author VARCHAR(255) NOT NULL REFERENCES author(author),
      created_at timestamptz DEFAULT NOW() NOT NULL
    )"))

(defn create-match []
  (query
    "CREATE TABLE IF NOT EXISTS match (
      id SERIAL PRIMARY KEY,
      map VARCHAR(100) NOT NULL REFERENCES map(map),
      team1_score INT NOT NULL,
      team2_score INT NOT NULL,
      team1 INT NOT NULL UNIQUE REFERENCES team(id),
      team2 INT NOT NULL UNIQUE REFERENCES team(id),
      created_at timestamptz DEFAULT NOW() NOT NULL
    )"))


(defn create-map-server []
  (query
    "CREATE TABLE IF NOT EXISTS map_server (
      id SERIAL PRIMARY KEY,
      map VARCHAR(100) NOT NULL REFERENCES map(map), -- Nuke
      server_id VARCHAR(255) NOT NULL REFERENCES server(server_id),
      maptype VARCHAR(100) NOT NULL REFERENCES maptype(maptype),
      created_at timestamptz DEFAULT NOW() NOT NULL
    )"))

(defn create-player-server-points []
  (query
    "CREATE TABLE IF NOT EXISTS player_server_points (
      player_id VARCHAR(255) NOT NULL REFERENCES player(player_id),
      server_id VARCHAR(255) NOT NULL REFERENCES server(server_id),
      points DOUBLE PRECISION DEFAULT 128 NOT NULL CHECK (points >= 0),
      created_at timestamptz DEFAULT NOW() NOT NULL,
      PRIMARY KEY (player_id, server_id)
    )"))

(defn create-player-team-server []
  (query
    "CREATE TABLE IF NOT EXISTS player_team_server (
      player_id VARCHAR(255) NOT NULL REFERENCES player(player_id),
      team_id INT NOT NULL REFERENCES team(id),
      server_id VARCHAR(255) NOT NULL REFERENCES server(server_id),
      created_at timestamptz DEFAULT NOW() NOT NULL,
      PRIMARY KEY (player_id, team_id)
    )"))

(defn insert-author []
  (query "INSERT INTO author (author)
         VALUES
         ('gL'),
         ('tallah'),
         ('Lunza'),
         ('V1nt'),
         ('ArtGame'),
         ('K@FF33'),
         ('nooby'),
         ('Souffle'),
         ('Osas'),
         ('st4r'),
         ('Di'),
         ('hydro'),
         ('Pashy'),
         ('Anime'),
         ('PAK'),
         ('Keep'),
         ('Lamochina'),
         ('GoGy'),
         ('Empty'),
         ('Alex Yao'),
         ('Aqua|Deniz'),
         ('Ruruy'),
         ('ImBatyshka'),
         ('Bosco'),
         ('barisbaba'),
         ('vic'),
         ('Angellama'),
         ('ceh9'),
         ('markeloff'),
         ('Zeus'),
         ('evendren'),
         ('s1m0Ng'),
         ('TaHkuCT'),
         ('blackzer0'),
         ('coby'),
         ('Shkodran Mustafi'),
         ('fuz'),
         ('Bella Witchy'),
         ('Bolyarin'),
         ('yUGi'),
         ('Shadow King'),
         ('darkij'),
         ('Letho'),
         ('kecha'),
         ('Edu'),
         ('Ness'),
         ('Fen9l'),
         ('luemmel'),
         ('Nepstar'),
         ('MrBrain'),
         ('F-TeN'),
         ('antivolt'),
         ('NanaX Community Bot')"))

(defn insert-map []
  (query
    "INSERT INTO map (map)
    VALUES
      ('Nuke'),
      ('Inferno'),
      ('Mirage'),
      ('Dust2'),
      ('Tuscan'),
      ('Train'),
      ('Cache'),
      ('Aztec'),
      ('Cbble'),
      ('CPLFire'),
      ('CPLMill'),
      ('Dust'),
      ('Overpass'),
      ('Prodigy'),
      ('Vertigo'),
      ('Assault'),
      ('HEKrystal'),
      ('IceWorld'),
      ('Mansion'),
      ('PoolDay'),
      ('PoolDay2')"))

(defn insert-maptype []
  (query "INSERT INTO maptype (maptype) VALUES ('main'), ('extra'), ('fun')"))

(defn insert-quote []
  (query
    "INSERT INTO quote (quote, author)
    VALUES
      ('Go second mid, then first mid, then jungle, then B.', 'gL'),
      ('Kurwa... Ja pierdolę.', 'tallah'),
      ('Оеёй, другалёчек!', 'Lunza'),
      ('Fucking game.', 'V1nt'),
      ('Понял?', 'ArtGame'),
      ('Hey, homeless.', 'K@FF33'),
      ('Zaebis!', 'K@FF33'),
      ('Hellooooo!', 'nooby'),
      ('I am not bothered.', 'nooby'),
      ('Я сверху.', 'Souffle'),
      ('Чё за бот такой тупой?', 'Osas'),
      ('СОНЯ КОПИТАН!', 'Osas'),
      ('Работай сука. Тут тебе не плачут.', 'Osas'),
      ('Blyat! Pizdec!', 'st4r'),
      ('We are professionals.', 'st4r'),
      ('Да иди ты на хуй!', 'Di'),
      ('Я за ящик.', 'hydro'),
      ('Я просто танцую.', 'Pashy'),
      ('Алло!?', 'Anime'),
      ('Домой!', 'Anime'),
      ('По пульке стреляйте.', 'PAK'),
      ('Kill him.', 'Keep'),
      ('s1mple.', 'Keep'),
      ('На америке надо по пульке стрелять.', 'PAK'),
      ('На хуй Калаш, Галил ебашит.', 'Di'),
      ('И ни одной моей цитаты... Ну и пожалуйста, блть, ну и пошло всё в пизду, нахуя мне это надо...', 'Lamochina'),
      ('Bluu dor!', 'GoGy'),
      ('Пёс ебучий!', 'Empty'),
      ('See you tomorrow.', 'Alex Yao'),
      ('Jebany fart.', 'tallah'),
      ('Do you need a chair?🪑', 'tallah'),
      ('NANAX2STRONG.', 'tallah'),
      ('Give info please!', 'Di'),
      ('Good morning friends. I wish you a healthy and happy day.', 'Aqua|Deniz'),
      ('Good morning friends. Wishing you a joyful and loving day.', 'Aqua|Deniz'),
      ('My birthday is July 30th.', 'Ruruy'),
      ('Нахуй я смок купил, не знаю. Сохранить?', 'st4r'),
      ('Я это запикаю.', 'PAK'),
      ('Ya!', 'Alex Yao'),
      ('Чё он за мифозник такой?!', 'ImBatyshka'),
      ('O my God, it''s beautiful!', 'Bosco'),
      ('Na na huy!', 'Bosco'),
      ('Это психологическое давление.', 'Di'),
      ('Да ёшкин-матрёшкин!', 'gL'),
      ('Бог наградил меня интеллектом, как у Гарри Каспарова.', 'gL'),
      ('Это так не работает.', 'gL'),
      ('Ну тогда во второй команде два без голоса.', 'Di'),
      ('Может сами поделите?', 'Di'),
      ('Да блять, делите сами!', 'Di'),
      ('Fuck you, bitch!', 'barisbaba'),
      ('Who wanna call?', 'st4r'),
      ('Что же вы творите-то?', 'ImBatyshka'),
      ('Эх, вы!', 'ImBatyshka'),
      ('Всё-таки, я ненавижу Nuke.', 'V1nt'),
      ('Подсадишь меня?', 'vic'),
      ('Иди сюда, подсажу.', 'Angellama'),
      ('Сосать, уроды! Сосать!', 'ceh9'),
      ('Мы чемпионы СНГ!', 'ceh9'),
      ('Шо?!', 'markeloff'),
      ('Сразу в ебало!', 'Zeus'),
      ('You are bad teammaker.', 'tallah'),
      ('Poshel na huy suka blyat!', 'Bosco'),
      ('Davai.', 'evendren'),
      ('I just took a shower.', 'evendren'),
      ('Poshel na huy!', 'evendren'),
      ('Good call, Simon.', 's1m0Ng'),
      ('Вечер в хату!', 'TaHkuCT'),
      ('MUYA VERNITE NAHYI!!!!', 'TaHkuCT'),
      ('Teams unbalanced.', 'blackzer0'),
      ('Shit!', 'PAK'),
      ('Minatooo.', 'coby'),
      ('?', 'Shkodran Mustafi'),
      ('А я вам сейчас покажу...', 'TaHkuCT'),
      ('Я ему ещё хаешку в сраку дал!', 'TaHkuCT'),
      ('What is the next map?', 'Alex Yao'),
      ('Стакнитесь на А, я на Б пошёл.', 'fuz'),
      ('Pishkaaaaaa!', 'fuz'),
      ('Wazuuuuuup!', 'fuz'),
      ('Wooohoooo!', 'Bella Witchy'),
      ('Oh, tallah, it''s you?!', 'Bella Witchy'),
      ('Fucking bot can u make normal teams?', 'Bella Witchy'),
      ('Kick this bot from nanaX i say many time).', 'Bolyarin'),
      ('Fucking hell.', 'yUGi'),
      ('glhf, Babyysss!', 'Shadow King'),
      ('Первая тима сильнее.', 'Angellama'),
      ('Try to hear sounds of cats like \"meow\".', 'PAK'),
      ('Ты чё ебанулся?', 'st4r'),
      ('Ooooooooooooooooo.', 'darkij'),
      ('Скинуть юсп?', 'Souffle'),
      ('Ooo!', 'Souffle'),
      ('Надо забирать пацана.', 'Letho'),
      ('Он неопасный.', 'Empty'),
      ('Нет, я не пойду, нахуй пошёл он.', 'Empty'),
      ('O my Lord...', 'kecha'),
      ('Одну, по фастику.', 'Empty'),
      ('Where is my money?', 'Edu'),
      ('I am a noob.', 'Ness'),
      ('У меня мышка не стреляет!', 'Ruruy'),
      ('Сейчас я дам энтри в аппсах.', 'Fen9l'),
      ('Hey, Jigar!', 'luemmel'),
      ('Я купаться пошёл.', 'Ruruy'),
      ('У меня интернет вырубили.', 'Ruruy'),
      ('Забери админку у V1nt''а.', 'Lunza'),
      ('У меня интернет вырубили.', 'Lunza'),
      ('У меня свет вырубили.', 'Ruruy'),
      ('У меня свет вырубили.', 'Lunza'),
      ('Эта игра была похожа на смех над человеком.', 'Nepstar'),
      ('I''m here to say hello.', 'MrBrain'),
      ('ERROR! ERROR! ERROR! ERROR! ERROR! = 0 pts.', 'NanaX Community Bot'),
      ('Eto pizdec.', 'F-TeN'),
      ('Есть чё пострелять?', 'antivolt')"))

(defn insert-rank []
  (query
    "INSERT INTO rank (rank, points)
    VALUES
      ('Bot', -65536),
      ('Noob', 128),
      ('Camper', 256),
      ('Lucker', 512),
      ('Strawberry Legend', 1024),
      ('Drunken Master', 2048),
      ('Rambo', 4096),
      ('Terminator', 8192),
      ('Legend', 16384),
      ('Professional', 32768),
      ('Nanaxer', 65536)"))

(defn add-update-player-points-procedure []
  (query
    "CREATE OR REPLACE PROCEDURE
    update_player_points(points_arg double precision, player_id_arg text, server_id_arg text)
    language plpgsql
    as $$
    BEGIN
      UPDATE player_server_points SET points = points + points_arg
      WHERE player_id = player_id_arg AND server_id = server_id_arg;
    EXCEPTION WHEN check_violation THEN
      UPDATE player_server_points SET points = 0
      WHERE player_id = player_id_arg AND server_id = server_id_arg;
    END;$$"))

(defn init-tables []
  (println 'INIT-TABLES)
  (go (try
        (<p! (add-update-player-points-procedure))
        ;(<p! (create-team))
        ;(<p! (create-server))
        ;(<p! (create-rank))
        ;(<p! (create-author))
        ;(<p! (create-map))
        ;(<p! (create-maptype))
        ;(<p! (create-player))
        ;(<p! (add-player-trigger))
        ;(<p! (create-quote))
        ;(<p! (create-match))
        ;(<p! (create-map-server))
        ;(<p! (create-player-server-points))
        ;(<p! (create-player-team-server))
        ;(<p! (insert-author))
        ;(<p! (insert-map))
        ;(<p! (insert-maptype))
        ;(<p! (insert-quote))
        ;(<p! (insert-rank))
        (catch js/Error e (println "ERROR init-tables" e)))))
