(ns scrappy-ball.scrapers
  (:require [net.cgrand.enlive-html :as html]
            [scrappy-ball.utils :as utils]
            [clojure.set :as sets]
            [scrappy-ball.stats.per36 :as stats-per36]
            [scrappy-ball.stats.totals :as stats-totals]
            [incanter.charts :as charts]
            [incanter.core :as incanter]))

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

(defn get-season-totals
  [year punts]
  (let [season-with-punts (-> (get-fantasy-season year "totals")
                              (stats-totals/get-fantasy-season punts))]
    (sort-by #(get-in % [:vorps :total]) #(compare %2 %1) season-with-punts)))

(defn get-season-per36
  [year punts]
  (let [season-with-punts (-> (get-fantasy-season year "per_minute")
                              (stats-per36/get-fantasy-season punts))]
    (sort-by #(get-in % [:vorps :total]) #(compare %2 %1) season-with-punts)))

(defn fantasy-val-reducer
  [v player]
  (conj v {:name (:name player) :fantasy-value (get-in player [:vorps :total])}))

(defn top-n-fantasy-players
  [ordered-season n]
  (reduce fantasy-val-reducer
          [] (take n ordered-season)))

(defn top-n-players-by-pos
  [ordered-season n pos]
  (->> ordered-season
       (filter #(= pos (:pos %)))
       (take n)
       (reduce fantasy-val-reducer [])))

(defn get-player-from-season
  [season player]
  (first (filter #(= player (:name %)) season)))

(defn merge-fantasy-seasons
  [ordered-seasons]
  "seasons should be ordered most-recent first"
  (let [merged-seasons (reduce (fn [acc player-name]
                                 (let [player-seasons (map #(get-in (get-player-from-season % player-name) [:vorps :total]) ordered-seasons)
                                       position (-> ordered-seasons first
                                                    (get-player-from-season player-name)
                                                    :pos)]
                                   (conj acc {:name player-name
                                              :fantasy-value (apply + player-seasons)
                                              :pos position})))
                               [] (->> ordered-seasons
                                       first
                                       (map :name)
                                       distinct))]
    (sort-by #(second %) #(compare %2 %1) merged-seasons)))

(defn gen-picks-with-punts
  [year punts]
  (let [punts-per36 (map #(if (contains? #{"efg%" "eft%"} %) % (str % "_per_mp")) punts)
        year-totals (get-season-totals year punts)
        year-per36 (get-season-per36 year punts-per36)
        merged-season (merge-fantasy-seasons [year-totals year-per36])
        val-reducer (fn [v player]
                      (conj v {:name (:name player) :fantasy-value (:fantasy-value player)}))
        top-position-players (fn [pos] (->> merged-season
                                            (filter #(= pos (:pos %)))
                                            (take 10)
                                            (reduce val-reducer [])))]
    (reduce (fn [acc position]
              (assoc acc position (top-position-players (name position))))
            {} [:PG :SG :SF :PF :C])))

(defn drop-player-from-list
  [list name])

(defn player-fantasy-values-over-years
  [player years]
  (reduce (fn
            [acc year]
            (let [player-year (->> (get-season-totals year [])
                                   (filter #(= player (:name %)))
                                   first)
                  fantasy-val (get-in player-year [:vorps :total])]
              (conj acc fantasy-val)))
          [] years))

(defn plot-fantasy-trajectories
  [player years-seq]
  (let [fantasy-values (player-fantasy-values-over-years player years-seq)]
    (incanter/view
     (charts/line-chart years-seq fantasy-values
                        :title (str player " Fantasy Value")
                        :x-label "NBA Season"
                        :y-label "Normalized Positional Fantasy Value"))))
