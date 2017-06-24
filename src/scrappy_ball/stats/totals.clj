(ns scrappy-ball.stats.totals
  (:require [scrappy-ball.stats.utils :as stat-utils]))

(def fantasy-cats ["pts" "fg3" "efg%" "eft%" "trb" "ast" "blk" "stl" "tov"])

(defn effective-percentage
  "The league positional average plus ((the difference between the player's average and the league average) times (the player's attempts over the league positional average of attempts))"
  [fantasy-season stat player-season]
  (let [position (:pos player-season)
        season-by-position (stat-utils/season-by-position fantasy-season position)
        position-made (-> season-by-position
                          (stat-utils/filter-stat (name stat))
                          (stat-utils/descriptive-stats-for-position-and-stat (name stat) position)
                          :mean)
        position-attempts (-> season-by-position
                              (stat-utils/filter-stat (str (name stat) "a"))
                              (stat-utils/descriptive-stats-for-position-and-stat (str (name stat) "a") position)
                              :mean)
        position-percentage (/ position-made position-attempts)
        player-attempts (Double. ((keyword (str (name stat) "a")) player-season))
        player-percentage (try (/ (Double. ((keyword stat) player-season))
                                  player-attempts)
                               (catch ArithmeticException e 0))]
    (+ position-percentage
       (* (- player-percentage position-percentage)
          (/ player-attempts position-attempts)))))

(defn assoc-epercentages
  [fantasy-season]
  (reduce (fn [acc player-season]
            (conj acc (-> player-season
                          (assoc :efg% (effective-percentage fantasy-season "fg" player-season))
                          (assoc :eft% (effective-percentage fantasy-season "ft" player-season)))))
          [] fantasy-season))

(defn get-fantasy-season
  [fantasy-season punts]
  (let [fantasy-season (filter #(> (Double. (:mp %)) 1000) fantasy-season)]
    (-> fantasy-season
        assoc-epercentages
        (stat-utils/assoc-vorps fantasy-cats punts))))
