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

(defn add-update-player-points-procedure []
  (query
   "CREATE OR REPLACE PROCEDURE
    update_player_points(points_arg double precision, player_id_arg text, server_id_arg text)
    language plpgsql
    as $$
    BEGIN
      UPDATE player_server_points SET points = points + points_arg + 3
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
        (catch js/Error e (println "ERROR init-tables" e)))))
