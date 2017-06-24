(ns scrappy-ball.stats.utils
  (:require [incanter.core :as incanter]
            [incanter.charts :as charts]
            [incanter.stats :as desc-stats]))

;; TODO: Should vorps be normalized by standard deviation?

(defn get-player-season
  "Get a player's fantasy season given their name in the format \"Last,First\""
  [fantasy-season player-name]
  (last (sort-by :g (filter #(= player-name (:name %)) fantasy-season))))

(defn season-by-position
  [fantasy-season position]
  (filter #(= position (:pos %)) fantasy-season))

(defn filter-stat
  [positional-season stat]
  (map (comp #(Double. %)
             (keyword stat)) positional-season))

(defn descriptive-stats-for-position-and-stat
  "Given a year, statistics-type [totals or per_game], position, and stat;
  return descriptive some descriptive stats. For a list of available stats, and how to format them,
  check the utils ns."
  [filtered-stats stat position]
  {:position position
   :stat stat
   :mean (desc-stats/mean filtered-stats)
   :median (desc-stats/median filtered-stats)
   :stdev (desc-stats/sd filtered-stats)
   :min (apply min filtered-stats)
   :max (apply max filtered-stats)})

(defn value-over-replacement
  [fantasy-season stat player-season]
  (let [position (:pos player-season)
        league-average (-> fantasy-season
                           (season-by-position position)
                           (filter-stat (name stat))
                           (descriptive-stats-for-position-and-stat (name stat) position)
                           :mean)
        player-total (Double. ((keyword stat) player-season))]
    (cond-> (/ player-total league-average)
      (contains? #{"tov" "tov_per_g" "tov_per_mp"} (name stat)) (/))))

(defn vorps-map
  [fantasy-season player-season fantasy-cats punts]
  (let [vorps (reduce (fn [acc stat]
                        (assoc acc (keyword stat) (value-over-replacement fantasy-season stat player-season)))
                      {} fantasy-cats)
        vorp-sum (reduce-kv (fn [acc k v]
                              (if (contains? punts (name k))
                                acc
                                (+ acc v)))
                            0 vorps)]
    (assoc vorps :total vorp-sum)))

(defn assoc-vorps
  [fantasy-season fantasy-cats punts]
  (reduce (fn [acc player-season]
            (conj acc (assoc player-season :vorps (vorps-map fantasy-season player-season
                                                             fantasy-cats punts))))
          [] fantasy-season))
