(ns db.create-tables
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

(defn create-country []
  (query
    "CREATE TABLE IF NOT EXISTS country (
      country VARCHAR(100) PRIMARY KEY,
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
      nanax_points INT DEFAULT 0 NOT NULL,
      tag VARCHAR(255) DEFAULT 'UNKNOWN' NOT NULL,
      country VARCHAR(255) DEFAULT 'UNKNOWN' NOT NULL REFERENCES country(country),
      created_at timestamptz DEFAULT NOW() NOT NULL,
      updated_at timestamptz DEFAULT NOW() NOT NULL
    )"))

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
      points DOUBLE PRECISION DEFAULT 128 NOT NULL,
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

(defn create-tables []
  (println 'CREATE-TABLES)
  (go (try
        (<p! (create-team))
        (<p! (create-server))
        (<p! (create-rank))
        (<p! (create-author))
        (<p! (create-country))
        (<p! (create-map))
        (<p! (create-maptype))
        (<p! (create-player))
        (<p! (create-quote))
        (<p! (create-match))
        (<p! (create-map-server))
        (<p! (create-player-server-points))
        (<p! (create-player-team-server))
        (catch js/Error e (do (println "ERROR create-tables" e))))))
