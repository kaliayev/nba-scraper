(ns scrappy-ball.stats.per36
  (:require [scrappy-ball.stats.utils :as stat-utils]))

(def fantasy-cats ["pts_per_mp" "fg3_per_mp" "efg%" "eft%" "trb_per_mp" "ast_per_mp" "blk_per_mp" "stl_per_mp" "tov_per_mp"])

(defn effective-percentage
  "The league positional average plus ((the difference between the player's average and the league average) times (the player's attempts over the league positional average of attempts))"
  [fantasy-season stat player-season]
  (let [position (:pos player-season)
        season-by-position (stat-utils/season-by-position fantasy-season position)
        cat-made (if (= (name stat) "fg")
                   "fg_per_mp"
                   "ft_per_mp")
        cat-attempted (if (= cat-made "fg_per_mp")
                        "fga_per_mp"
                        "fta_per_mp")
        position-made (-> season-by-position
                          (stat-utils/filter-stat cat-made)
                          (stat-utils/descriptive-stats-for-position-and-stat cat-made position)
                          :mean)
        position-attempts (-> season-by-position
                              (stat-utils/filter-stat cat-attempted)
                              (stat-utils/descriptive-stats-for-position-and-stat cat-attempted position)
                              :mean)
        position-percentage (/ position-made position-attempts)
        player-attempts (Double. ((keyword cat-attempted) player-season))
        player-percentage (try (/ (Double. ((keyword cat-made) player-season))
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
