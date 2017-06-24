(ns scrappy-ball.scrapers
  (:require [net.cgrand.enlive-html :as html]
            [scrappy-ball.utils :as utils]
            [clojure.set :as sets]
            [scrappy-ball.stats.per36 :as stats-per36]
            [scrappy-ball.stats.totals :as stats-totals]))

(defn pick-stats-set
  [stats-type]
  (case stats-type
    ("totals") utils/fantasy-stats-totals
    ("per_game") utils/fantasy-stats-per-game
    ("per_minute") (sets/union utils/fantasy-stats-per-36)))

(defn get-raw-season
  [year stats-type]
  (let [content (-> year
                    (utils/->url stats-type)
                    html/html-resource
                    (html/select [:tbody])
                    first
                    :content)]
    (filter #(not (string? %)) content)))

(defn player-season->map
  [player-season stats-set]
  (let [relevant-stats (filter #(contains? stats-set (get-in % [:attrs :data-stat])) (:content player-season))]
    (reduce (fn [acc stat]
              (let [stat-name (get-in stat [:attrs :data-stat])]
                (if (not= stat-name "player")
                  (assoc acc (keyword stat-name) (first (:content stat)))
                  (assoc acc :name (get-in stat [:attrs :csk])))))
            {} relevant-stats)))

(defn get-fantasy-season
  [year stats-type]
  (let [stats-set (pick-stats-set stats-type)
        raw-season (get-raw-season year stats-type)
        season-stats (reduce (fn [acc player-season]
                               (conj acc (player-season->map player-season stats-set)))
                             [] raw-season)]
    (filter #(identity (:name %)) season-stats)))
